package org.example.djajbladibackend.dto.feeding;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class FeedingRecordRequest {

    @NotNull(message = "Batch ID is required")
    private Long batchId;

    @NotNull(message = "Feed type is required")
    @Size(max = 100)
    private String feedType;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0.01", message = "Quantity must be > 0")
    private BigDecimal quantity;

    @NotNull(message = "Feeding date is required")
    private LocalDate feedingDate;

    @Size(max = 2000)
    private String notes;
}
