package org.example.djajbladibackend.services.dashboard;

import org.example.djajbladibackend.dto.dashboard.SupervisionDashboardResponse;
import org.example.djajbladibackend.exception.DateRangeTooLargeException;
import org.example.djajbladibackend.exception.ForbiddenRoleException;
import org.example.djajbladibackend.exception.InvalidDataException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.ApprovalStatus;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.DailyMortalityRecordRepository;
import org.example.djajbladibackend.repository.FeedingRecordRepository;
import org.example.djajbladibackend.repository.HealthRecordRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class SupervisionDashboardService {

    private final FeedingRecordRepository feedingRepository;
    private final DailyMortalityRecordRepository mortalityRepository;
    private final HealthRecordRepository healthRepository;
    private final UserRepository userRepository;

    @Value("${app.supervision.max-date-range-days:366}")
    private int maxDateRangeDays;

    public SupervisionDashboardService(FeedingRecordRepository feedingRepository,
                                        DailyMortalityRecordRepository mortalityRepository,
                                        HealthRecordRepository healthRepository,
                                        UserRepository userRepository) {
        this.feedingRepository = feedingRepository;
        this.mortalityRepository = mortalityRepository;
        this.healthRepository = healthRepository;
        this.userRepository = userRepository;
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
        if (java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) > maxDateRangeDays) {
            throw new DateRangeTooLargeException("Date range cannot exceed " + maxDateRangeDays + " days");
        }

        var feedingRecords = feedingRepository.findByFeedingDateBetweenWithRelations(startDate, endDate);
        var mortalityRecords = mortalityRepository.findByRecordDateBetweenWithRelations(startDate, endDate);
        var pendingAlerts = healthRepository.findPendingApprovalWithRelations();

        Map<String, List<Object>> byBatchAndDate = new HashMap<>();
        for (var f : feedingRecords) {
            String key = f.getBatch().getId() + "|" + f.getFeedingDate();
            byBatchAndDate.computeIfAbsent(key, k -> new ArrayList<>()).add(f);
        }
        for (var m : mortalityRecords) {
            String key = m.getBatch().getId() + "|" + m.getRecordDate();
            List<Object> list = byBatchAndDate.computeIfAbsent(key, k -> new ArrayList<>());
            Optional<Object> existing = list.stream().filter(o -> o instanceof org.example.djajbladibackend.models.DailyMortalityRecord).findFirst();
            if (existing.isEmpty()) {
                list.add(m);
            }
        }

        List<SupervisionDashboardResponse.BatchDailySummary> summaries = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (var f : feedingRecords) {
            String key = f.getBatch().getId() + "|" + f.getFeedingDate();
            if (seen.add(key)) {
                BigDecimal totalQty = feedingRecords.stream()
                        .filter(x -> x.getBatch().getId().equals(f.getBatch().getId()) && x.getFeedingDate().equals(f.getFeedingDate()))
                        .map(org.example.djajbladibackend.models.FeedingRecord::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                int mortality = mortalityRecords.stream()
                        .filter(x -> x.getBatch().getId().equals(f.getBatch().getId()) && x.getRecordDate().equals(f.getFeedingDate()))
                        .mapToInt(org.example.djajbladibackend.models.DailyMortalityRecord::getMortalityCount)
                        .sum();
                String recordedByName = f.getRecordedBy().getFullName();
                boolean abnormal = isAbnormalConsumption(f.getBatch().getChickenCount(), totalQty, mortality);

                summaries.add(SupervisionDashboardResponse.BatchDailySummary.builder()
                        .batchId(f.getBatch().getId())
                        .batchNumber(f.getBatch().getBatchNumber())
                        .date(f.getFeedingDate())
                        .totalQuantityEaten(totalQty.setScale(2, RoundingMode.HALF_UP))
                        .mortalityCount(mortality)
                        .recordedByName(recordedByName)
                        .abnormalConsumption(abnormal)
                        .build());
            }
        }
        for (var m : mortalityRecords) {
            String key = m.getBatch().getId() + "|" + m.getRecordDate();
            if (seen.add(key)) {
                BigDecimal totalQty = feedingRecords.stream()
                        .filter(x -> x.getBatch().getId().equals(m.getBatch().getId()) && x.getFeedingDate().equals(m.getRecordDate()))
                        .map(org.example.djajbladibackend.models.FeedingRecord::getQuantity)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                summaries.add(SupervisionDashboardResponse.BatchDailySummary.builder()
                        .batchId(m.getBatch().getId())
                        .batchNumber(m.getBatch().getBatchNumber())
                        .date(m.getRecordDate())
                        .totalQuantityEaten(totalQty.setScale(2, RoundingMode.HALF_UP))
                        .mortalityCount(m.getMortalityCount())
                        .recordedByName(m.getRecordedBy().getFullName())
                        .abnormalConsumption(isAbnormalConsumption(m.getBatch().getChickenCount(), totalQty, m.getMortalityCount()))
                        .build());
            }
        }
        summaries.sort(Comparator.comparing(SupervisionDashboardResponse.BatchDailySummary::getDate).reversed()
                .thenComparing(SupervisionDashboardResponse.BatchDailySummary::getBatchNumber));

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

        return SupervisionDashboardResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .batchSummaries(summaries)
                .pendingAlerts(alertSummaries)
                .build();
    }

    private boolean isAbnormalConsumption(Integer chickenCount, BigDecimal totalQuantity, int mortality) {
        if (chickenCount == null || chickenCount <= 0) return false;
        BigDecimal perChicken = totalQuantity.divide(BigDecimal.valueOf(chickenCount), 4, RoundingMode.HALF_UP);
        return perChicken.compareTo(BigDecimal.valueOf(0.5)) > 0 || mortality > chickenCount / 100;
    }
}
