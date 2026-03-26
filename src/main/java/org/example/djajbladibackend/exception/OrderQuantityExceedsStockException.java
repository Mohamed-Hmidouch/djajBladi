package org.example.djajbladibackend.exception;

public class OrderQuantityExceedsStockException extends RuntimeException {

    private final Long batchId;
    private final int requested;
    private final int available;

    public OrderQuantityExceedsStockException(Long batchId, int requested, int available) {
        super(String.format(
            "Quantite demandee (%d) depasse le stock disponible (%d) pour le lot %d",
            requested, available, batchId
        ));
        this.batchId = batchId;
        this.requested = requested;
        this.available = available;
    }

    public Long getBatchId() { return batchId; }
    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}
