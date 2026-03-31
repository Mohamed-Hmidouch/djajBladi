package org.example.djajbladibackend.services.mortality;

import lombok.extern.slf4j.Slf4j;
import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.dto.mortality.DailyMortalityRequest;
import org.example.djajbladibackend.dto.mortality.DailyMortalityResponse;
import org.example.djajbladibackend.exception.BatchNotActiveException;
import org.example.djajbladibackend.exception.DateRangeTooLargeException;
import org.example.djajbladibackend.exception.DuplicateDailyMortalityException;
import org.example.djajbladibackend.exception.ForbiddenRoleException;
import org.example.djajbladibackend.exception.InvalidDataException;
import org.example.djajbladibackend.exception.MortalityExceedsBatchSizeException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.DailyMortalityRecord;
import org.example.djajbladibackend.models.HealthRecord;
import org.example.djajbladibackend.models.MortalitySource;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.DailyMortalityRecordRepository;
import org.example.djajbladibackend.repository.HealthRecordRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
public class DailyMortalityService {

    private final DailyMortalityRecordRepository mortalityRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;
    private final HealthRecordRepository healthRecordRepository;

    @Value("${app.supervision.max-date-range-days:366}")
    private int maxDateRangeDays;

    public DailyMortalityService(DailyMortalityRecordRepository mortalityRepository,
                                 BatchRepository batchRepository,
                                 UserRepository userRepository,
                                 HealthRecordRepository healthRecordRepository) {
        this.mortalityRepository = mortalityRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
        this.healthRecordRepository = healthRecordRepository;
    }

