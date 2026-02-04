package org.example.djajbladibackend.controller.vet;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.health.HealthRecordCreateRequest;
import org.example.djajbladibackend.dto.health.HealthRecordResponse;
import org.example.djajbladibackend.services.health.HealthRecordService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * ✅ Security Best Practice: @PreAuthorize pour ADMIN et VETERINAIRE
 */
@RestController
@RequestMapping(value = { "/api/vet/health-records", "/api/dashboard/vet/health-records" })
@PreAuthorize("hasAnyRole('ADMIN', 'VETERINAIRE')")
public class VetHealthController {

    private final HealthRecordService healthRecordService;

    public VetHealthController(HealthRecordService healthRecordService) {
        this.healthRecordService = healthRecordService;
    }

    @PostMapping
    public ResponseEntity<HealthRecordResponse> create(
            @Valid @RequestBody HealthRecordCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(healthRecordService.create(request, userDetails.getUsername()));
    }
}
