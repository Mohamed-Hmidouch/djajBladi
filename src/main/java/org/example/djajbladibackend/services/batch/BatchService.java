package org.example.djajbladibackend.services.batch;

import org.example.djajbladibackend.dto.batch.BatchCreateRequest;
import org.example.djajbladibackend.dto.batch.BatchResponse;
import org.example.djajbladibackend.exception.DuplicateBatchNumberException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.BatchStatus;
import org.example.djajbladibackend.models.Building;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.BuildingRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BatchService {

    private final BatchRepository batchRepository;
    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    public BatchService(BatchRepository batchRepository, BuildingRepository buildingRepository, UserRepository userRepository) {
        this.batchRepository = batchRepository;
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BatchResponse create(BatchCreateRequest req, String adminEmail) {
        if (batchRepository.existsByBatchNumber(req.getBatchNumber())) {
            throw new DuplicateBatchNumberException("Batch number already exists: " + req.getBatchNumber());
        }
        var createdBy = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + adminEmail));
        Building building = null;
        if (req.getBuildingId() != null) {
            building = buildingRepository.findById(req.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building", "id", req.getBuildingId()));
        }
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
                .build();
        Batch saved = batchRepository.save(b);
        return toResponse(saved);
    }

    public BatchResponse findById(Long id) {
        Batch b = batchRepository.findByIdWithCreatedByAndBuilding(id)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", id));
        return toResponse(b);
    }

    private BatchResponse toResponse(Batch b) {
        return BatchResponse.builder()
                .id(b.getId())
                .batchNumber(b.getBatchNumber())
                .strain(b.getStrain())
                .chickenCount(b.getChickenCount())
                .arrivalDate(b.getArrivalDate())
                .purchasePrice(b.getPurchasePrice())
                .buildingId(b.getBuilding() != null ? b.getBuilding().getId() : null)
                .buildingName(b.getBuilding() != null ? b.getBuilding().getName() : null)
                .status(b.getStatus())
                .notes(b.getNotes())
                .createdById(b.getCreatedBy() != null ? b.getCreatedBy().getId() : null)
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
