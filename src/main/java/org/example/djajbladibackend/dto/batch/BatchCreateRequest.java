package org.example.djajbladibackend.dto.batch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BatchCreateRequest {

    @NotBlank(message = "Batch number is required")
    @Size(max = 50)
    private String batchNumber;

    @NotBlank(message = "Strain is required")
    @Size(max = 100)
    private String strain;

    @NotNull(message = "Chicken count is required")
    @Positive(message = "Chicken count must be positive")
    private Integer chickenCount;

    @NotNull(message = "Arrival date is required")
    private LocalDate arrivalDate;

    @NotNull(message = "Purchase price is required")
    private BigDecimal purchasePrice;

    private Long buildingId;

    @Size(max = 2000)
    private String notes;
}