    @Transactional
    public DailyMortalityResponse record(DailyMortalityRequest req, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + userEmail));
        if (user.getRole() != RoleEnum.Ouvrier && user.getRole() != RoleEnum.Admin) {
            throw new ForbiddenRoleException("Seul un ouvrier ou un administrateur peut enregistrer la mortalité.");
        }
        if (req.getRecordDate().isAfter(LocalDate.now())) {
            throw new InvalidDataException("La date d'enregistrement ne peut pas être dans le futur.");
        }
        if (req.getMortalityCount() == null || req.getMortalityCount() < 0) {
            throw new InvalidDataException("Le nombre de mortalités doit être supérieur ou égal à 0.");
        }
        Batch batch = batchRepository.findById(req.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Lot introuvable : " + req.getBatchId()));
        if (batch.getStatus() != org.example.djajbladibackend.models.BatchStatus.Active) {
            throw new BatchNotActiveException("La mortalité ne peut être enregistrée que pour les lots actifs. Statut actuel : " + batch.getStatus());
        }
        if (req.getMortalityCount() > batch.getChickenCount()) {
            throw new MortalityExceedsBatchSizeException("Le nombre de mortalités (" + req.getMortalityCount() + ") ne peut pas dépasser la taille du lot (" + batch.getChickenCount() + ").");
        }
        if (mortalityRepository.existsByBatchIdAndRecordDate(batch.getId(), req.getRecordDate())) {
            throw new DuplicateDailyMortalityException(
                    "La mortalité est déjà enregistrée pour le lot " + batch.getBatchNumber() + " à la date du " + req.getRecordDate() + ".");
        }

        DailyMortalityRecord record = DailyMortalityRecord.builder()
                .batch(batch)
                .recordDate(req.getRecordDate())
                .mortalityCount(req.getMortalityCount())
                .notes(req.getNotes())
                .recordedBy(user)
                .source(MortalitySource.WORKER_REPORT)
                .build();
        DailyMortalityRecord saved = mortalityRepository.save(record);

        return toResponse(saved);
    }

    @Transactional
    public DailyMortalityResponse update(Long id, DailyMortalityRequest req, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + userEmail));
        if (user.getRole() != RoleEnum.Ouvrier && user.getRole() != RoleEnum.Admin) {
            throw new ForbiddenRoleException("Seul un ouvrier ou un administrateur peut modifier la mortalité.");
        }
        DailyMortalityRecord record = mortalityRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enregistrement de mortalité introuvable : " + id));
        if (req.getRecordDate().isAfter(LocalDate.now())) {
            throw new InvalidDataException("La date d'enregistrement ne peut pas être dans le futur.");
        }
        if (req.getMortalityCount() == null || req.getMortalityCount() < 0) {
            throw new InvalidDataException("Le nombre de mortalités doit être supérieur ou égal à 0.");
        }
        if (req.getMortalityCount() > record.getBatch().getChickenCount()) {
            throw new MortalityExceedsBatchSizeException("Le nombre de mortalités (" + req.getMortalityCount() + ") ne peut pas dépasser la taille du lot (" + record.getBatch().getChickenCount() + ").");
        }

        record.setRecordDate(req.getRecordDate());
        record.setMortalityCount(req.getMortalityCount());
        record.setNotes(req.getNotes());
        DailyMortalityRecord saved = mortalityRepository.save(record);

        return toResponse(saved);
    }

    public List<DailyMortalityResponse> findByDateRange(LocalDate start, LocalDate end, Long batchId) {
        if (start.isAfter(end)) {
            throw new InvalidDataException("La date de début doit être antérieure ou égale à la date de fin.");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(start, end) > maxDateRangeDays) {
            throw new DateRangeTooLargeException("La plage de dates ne peut pas dépasser " + maxDateRangeDays + " jours.");
        }
        List<DailyMortalityRecord> records;
        if (batchId != null) {
            records = mortalityRepository.findByBatchIdAndRecordDateBetweenWithRelations(batchId, start, end);
        } else {
            records = mortalityRepository.findByRecordDateBetweenWithRelations(start, end);
        }
        return records.stream().map(this::toResponse).toList();
    }

    public PageResponse<DailyMortalityResponse> findByDateRangePaged(LocalDate start, LocalDate end, Long batchId, int page, int size) {
        if (start.isAfter(end)) {
            throw new InvalidDataException("La date de début doit être antérieure ou égale à la date de fin.");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(start, end) > maxDateRangeDays) {
            throw new DateRangeTooLargeException("La plage de dates ne peut pas dépasser " + maxDateRangeDays + " jours.");
        }
        var pageable = PageRequest.of(page, size);
        if (batchId != null) {
            return PageResponse.from(
                    mortalityRepository.findByBatchIdAndDateRangePageable(batchId, start, end, pageable)
                            .map(this::toResponse)
            );
        }
        return PageResponse.from(
                mortalityRepository.findByDateRangePageable(start, end, pageable)
                        .map(this::toResponse)
        );
    }

    /**
     * Called by HealthRecordService when a veterinarian records mortality in a health examination.
     * Atomically decrements batch current_count and creates a VETERINARIAN_EXAMINATION mortality record.
     * Requirements: 3.1, 3.2, 3.3, 3.5, 3.6, 9.2
     */
    @Transactional
    public void decrementStock(Long batchId, Integer mortalityCount, LocalDate recordDate, Long healthRecordId) {
        if (mortalityCount == null || mortalityCount <= 0) {
            throw new InvalidDataException("Le nombre de mortalités doit être supérieur à 0.");
        }
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Lot introuvable : " + batchId));

        int affected = batchRepository.decrementCurrentCount(batchId, mortalityCount);
        if (affected == 0) {
            throw new MortalityExceedsBatchSizeException(
                    "Impossible de décrémenter le lot " + batchId + " de " + mortalityCount +
                    " : l'effectif actuel est insuffisant.");
        }

        User systemUser = batch.getAssignedTo();

        HealthRecord healthRecord = null;
        if (healthRecordId != null) {
            healthRecord = healthRecordRepository.findById(healthRecordId).orElse(null);
        }

        DailyMortalityRecord mortalityRecord = DailyMortalityRecord.builder()
                .batch(batch)
                .recordDate(recordDate)
                .mortalityCount(mortalityCount)
                .recordedBy(systemUser)
                .source(MortalitySource.VETERINARIAN_EXAMINATION)
                .healthRecord(healthRecord)
                .build();
        mortalityRepository.save(mortalityRecord);

        log.info("Veterinarian mortality sync: batchId={}, count={}, date={}, healthRecordId={}",
                batchId, mortalityCount, recordDate, healthRecordId);
    }

    /**
     * Finds mortality records filtered by source and date range.
     * Requirements: 9.4
     */
    public List<DailyMortalityResponse> findBySource(MortalitySource source, LocalDate startDate, LocalDate endDate) {
        return mortalityRepository.findBySourceAndDateBetween(source, startDate, endDate)
                .stream().map(this::toResponse).toList();
    }

    private DailyMortalityResponse toResponse(DailyMortalityRecord r) {
        return DailyMortalityResponse.builder()
                .id(r.getId())
                .batchId(r.getBatch().getId())
                .batchNumber(r.getBatch().getBatchNumber())
                .recordDate(r.getRecordDate())
                .mortalityCount(r.getMortalityCount())
                .notes(r.getNotes())
                .recordedById(r.getRecordedBy().getId())
                .recordedByName(r.getRecordedBy().getFullName())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
