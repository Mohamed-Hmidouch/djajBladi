package org.example.djajbladibackend.dto.mortality;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class DailyMortalityResponse {
    private Long id;
    private Long batchId;
    private String batchNumber;
    private LocalDate recordDate;
    private Integer mortalityCount;
    private String notes;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
