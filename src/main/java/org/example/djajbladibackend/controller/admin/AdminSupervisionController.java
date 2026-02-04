package org.example.djajbladibackend.controller.admin;

import org.example.djajbladibackend.dto.dashboard.SupervisionDashboardResponse;
import org.example.djajbladibackend.dto.health.HealthRecordResponse;
import org.example.djajbladibackend.services.dashboard.SupervisionDashboardService;
import org.example.djajbladibackend.services.health.HealthRecordService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * ✅ Security Best Practice: @PreAuthorize au niveau classe pour ADMIN
 */
@RestController
@RequestMapping(value = { "/api/admin/dashboard", "/api/dashboard/admin/dashboard" })
@PreAuthorize("hasRole('ADMIN')")
public class AdminSupervisionController {

    private final SupervisionDashboardService dashboardService;
    private final HealthRecordService healthRecordService;

    public AdminSupervisionController(SupervisionDashboardService dashboardService,
                                       HealthRecordService healthRecordService) {
        this.dashboardService = dashboardService;
        this.healthRecordService = healthRecordService;
    }

    @GetMapping("/supervision")
    public ResponseEntity<SupervisionDashboardResponse> getSupervision(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LocalDate end = endDate != null ? endDate : LocalDate.now();
        LocalDate start = startDate != null ? startDate : end.minusDays(7);
        return ResponseEntity.ok(dashboardService.getDashboard(start, end, userDetails.getUsername()));
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<HealthRecordResponse>> getPendingAlerts(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(healthRecordService.findPendingApproval());
    }

    @PostMapping("/health-records/{id}/approve")
    public ResponseEntity<HealthRecordResponse> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(healthRecordService.approve(id, userDetails.getUsername()));
    }

    @PostMapping("/health-records/{id}/reject")
    public ResponseEntity<HealthRecordResponse> reject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(healthRecordService.reject(id, userDetails.getUsername()));
    }
}


