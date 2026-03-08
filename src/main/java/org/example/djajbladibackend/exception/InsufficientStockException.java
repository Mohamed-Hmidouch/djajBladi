package org.example.djajbladibackend.exception;

import java.math.BigDecimal;

/**
 * Levée quand la quantité disponible en stock est inférieure à la quantité demandée.
 * HTTP 409 Conflict — l'état du stock est incompatible avec l'opération demandée.
 */
public class InsufficientStockException extends RuntimeException {

    private final Long stockItemId;
    private final String stockItemName;
    private final BigDecimal available;
    private final BigDecimal requested;

    public InsufficientStockException(Long stockItemId, String stockItemName,
                                       BigDecimal available, BigDecimal requested) {
        super(String.format(
            "Stock insuffisant pour '%s' (id=%d) : disponible=%.2f kg, demande=%.2f kg",
            stockItemName, stockItemId, available, requested
        ));
        this.stockItemId = stockItemId;
        this.stockItemName = stockItemName;
        this.available = available;
        this.requested = requested;
    }

    public Long getStockItemId() { return stockItemId; }
    public String getStockItemName() { return stockItemName; }
    public BigDecimal getAvailable() { return available; }
    public BigDecimal getRequested() { return requested; }
}
