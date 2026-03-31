package org.example.djajbladibackend.services.batch;

import lombok.extern.slf4j.Slf4j;
import org.example.djajbladibackend.dto.batch.BatchCreateRequest;
import org.example.djajbladibackend.dto.batch.BatchResponse;
import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.exception.DuplicateBatchNumberException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.exception.WithdrawalPeriodActiveException;
import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.BatchStatus;
import org.example.djajbladibackend.models.Building;
import org.example.djajbladibackend.models.HealthRecord;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.BuildingRepository;
import org.example.djajbladibackend.repository.HealthRecordRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
@Slf4j
public class BatchService {

    private final BatchRepository batchRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;
    private final HealthRecordRepository healthRecordRepository;

    public BatchService(BatchRepository batchRepository,
                        BuildingRepository buildingRepository,
                        UserRepository userRepository,
                        HealthRecordRepository healthRecordRepository) {
        this.batchRepository = batchRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
        this.healthRecordRepository = healthRecordRepository;
    }

    @Transactional
    public BatchResponse create(BatchCreateRequest req, String adminEmail) {
        if (batchRepository.existsByBatchNumber(req.getBatchNumber())) {
            throw new DuplicateBatchNumberException("Le numéro de lot existe déjà : " + req.getBatchNumber());
        }
        var createdBy = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable avec l'email : " + adminEmail));
        Building building = null;
        if (req.getBuildingId() != null) {
            building = buildingRepository.findById(req.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building", "id", req.getBuildingId()));
        }
        var assignedTo = userRepository.findById(req.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", req.getAssignedToId()));
        Batch b = Batch.builder()
                .batchNumber(req.getBatchNumber())
                .strain(req.getStrain())
                .chickenCount(req.getChickenCount())
                .arrivalDate(req.getArrivalDate())
                .purchasePrice(req.getPurchasePrice())
                .building(building)
                .status(BatchStatus.Active)
                .notes(req.getNotes())
                .createdBy(createdBy)
                .assignedTo(assignedTo)
                .build();
        Batch saved = batchRepository.save(b);
        return toResponse(saved);
    }

    @Transactional
    public BatchResponse update(Long id, org.example.djajbladibackend.dto.batch.BatchUpdateRequest req) {
        Batch b = batchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", id));

        if (!b.getBatchNumber().equals(req.getBatchNumber()) && batchRepository.existsByBatchNumber(req.getBatchNumber())) {
            throw new DuplicateBatchNumberException("Batch number already exists: " + req.getBatchNumber());
        }

        Building building = null;
        if (req.getBuildingId() != null) {
            building = buildingRepository.findById(req.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building", "id", req.getBuildingId()));
        }

        var assignedTo = userRepository.findById(req.getAssignedToId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", req.getAssignedToId()));

        if (req.getStatus() != null && req.getStatus() != b.getStatus()) {
            validateStatusTransition(id, req.getStatus());
        }

        b.setBatchNumber(req.getBatchNumber());
        b.setStrain(req.getStrain());
        b.setChickenCount(req.getChickenCount());
        b.setArrivalDate(req.getArrivalDate());
        b.setPurchasePrice(req.getPurchasePrice());
        b.setBuilding(building);
        b.setNotes(req.getNotes());
        b.setAssignedTo(assignedTo);
        b.setStatus(req.getStatus());

        Batch saved = batchRepository.save(b);
        return toResponse(saved);
    }

    public PageResponse<BatchResponse> findAll(int page, int size) {
        return PageResponse.from(
                batchRepository.findAllWithRelationsPageable(PageRequest.of(page, size))
                        .map(this::toResponse)
        );
    }

    public PageResponse<BatchResponse> findByAssignedUser(Long userId, int page, int size) {
        return PageResponse.from(
                batchRepository.findByAssignedToIdWithRelationsPageable(userId, PageRequest.of(page, size))
                        .map(this::toResponse)
        );
    }

    public PageResponse<BatchResponse> findByAssignedUserEmail(String email, int page, int size) {
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + email));
        return findByAssignedUser(user.getId(), page, size);
    }

    public BatchResponse findById(Long id) {
        Batch b = batchRepository.findByIdWithCreatedByAndBuilding(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", id));
        return toResponse(b);
    }

    /**
     * Returns true if the batch has no active withdrawal periods, allowing sale.
     * Requirements: 1.2, 1.5
     */
    public boolean isBatchSellable(Long batchId) {
        List<HealthRecord> activeWithdrawals = healthRecordRepository.findActiveWithdrawalPeriods(batchId);
        return activeWithdrawals.isEmpty();
    }

    /**
     * Returns the latest withdrawal expiration date for the batch, if any.
     * Requirements: 1.4, 7.4
     */
    public Optional<LocalDate> getWithdrawalExpirationDate(Long batchId) {
        LocalDate expiration = healthRecordRepository.findLatestWithdrawalExpiration(batchId);
        return Optional.ofNullable(expiration);
    }

    /**
     * Validates that a batch can transition to the requested status.
     * Blocks SOLD and READY_FOR_SALE when an active withdrawal period exists.
     * Requirements: 1.3, 7.1, 7.2, 7.3
     */
    public void validateStatusTransition(Long batchId, BatchStatus newStatus) {
        if (newStatus == BatchStatus.SOLD || newStatus == BatchStatus.READY_FOR_SALE) {
            if (!isBatchSellable(batchId)) {
                LocalDate expirationDate = getWithdrawalExpirationDate(batchId)
                        .orElse(LocalDate.now().plusDays(1));
                log.warn("Batch {} sale blocked: active withdrawal period until {}", batchId, expirationDate);
                throw new WithdrawalPeriodActiveException(batchId, expirationDate);
            }
        }
    }

    private BatchResponse toResponse(Batch b) {
        return BatchResponse.builder()
                .id(b.getId())
                .batchNumber(b.getBatchNumber())
                .strain(b.getStrain())
                .chickenCount(b.getChickenCount())
                .currentCount(b.getCurrentCount())
                .arrivalDate(b.getArrivalDate())
                .purchasePrice(b.getPurchasePrice())
                .buildingId(b.getBuilding() != null ? b.getBuilding().getId() : null)
                .buildingName(b.getBuilding() != null ? b.getBuilding().getName() : null)
                .status(b.getStatus())
                .notes(b.getNotes())
                .createdById(b.getCreatedBy() != null ? b.getCreatedBy().getId() : null)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .assignedToId(b.getAssignedTo() != null ? b.getAssignedTo().getId() : null)
                .assignedToName(b.getAssignedTo() != null ? b.getAssignedTo().getFullName() : null)
                .build();
    }
}
