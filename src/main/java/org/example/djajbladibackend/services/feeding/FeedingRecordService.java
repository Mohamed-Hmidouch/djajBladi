package org.example.djajbladibackend.services.feeding;

import org.example.djajbladibackend.dto.feeding.FeedingRecordRequest;
import org.example.djajbladibackend.dto.feeding.FeedingRecordResponse;
import org.example.djajbladibackend.exception.BatchNotActiveException;
import org.example.djajbladibackend.exception.DateRangeTooLargeException;
import org.example.djajbladibackend.exception.ForbiddenRoleException;
import org.example.djajbladibackend.exception.InvalidDataException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.FeedingRecord;
import org.example.djajbladibackend.models.User;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.FeedingRecordRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class FeedingRecordService {

    private final FeedingRecordRepository feedingRepository;
    private final BatchRepository batchRepository;
    private final UserRepository userRepository;

    @Value("${app.supervision.max-date-range-days:366}")
    private int maxDateRangeDays;

    public FeedingRecordService(FeedingRecordRepository feedingRepository,
                                BatchRepository batchRepository,
                                UserRepository userRepository) {
        this.feedingRepository = feedingRepository;
        this.batchRepository = batchRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public FeedingRecordResponse create(FeedingRecordRequest req, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userEmail));
        if (user.getRole() != RoleEnum.Ouvrier && user.getRole() != RoleEnum.Admin) {
            throw new ForbiddenRoleException("Only Ouvrier or Admin can create feeding records");
        }
        if (req.getFeedingDate().isAfter(LocalDate.now())) {
            throw new InvalidDataException("Feeding date cannot be in the future");
        }
        if (req.getFeedType() == null || req.getFeedType().trim().isEmpty()) {
            throw new InvalidDataException("Feed type is required and cannot be blank");
        }
        if (req.getQuantity() == null || req.getQuantity().signum() <= 0) {
            throw new InvalidDataException("Quantity must be greater than 0");
        }
        Batch batch = batchRepository.findById(req.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + req.getBatchId()));
        if (batch.getStatus() != org.example.djajbladibackend.models.BatchStatus.Active) {
            throw new BatchNotActiveException("Feeding can only be recorded for active batches. Current status: " + batch.getStatus());
        }

        FeedingRecord record = FeedingRecord.builder()
                .batch(batch)
                .feedType(req.getFeedType().trim())
                .quantity(req.getQuantity())
                .feedingDate(req.getFeedingDate())
                .notes(req.getNotes() != null ? req.getNotes().trim() : null)
                .recordedBy(user)
                .build();
        FeedingRecord saved = feedingRepository.save(record);

        return toResponse(saved);
    }

    public List<FeedingRecordResponse> findByDateRange(LocalDate start, LocalDate end, Long batchId) {
        if (start.isAfter(end)) {
            throw new InvalidDataException("Start date must be before or equal to end date");
        }
        if (java.time.temporal.ChronoUnit.DAYS.between(start, end) > maxDateRangeDays) {
            throw new DateRangeTooLargeException("Date range cannot exceed " + maxDateRangeDays + " days");
        }
        List<FeedingRecord> records;
        if (batchId != null) {
            records = feedingRepository.findByBatchIdAndDateRangeWithRelations(batchId, start, end);
        } else {
            records = feedingRepository.findByFeedingDateBetweenWithRelations(start, end);
        }
        return records.stream().map(this::toResponse).toList();
    }

    private FeedingRecordResponse toResponse(FeedingRecord r) {
        return FeedingRecordResponse.builder()
                .id(r.getId())
                .batchId(r.getBatch().getId())
                .batchNumber(r.getBatch().getBatchNumber())
                .feedType(r.getFeedType())
                .quantity(r.getQuantity())
                .feedingDate(r.getFeedingDate())
                .notes(r.getNotes())
                .recordedById(r.getRecordedBy().getId())
                .recordedByName(r.getRecordedBy().getFullName())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
