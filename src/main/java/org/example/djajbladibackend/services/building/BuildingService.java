package org.example.djajbladibackend.services.building;

import org.example.djajbladibackend.dto.building.BuildingRequest;
import org.example.djajbladibackend.dto.building.BuildingResponse;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.Building;
import org.example.djajbladibackend.repository.BuildingRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final UserRepository userRepository;

    public BuildingService(BuildingRepository buildingRepository, UserRepository userRepository) {
        this.buildingRepository = buildingRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BuildingResponse create(BuildingRequest req, String adminEmail) {
        var createdBy = userRepository.findByEmail(adminEmail).orElse(null);
        Building b = Building.builder()
                .name(req.getName())
                .maxCapacity(req.getMaxCapacity())
                .imageUrl(req.getImageUrl())
                .createdBy(createdBy)
                .build();
        Building saved = buildingRepository.save(b);
        return toResponse(saved);
    }

    public BuildingResponse findById(Long id) {
        Building b = buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", "id", id));
        return toResponse(b);
    }

    public List<BuildingResponse> findAll() {
        return buildingRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    private BuildingResponse toResponse(Building b) {
        return BuildingResponse.builder()
                .id(b.getId())
                .name(b.getName())
                .maxCapacity(b.getMaxCapacity())
                .imageUrl(b.getImageUrl())
                .createdAt(b.getCreatedAt())
                .updatedAt(b.getUpdatedAt())
                .build();
    }
}
