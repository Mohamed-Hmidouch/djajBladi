package org.example.djajbladibackend.controller.admin;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.stock.StockItemCreateRequest;
import org.example.djajbladibackend.dto.stock.StockItemResponse;
import org.example.djajbladibackend.services.stock.StockService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = { "/api/admin/stock", "/api/dashboard/admin/stock" })
public class AdminStockController {

    private final StockService stockService;

    public AdminStockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping
    public ResponseEntity<StockItemResponse> add(
            @Valid @RequestBody StockItemCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        StockItemResponse created = stockService.add(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<StockItemResponse>> findAll() {
        return ResponseEntity.ok(stockService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockItemResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(stockService.findById(id));
    }
}
