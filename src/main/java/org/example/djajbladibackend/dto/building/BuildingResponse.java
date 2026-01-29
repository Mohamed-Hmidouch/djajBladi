package org.example.djajbladibackend.dto.building;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class BuildingResponse {
    private Long id;
    private String name;
    private Integer maxCapacity;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
