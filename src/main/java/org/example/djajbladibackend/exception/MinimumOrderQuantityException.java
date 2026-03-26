package org.example.djajbladibackend.exception;

public class MinimumOrderQuantityException extends RuntimeException {

    private final Long batchId;
    private final int requested;
    private final int minimum;

    public MinimumOrderQuantityException(Long batchId, int requested, int minimum) {
        super(String.format(
            "Quantite minimum pour le lot %d est %d poulets. Vous avez demande %d",
            batchId, minimum, requested
        ));
        this.batchId = batchId;
        this.requested = requested;
        this.minimum = minimum;
    }

    public Long getBatchId() { return batchId; }
    public int getRequested() { return requested; }
    public int getMinimum() { return minimum; }
}
