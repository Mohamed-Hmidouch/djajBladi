package org.example.djajbladibackend.controller.admin;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.batch.BatchCreateRequest;
import org.example.djajbladibackend.dto.batch.BatchResponse;
import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.dto.financial.BatchCostBreakdownResponse;
import org.example.djajbladibackend.services.batch.BatchService;
import org.example.djajbladibackend.services.financial.BatchCostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * ✅ Security Best Practice: @PreAuthorize au niveau classe pour ADMIN
 */
@RestController
@RequestMapping(value = { "/api/admin/batches", "/api/dashboard/admin/batches" })
@PreAuthorize("hasRole('ADMIN')")
public class AdminBatchController {

    private final BatchService batchService;
    private final BatchCostService batchCostService;

    public AdminBatchController(BatchService batchService, BatchCostService batchCostService) {
        this.batchService = batchService;
        this.batchCostService = batchCostService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<BatchResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(batchService.findAll(page, size));
    }

    @PostMapping
    public ResponseEntity<BatchResponse> create(
            @Valid @RequestBody BatchCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BatchResponse created = batchService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Calcul du cout de revient d'un lot.
     *
     * GET /api/admin/batches/{id}/cost
     *     ?fixedCharges=500.00  (optionnel, override la valeur configuree)
     */
    @GetMapping("/{id}/cost")
    public ResponseEntity<BatchCostBreakdownResponse> getCostBreakdown(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal fixedCharges) {
        return ResponseEntity.ok(batchCostService.calculateCost(id, fixedCharges));
    }
}
