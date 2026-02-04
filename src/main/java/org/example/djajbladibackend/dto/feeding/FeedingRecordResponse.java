package org.example.djajbladibackend.dto.feeding;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class FeedingRecordResponse {
    private Long id;
    private Long batchId;
    private String batchNumber;
    private String feedType;
    private BigDecimal quantity;
    private LocalDate feedingDate;
    private String notes;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
