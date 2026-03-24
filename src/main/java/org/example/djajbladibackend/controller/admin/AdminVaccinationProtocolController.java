package org.example.djajbladibackend.controller.admin;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.vaccination.VaccinationProtocolRequest;
import org.example.djajbladibackend.dto.vaccination.VaccinationProtocolResponse;
import org.example.djajbladibackend.services.vaccination.VaccinationProtocolService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/vaccination-protocols")
@PreAuthorize("hasRole('ADMIN')")
public class AdminVaccinationProtocolController {

    private final VaccinationProtocolService protocolService;

    public AdminVaccinationProtocolController(VaccinationProtocolService protocolService) {
        this.protocolService = protocolService;
    }

    @PostMapping
    public ResponseEntity<VaccinationProtocolResponse> create(
            @Valid @RequestBody VaccinationProtocolRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        VaccinationProtocolResponse created = protocolService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VaccinationProtocolResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody VaccinationProtocolRequest request) {
        return ResponseEntity.ok(protocolService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        protocolService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-strain/{strain}")
    public ResponseEntity<List<VaccinationProtocolResponse>> findByStrain(@PathVariable String strain) {
        return ResponseEntity.ok(protocolService.findByStrain(strain));
    }
}
