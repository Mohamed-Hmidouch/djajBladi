package org.example.djajbladibackend.dto.mortality;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyMortalityRequest {

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Record date is required")
    private LocalDate recordDate;

    @NotNull(message = "Mortality count is required")
    @Min(value = 0, message = "Mortality count must be >= 0")
    private Integer mortalityCount;

    private String notes;
}
