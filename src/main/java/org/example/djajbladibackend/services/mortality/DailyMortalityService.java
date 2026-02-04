package org.example.djajbladibackend.services.mortality;

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
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.DailyMortalityRecordRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class DailyMortalityService {

    private final DailyMortalityRecordRepository mortalityRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;

    @Value("${app.supervision.max-date-range-days:366}")
    private int maxDateRangeDays;

    public DailyMortalityService(DailyMortalityRecordRepository mortalityRepository,
                                 BatchRepository batchRepository,
                                 UserRepository userRepository) {
        this.mortalityRepository = mortalityRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public DailyMortalityResponse record(DailyMortalityRequest req, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        if (user.getRole() != RoleEnum.Ouvrier && user.getRole() != RoleEnum.Admin) {
            throw new ForbiddenRoleException("Only Ouvrier or Admin can record daily mortality");
        }
        if (req.getRecordDate().isAfter(LocalDate.now())) {
            throw new InvalidDataException("Record date cannot be in the future");
        }
        if (req.getMortalityCount() == null || req.getMortalityCount() < 0) {
            throw new InvalidDataException("Mortality count must be >= 0");
        }
        Batch batch = batchRepository.findById(req.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + req.getBatchId()));
        if (batch.getStatus() != org.example.djajbladibackend.models.BatchStatus.Active) {
            throw new BatchNotActiveException("Mortality can only be recorded for active batches. Current status: " + batch.getStatus());
        }
        if (req.getMortalityCount() > batch.getChickenCount()) {
            throw new MortalityExceedsBatchSizeException("Mortality count (" + req.getMortalityCount() + ") cannot exceed batch size (" + batch.getChickenCount() + ")");
        }
        if (mortalityRepository.existsByBatchIdAndRecordDate(batch.getId(), req.getRecordDate())) {
            throw new DuplicateDailyMortalityException(
                    "Mortality already recorded for batch " + batch.getBatchNumber() + " on " + req.getRecordDate());
        }

        DailyMortalityRecord record = DailyMortalityRecord.builder()
                .batch(batch)
                .recordDate(req.getRecordDate())
                .mortalityCount(req.getMortalityCount())
                .notes(req.getNotes())
                .recordedBy(user)
                .build();
        DailyMortalityRecord saved = mortalityRepository.save(record);

        return toResponse(saved);
    }

    @Transactional
    public DailyMortalityResponse update(Long id, DailyMortalityRequest req, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        if (user.getRole() != RoleEnum.Ouvrier && user.getRole() != RoleEnum.Admin) {
            throw new ForbiddenRoleException("Only Ouvrier or Admin can update daily mortality");
        }
        DailyMortalityRecord record = mortalityRepository.findByIdWithRelations(id)
                .orElseThrow(() -> new ResourceNotFoundException("Daily mortality record not found: " + id));
        if (req.getRecordDate().isAfter(LocalDate.now())) {
            throw new InvalidDataException("Record date cannot be in the future");
        }
        if (req.getMortalityCount() == null || req.getMortalityCount() < 0) {
            throw new InvalidDataException("Mortality count must be >= 0");
        }
        if (req.getMortalityCount() > record.getBatch().getChickenCount()) {
            throw new MortalityExceedsBatchSizeException("Mortality count (" + req.getMortalityCount() + ") cannot exceed batch size (" + record.getBatch().getChickenCount() + ")");
        }

        record.setRecordDate(req.getRecordDate());
        record.setMortalityCount(req.getMortalityCount());
        record.setNotes(req.getNotes());
        DailyMortalityRecord saved = mortalityRepository.save(record);

        return toResponse(saved);
    }

    public List<DailyMortalityResponse> findByDateRange(LocalDate start, LocalDate end, Long batchId) {
        if (start.isAfter(end)) {
            throw new InvalidDataException("Start date must be before or equal to end date");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(start, end) > maxDateRangeDays) {
            throw new DateRangeTooLargeException("Date range cannot exceed " + maxDateRangeDays + " days");
        }
        List<DailyMortalityRecord> records;
        if (batchId != null) {
            records = mortalityRepository.findByBatchIdAndRecordDateBetweenWithRelations(batchId, start, end);
        } else {
            records = mortalityRepository.findByRecordDateBetweenWithRelations(start, end);
        }
        return records.stream().map(this::toResponse).toList();
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
