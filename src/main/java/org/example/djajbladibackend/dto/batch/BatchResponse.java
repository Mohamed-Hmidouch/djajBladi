package org.example.djajbladibackend.dto.batch;

import lombok.Builder;
import lombok.Data;
import org.example.djajbladibackend.models.BatchStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class BatchResponse {
    private Long id;
    private String batchNumber;
    private String strain;
    private Integer chickenCount;
    private LocalDate arrivalDate;
    private BigDecimal purchasePrice;
    private Long buildingId;
    private String buildingName;
    private BatchStatus status;
    private String notes;
    private Long createdById;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
