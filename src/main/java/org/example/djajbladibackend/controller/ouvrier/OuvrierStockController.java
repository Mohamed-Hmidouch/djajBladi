package org.example.djajbladibackend.controller.ouvrier;

import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.dto.stock.StockItemResponse;
import org.example.djajbladibackend.services.stock.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only stock endpoint accessible by Ouvrier and Admin.
 * Ouvrier needs stock list to select feed items when logging feeding records.
 */
@RestController
@RequestMapping("/api/ouvrier/stock")
@PreAuthorize("hasAnyRole('ADMIN', 'OUVRIER')")
public class OuvrierStockController {

    private final StockService stockService;

    public OuvrierStockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<StockItemResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(stockService.findAll(page, size));
    }
}
