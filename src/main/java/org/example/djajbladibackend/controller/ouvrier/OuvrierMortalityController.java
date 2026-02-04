package org.example.djajbladibackend.controller.ouvrier;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.mortality.DailyMortalityRequest;
import org.example.djajbladibackend.dto.mortality.DailyMortalityResponse;
import org.example.djajbladibackend.services.mortality.DailyMortalityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = { "/api/ouvrier/mortality", "/api/dashboard/ouvrier/mortality" })
public class OuvrierMortalityController {

    private final DailyMortalityService mortalityService;

    public OuvrierMortalityController(DailyMortalityService mortalityService) {
        this.mortalityService = mortalityService;
    }

    @PostMapping
    public ResponseEntity<DailyMortalityResponse> record(
            @Valid @RequestBody DailyMortalityRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mortalityService.record(request, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DailyMortalityResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DailyMortalityRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(mortalityService.update(id, request, userDetails.getUsername()));
    }

    @GetMapping
    public ResponseEntity<List<DailyMortalityResponse>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long batchId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(mortalityService.findByDateRange(startDate, endDate, batchId));
    }
}
