package org.example.djajbladibackend.dto.stock;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.example.djajbladibackend.models.StockType;

import java.math.BigDecimal;

@Data
public class StockItemCreateRequest {

    @NotNull(message = "Type is required")
    private StockType type;

    @Size(max = 200)
    private String name;

    @NotNull(message = "Quantity is required")
    @DecimalMin(value = "0", inclusive = false, message = "Quantity must be positive")
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    @Size(max = 50)
    private String unit;
}
