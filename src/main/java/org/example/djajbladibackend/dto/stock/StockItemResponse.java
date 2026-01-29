package org.example.djajbladibackend.dto.stock;

import lombok.Builder;
import lombok.Data;
import org.example.djajbladibackend.models.StockType;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class StockItemResponse {
    private Long id;
    private StockType type;
    private String name;
    private BigDecimal quantity;
    private String unit;
    private Instant createdAt;
    private Instant updatedAt;
}
