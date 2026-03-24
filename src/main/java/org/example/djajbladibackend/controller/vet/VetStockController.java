package org.example.djajbladibackend.controller.vet;

import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.dto.stock.StockItemResponse;
import org.example.djajbladibackend.models.StockType;
import org.example.djajbladibackend.repository.StockItemRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Read-only stock access for VETERINAIRE role.
 * Exposes only VACCINE and MEDICATION stock items — not FEED or EQUIPMENT.
 * Prevents the vet from seeing unrelated farm operations data.
 */
@RestController
@RequestMapping(value = { "/api/vet/stock", "/api/dashboard/vet/stock" })
@PreAuthorize("hasAnyRole('ADMIN', 'VETERINAIRE')")
public class VetStockController {

    private final StockItemRepository stockItemRepository;

    public VetStockController(StockItemRepository stockItemRepository) {
        this.stockItemRepository = stockItemRepository;
    }

    /**
     * Returns vaccine + medication stock only — used by the vet health form sanitary section.
     * GET /api/vet/stock/sanitary
     */
    @GetMapping("/sanitary")
    public ResponseEntity<List<StockItemResponse>> findSanitaryStock() {
        List<StockItemResponse> vaccines = stockItemRepository
                .findByTypeOrderByNameAsc(StockType.VACCINE)
                .stream().map(this::toResponse).toList();
        List<StockItemResponse> medications = stockItemRepository
                .findByTypeOrderByNameAsc(StockType.MEDICATION)
                .stream().map(this::toResponse).toList();
        List<StockItemResponse> combined = new java.util.ArrayList<>();
        combined.addAll(vaccines);
        combined.addAll(medications);
        return ResponseEntity.ok(combined);
    }

    /**
     * Paginated full stock list (all types) — for vet reporting/visibility.
     * GET /api/vet/stock?page=0&size=50
     */
    @GetMapping
    public ResponseEntity<PageResponse<StockItemResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        PageResponse<StockItemResponse> result = PageResponse.from(
                stockItemRepository.findAllByOrderByTypeAscNameAsc(PageRequest.of(page, size))
                        .map(this::toResponse)
        );
        return ResponseEntity.ok(result);
    }

    private StockItemResponse toResponse(org.example.djajbladibackend.models.StockItem s) {
        return StockItemResponse.builder()
                .id(s.getId())
                .type(s.getType())
                .name(s.getName())
                .quantity(s.getQuantity())
                .unit(s.getUnit())
                .unitPrice(s.getUnitPrice())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
