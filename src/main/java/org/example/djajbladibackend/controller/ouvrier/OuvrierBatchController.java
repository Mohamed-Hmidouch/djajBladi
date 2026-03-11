package org.example.djajbladibackend.controller.ouvrier;

import org.example.djajbladibackend.dto.batch.BatchResponse;
import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.services.batch.BatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only batch endpoint accessible by Ouvrier and Admin.
 * Ouvrier needs batch list to log feeding and mortality.
 */
@RestController
@RequestMapping("/api/ouvrier/batches")
@PreAuthorize("hasAnyRole('ADMIN', 'OUVRIER')")
public class OuvrierBatchController {

    private final BatchService batchService;

    public OuvrierBatchController(BatchService batchService) {
        this.batchService = batchService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<BatchResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(batchService.findAll(page, size));
    }
}
