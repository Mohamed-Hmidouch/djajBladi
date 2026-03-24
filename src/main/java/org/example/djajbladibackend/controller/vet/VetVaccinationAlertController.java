package org.example.djajbladibackend.controller.vet;

import org.example.djajbladibackend.dto.vaccination.VaccinationAlertResponse;
import org.example.djajbladibackend.dto.vaccination.VaccinationScheduleResponse;
import org.example.djajbladibackend.services.vaccination.VaccinationProtocolService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = {"/api/vet/vaccination-alerts", "/api/admin/vaccination-alerts"})
@PreAuthorize("hasAnyRole('ADMIN', 'VETERINAIRE')")
public class VetVaccinationAlertController {

    private final VaccinationProtocolService protocolService;

    public VetVaccinationAlertController(VaccinationProtocolService protocolService) {
        this.protocolService = protocolService;
    }

    /**
     * Returns pending vaccination alerts for active batches (due within 7 days and not yet done).
     * GET /api/vet/vaccination-alerts
     */
    @GetMapping
    public ResponseEntity<List<VaccinationAlertResponse>> getAlertsForToday() {
        return ResponseEntity.ok(protocolService.getAlertsForToday());
    }

    /**
     * Returns overdue vaccination alerts for active batches.
     * GET /api/vet/vaccination-alerts/overdue
     */
    @GetMapping("/overdue")
    public ResponseEntity<List<VaccinationAlertResponse>> getOverdueAlerts() {
        return ResponseEntity.ok(protocolService.getOverdueAlerts());
    }

    /**
     * Returns the full vaccination schedule for a specific batch.
     * GET /api/vet/vaccination-alerts/batch/{batchId}/schedule
     */
    @GetMapping("/batch/{batchId}/schedule")
    public ResponseEntity<List<VaccinationScheduleResponse>> getScheduleForBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(protocolService.getScheduleForBatch(batchId));
    }
}
