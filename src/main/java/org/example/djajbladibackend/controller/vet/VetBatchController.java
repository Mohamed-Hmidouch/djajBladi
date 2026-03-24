package org.example.djajbladibackend.controller.vet;

import org.example.djajbladibackend.dto.batch.BatchResponse;
import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.models.BatchStatus;
import org.example.djajbladibackend.repository.BatchRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only batch access for VETERINAIRE role.
 * Exposes only the minimum data needed for health record creation and vaccination tracking.
 */
@RestController
@RequestMapping(value = { "/api/vet/batches", "/api/dashboard/vet/batches" })
@PreAuthorize("hasAnyRole('ADMIN', 'VETERINAIRE')")
public class VetBatchController {

    private final BatchRepository batchRepository;

    public VetBatchController(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    /**
     * Returns all active batches — used by the vet health form dropdown and dashboard.
     * JOIN FETCH prevents N+1 on createdBy / assignedTo / building relations.
     * GET /api/vet/batches/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<BatchResponse>> findActive() {
        List<BatchResponse> result = batchRepository
                .findByStatusWithCreatedBy(BatchStatus.Active)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(result);
    }

    /**
     * Paginated list of all batches — used by vet history/reporting views.
     * GET /api/vet/batches?page=0&size=20
     */
    @GetMapping
    public ResponseEntity<PageResponse<BatchResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<BatchResponse> result = PageResponse.from(
                batchRepository.findAllWithRelationsPageable(PageRequest.of(page, size))
                        .map(this::toResponse)
        );
        return ResponseEntity.ok(result);
    }

    private BatchResponse toResponse(org.example.djajbladibackend.models.Batch b) {
        return BatchResponse.builder()
                .id(b.getId())
                .batchNumber(b.getBatchNumber())
                .strain(b.getStrain())
                .chickenCount(b.getChickenCount())
                .currentCount(b.getCurrentCount())
                .arrivalDate(b.getArrivalDate())
                .status(b.getStatus())
                .buildingId(b.getBuilding() != null ? b.getBuilding().getId() : null)
                .buildingName(b.getBuilding() != null ? b.getBuilding().getName() : null)
                .assignedToId(b.getAssignedTo() != null ? b.getAssignedTo().getId() : null)
                .assignedToName(b.getAssignedTo() != null ? b.getAssignedTo().getFullName() : null)
                .notes(b.getNotes())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
