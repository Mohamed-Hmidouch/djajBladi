package org.example.djajbladibackend.controller;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.building.BuildingRequest;
import org.example.djajbladibackend.dto.building.BuildingResponse;
import org.example.djajbladibackend.services.building.BuildingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = { "/api/admin/buildings", "/api/dashboard/admin/buildings" })
public class AdminBuildingController {

    private final BuildingService buildingService;

    public AdminBuildingController(BuildingService buildingService) {
        this.buildingService = buildingService;
    }

    @PostMapping
    public ResponseEntity<BuildingResponse> create(
            @Valid @RequestBody BuildingRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BuildingResponse created = buildingService.create(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<BuildingResponse>> findAll() {
        return ResponseEntity.ok(buildingService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BuildingResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(buildingService.findById(id));
    }
}
