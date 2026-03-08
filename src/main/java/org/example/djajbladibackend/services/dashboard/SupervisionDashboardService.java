package org.example.djajbladibackend.services.dashboard;

import org.example.djajbladibackend.dto.dashboard.SupervisionDashboardResponse;
import org.example.djajbladibackend.exception.DateRangeTooLargeException;
import org.example.djajbladibackend.exception.ForbiddenRoleException;
import org.example.djajbladibackend.exception.InvalidDataException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.ApprovalStatus;
import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.BatchStatus;
import org.example.djajbladibackend.models.DailyMortalityRecord;
import org.example.djajbladibackend.models.FeedingRecord;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.DailyMortalityRecordRepository;
import org.example.djajbladibackend.repository.FeedingRecordRepository;
import org.example.djajbladibackend.repository.HealthRecordRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.example.djajbladibackend.services.fcr.StrainWeightCurveService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class SupervisionDashboardService {

    private final FeedingRecordRepository feedingRepository;
    private final DailyMortalityRecordRepository mortalityRepository;
    private final HealthRecordRepository healthRepository;
    private final UserRepository userRepository;
    private final BatchRepository batchRepository;
    private final StrainWeightCurveService weightCurveService;

    @Value("${app.supervision.max-date-range-days:366}")
    private int maxDateRangeDays;

    @Value("${app.fcr.alert-threshold:1.8}")
    private BigDecimal fcrAlertThreshold;

    public SupervisionDashboardService(FeedingRecordRepository feedingRepository,
                                        DailyMortalityRecordRepository mortalityRepository,
                                        HealthRecordRepository healthRepository,
                                        UserRepository userRepository,
                                        BatchRepository batchRepository,
                                        StrainWeightCurveService weightCurveService) {
        this.feedingRepository = feedingRepository;
        this.mortalityRepository = mortalityRepository;
        this.healthRepository = healthRepository;
        this.userRepository = userRepository;
        this.batchRepository = batchRepository;
        this.weightCurveService = weightCurveService;
    }

    public SupervisionDashboardResponse getDashboard(LocalDate startDate, LocalDate endDate, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + adminEmail));
        if (admin.getRole() != RoleEnum.Admin) {
            throw new ForbiddenRoleException("Only Admin can access the supervision dashboard");
        }
        if (startDate.isAfter(endDate)) {
            throw new InvalidDataException("Start date must be before or equal to end date");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) > maxDateRangeDays) {
            throw new DateRangeTooLargeException("Date range cannot exceed " + maxDateRangeDays + " days");
        }

        var feedingRecords = feedingRepository.findByFeedingDateBetweenWithRelations(startDate, endDate);
        var mortalityRecords = mortalityRepository.findByRecordDateBetweenWithRelations(startDate, endDate);
        var pendingAlerts = healthRepository.findPendingApprovalWithRelations();

        // Collect all unique batch IDs appearing in the date range to prefetch cumulative data
        Set<Long> batchIds = feedingRecords.stream()
                .map(f -> f.getBatch().getId())
                .collect(Collectors.toSet());
        mortalityRecords.stream()
                .map(m -> m.getBatch().getId())
                .forEach(batchIds::add);

        // Prefetch ALL feeding records per batch (needed for cumulative FCR)
        Map<Long, List<FeedingRecord>> allFeedingByBatch = new HashMap<>();
        for (Long batchId : batchIds) {
            allFeedingByBatch.put(batchId, feedingRepository.findByBatchIdWithRelations(batchId));
        }

        List<SupervisionDashboardResponse.BatchDailySummary> summaries = buildDailySummaries(
                feedingRecords, mortalityRecords, allFeedingByBatch);

        List<SupervisionDashboardResponse.HealthAlertSummary> alertSummaries = pendingAlerts.stream()
                .filter(h -> h.getApprovalStatus() == ApprovalStatus.PENDING_APPROVAL)
                .map(h -> SupervisionDashboardResponse.HealthAlertSummary.builder()
                        .healthRecordId(h.getId())
                        .batchId(h.getBatch().getId())
                        .batchNumber(h.getBatch().getBatchNumber())
                        .diagnosis(h.getDiagnosis())
                        .treatment(h.getTreatment())
                        .treatmentCost(h.getTreatmentCost())
                        .examinationDate(h.getExaminationDate())
                        .veterinarianName(h.getVeterinarian().getFullName())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();

        // Build FCR summaries for all active batches
        List<Batch> activeBatches = batchRepository.findByStatus(BatchStatus.Active);
        List<SupervisionDashboardResponse.BatchFcrSummary> fcrSummaries =
                buildFcrSummaries(activeBatches);

        return SupervisionDashboardResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .batchSummaries(summaries)
                .pendingAlerts(alertSummaries)
                .fcrSummaries(fcrSummaries)
                .build();
    }

    /**
     * Calcul des FCR en temps reel pour tous les lots actifs.
     * Exposed for the dedicated FCR endpoint as well.
     */
    public List<SupervisionDashboardResponse.BatchFcrSummary> buildFcrSummaries(List<Batch> batches) {
        List<SupervisionDashboardResponse.BatchFcrSummary> result = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Batch batch : batches) {
            if (batch.getArrivalDate() == null || batch.getChickenCount() == null) continue;

            List<FeedingRecord> allFeeding = feedingRepository.findByBatchIdWithRelations(batch.getId());
            if (allFeeding.isEmpty()) continue;

            int ageInDays = (int) ChronoUnit.DAYS.between(batch.getArrivalDate(), today) + 1;
            if (ageInDays <= 0) continue;

            // Total mortality up to today
            int totalMortality = mortalityRepository
                    .findByBatchIdAndRecordDateBetweenWithRelations(batch.getId(), batch.getArrivalDate(), today)
                    .stream()
                    .mapToInt(DailyMortalityRecord::getMortalityCount)
                    .sum();

            int aliveChickens = Math.max(1, batch.getChickenCount() - totalMortality);

            BigDecimal totalFeedKg = allFeeding.stream()
                    .filter(f -> !f.getFeedingDate().isAfter(today))
                    .map(FeedingRecord::getQuantity)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal estimatedWeightKg = weightCurveService.getEstimatedWeightKg(batch.getStrain(), ageInDays);

            BigDecimal totalLiveMassKg = estimatedWeightKg.multiply(BigDecimal.valueOf(aliveChickens));
            BigDecimal cumulativeFcr = null;
            if (totalLiveMassKg.compareTo(BigDecimal.ZERO) > 0) {
                cumulativeFcr = totalFeedKg.divide(totalLiveMassKg, 3, RoundingMode.HALF_UP);
            }

            boolean fcrAlert = cumulativeFcr != null && cumulativeFcr.compareTo(fcrAlertThreshold) > 0;
            String fcrStatus = computeFcrStatus(cumulativeFcr);

            result.add(SupervisionDashboardResponse.BatchFcrSummary.builder()
                    .batchId(batch.getId())
                    .batchNumber(batch.getBatchNumber())
                    .strain(batch.getStrain())
                    .ageInDays(ageInDays)
                    .aliveChickens(aliveChickens)
                    .totalFeedConsumedKg(totalFeedKg.setScale(2, RoundingMode.HALF_UP))
                    .estimatedWeightKg(estimatedWeightKg)
                    .actualWeightKg(null)
                    .cumulativeFcr(cumulativeFcr)
                    .fcrAlert(fcrAlert)
                    .fcrThreshold(fcrAlertThreshold)
                    .fcrStatus(fcrStatus)
                    .build());
        }

        result.sort(Comparator.comparing(SupervisionDashboardResponse.BatchFcrSummary::getBatchNumber));
        return result;
    }

    private List<SupervisionDashboardResponse.BatchDailySummary> buildDailySummaries(
            List<FeedingRecord> feedingRecords,
            List<org.example.djajbladibackend.models.DailyMortalityRecord> mortalityRecords,
            Map<Long, List<FeedingRecord>> allFeedingByBatch) {

        List<SupervisionDashboardResponse.BatchDailySummary> summaries = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        for (var f : feedingRecords) {
            String key = f.getBatch().getId() + "|" + f.getFeedingDate();
            if (seen.add(key)) {
                Batch batch = f.getBatch();
                LocalDate date = f.getFeedingDate();

                BigDecimal totalQty = feedingRecords.stream()
                        .filter(x -> x.getBatch().getId().equals(batch.getId()) && x.getFeedingDate().equals(date))
                        .map(FeedingRecord::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                int mortality = mortalityRecords.stream()
                        .filter(x -> x.getBatch().getId().equals(batch.getId()) && x.getRecordDate().equals(date))
                        .mapToInt(DailyMortalityRecord::getMortalityCount)
                        .sum();

                summaries.add(buildDailySummary(batch, date, totalQty, mortality,
                        f.getRecordedBy().getFullName(), allFeedingByBatch, mortalityRecords));
            }
        }

        for (var m : mortalityRecords) {
            String key = m.getBatch().getId() + "|" + m.getRecordDate();
            if (seen.add(key)) {
                Batch batch = m.getBatch();
                LocalDate date = m.getRecordDate();

                BigDecimal totalQty = feedingRecords.stream()
                        .filter(x -> x.getBatch().getId().equals(batch.getId()) && x.getFeedingDate().equals(date))
                        .map(FeedingRecord::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                summaries.add(buildDailySummary(batch, date, totalQty, m.getMortalityCount(),
                        m.getRecordedBy().getFullName(), allFeedingByBatch, mortalityRecords));
            }
        }

        summaries.sort(Comparator.comparing(SupervisionDashboardResponse.BatchDailySummary::getDate).reversed()
                .thenComparing(SupervisionDashboardResponse.BatchDailySummary::getBatchNumber));
        return summaries;
    }

    private SupervisionDashboardResponse.BatchDailySummary buildDailySummary(
            Batch batch,
            LocalDate date,
            BigDecimal totalQtyToday,
            int mortalityToday,
            String recordedByName,
            Map<Long, List<FeedingRecord>> allFeedingByBatch,
            List<DailyMortalityRecord> mortalityRecords) {

        int ageInDays = batch.getArrivalDate() != null
                ? (int) ChronoUnit.DAYS.between(batch.getArrivalDate(), date) + 1
                : 0;

        BigDecimal estimatedWeightKg = ageInDays > 0
                ? weightCurveService.getEstimatedWeightKg(batch.getStrain(), ageInDays)
                : BigDecimal.ZERO;

        // Cumulative feed from day 1 to current date (inclusive)
        List<FeedingRecord> allFeedForBatch = allFeedingByBatch.getOrDefault(batch.getId(), List.of());
        BigDecimal cumulativeFeedKg = allFeedForBatch.stream()
                .filter(f -> !f.getFeedingDate().isAfter(date))
                .map(FeedingRecord::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Cumulative mortality up to current date
        int cumulativeMortality = mortalityRecords.stream()
                .filter(m -> m.getBatch().getId().equals(batch.getId()) && !m.getRecordDate().isAfter(date))
                .mapToInt(DailyMortalityRecord::getMortalityCount)
                .sum();
        // Add today's mortality not yet in the list (when coming from feeding loop)
        // We rely on the mortalityRecords list already containing today's record if it exists.

        int aliveChickens = Math.max(1, batch.getChickenCount() != null
                ? batch.getChickenCount() - cumulativeMortality
                : 1);

        BigDecimal totalLiveMassKg = estimatedWeightKg.multiply(BigDecimal.valueOf(aliveChickens));

        BigDecimal cumulativeFcr = null;
        if (totalLiveMassKg.compareTo(BigDecimal.ZERO) > 0) {
            cumulativeFcr = cumulativeFeedKg.divide(totalLiveMassKg, 3, RoundingMode.HALF_UP);
        }

        // Daily FCR: today's feed / (alive * daily weight gain)
        BigDecimal dailyFcr = null;
        if (ageInDays > 1) {
            BigDecimal prevWeight = weightCurveService.getEstimatedWeightKg(batch.getStrain(), ageInDays - 1);
            BigDecimal dailyGainKg = estimatedWeightKg.subtract(prevWeight);
            BigDecimal dailyLiveMassGainKg = dailyGainKg.multiply(BigDecimal.valueOf(aliveChickens));
            if (dailyLiveMassGainKg.compareTo(BigDecimal.ZERO) > 0 && totalQtyToday.compareTo(BigDecimal.ZERO) > 0) {
                dailyFcr = totalQtyToday.divide(dailyLiveMassGainKg, 3, RoundingMode.HALF_UP);
            }
        }

        boolean fcrAlert = cumulativeFcr != null && cumulativeFcr.compareTo(fcrAlertThreshold) > 0;
        boolean abnormal = isAbnormalConsumption(batch.getStrain(), ageInDays, batch.getChickenCount(), totalQtyToday, mortalityToday);

        return SupervisionDashboardResponse.BatchDailySummary.builder()
                .batchId(batch.getId())
                .batchNumber(batch.getBatchNumber())
                .date(date)
                .totalQuantityEaten(totalQtyToday.setScale(2, RoundingMode.HALF_UP))
                .mortalityCount(mortalityToday)
                .recordedByName(recordedByName)
                .abnormalConsumption(abnormal)
                .ageInDays(ageInDays)
                .estimatedWeightKg(estimatedWeightKg)
                .actualWeightKg(null)
                .cumulativeFeedKg(cumulativeFeedKg.setScale(2, RoundingMode.HALF_UP))
                .cumulativeFcr(cumulativeFcr)
                .dailyFcr(dailyFcr)
                .fcrAlert(fcrAlert)
                .build();
    }

    /**
     * Age-aware abnormal consumption check.
     * Uses the expected daily feed per bird from the weight curve (weight gain * target FCR 1.8).
     * Falls back to 0.5 kg/bird/day if no age data available.
     */
    private boolean isAbnormalConsumption(String strain, int ageInDays,
                                           Integer chickenCount, BigDecimal totalQuantity, int mortality) {
        if (chickenCount == null || chickenCount <= 0) return false;
        BigDecimal perChicken = totalQuantity.divide(BigDecimal.valueOf(chickenCount), 4, RoundingMode.HALF_UP);

        BigDecimal threshold;
        if (ageInDays > 1) {
            BigDecimal weightToday = weightCurveService.getEstimatedWeightKg(strain, ageInDays);
            BigDecimal weightYesterday = weightCurveService.getEstimatedWeightKg(strain, ageInDays - 1);
            BigDecimal dailyGain = weightToday.subtract(weightYesterday);
            // Expected daily feed = daily gain * target FCR 2.0 (generous upper bound)
            threshold = dailyGain.multiply(BigDecimal.valueOf(2.0)).add(BigDecimal.valueOf(0.02));
        } else {
            threshold = BigDecimal.valueOf(0.05);
        }

        boolean excessiveConsumption = perChicken.compareTo(threshold) > 0;
        boolean excessiveMortality = mortality > chickenCount / 100;
        return excessiveConsumption || excessiveMortality;
    }

    /**
     * FCR status label based on industry benchmarks for broilers.
     * < 1.6  -> EXCELLENT
     * 1.6-1.8 -> BON
     * 1.8-2.0 -> ALERTE
     * > 2.0  -> CRITIQUE
     */
    private String computeFcrStatus(BigDecimal fcr) {
        if (fcr == null) return "INCONNU";
        double v = fcr.doubleValue();
        if (v < 1.6) return "EXCELLENT";
        if (v < 1.8) return "BON";
        if (v < 2.0) return "ALERTE";
        return "CRITIQUE";
    }
}
