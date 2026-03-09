package org.example.djajbladibackend.controller.ouvrier;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.dto.mortality.DailyMortalityRequest;
import org.example.djajbladibackend.dto.mortality.DailyMortalityResponse;
import org.example.djajbladibackend.services.mortality.DailyMortalityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * ✅ Security Best Practice: @PreAuthorize pour ADMIN et OUVRIER
 */
@RestController
@RequestMapping(value = { "/api/ouvrier/mortality", "/api/dashboard/ouvrier/mortality" })
@PreAuthorize("hasAnyRole('ADMIN', 'OUVRIER')")
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
    public ResponseEntity<PageResponse<DailyMortalityResponse>> list(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long batchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(mortalityService.findByDateRangePaged(startDate, endDate, batchId, page, size));
    }
}
