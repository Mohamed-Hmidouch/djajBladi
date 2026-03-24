package org.example.djajbladibackend.dto.vaccination;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class VaccinationProtocolResponse {
    private Long id;
    private String strain;
    private String vaccineName;
    private Integer dayOfLife;
    private String notes;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
