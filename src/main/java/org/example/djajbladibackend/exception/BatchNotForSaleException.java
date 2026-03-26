package org.example.djajbladibackend.exception;

public class BatchNotForSaleException extends RuntimeException {

    private final Long batchId;
    private final String currentStatus;

    public BatchNotForSaleException(Long batchId, String currentStatus) {
        super(String.format(
            "Le lot %d n'est pas disponible a la vente. Statut actuel : %s",
            batchId, currentStatus
        ));
        this.batchId = batchId;
        this.currentStatus = currentStatus;
    }

    public Long getBatchId() { return batchId; }
    public String getCurrentStatus() { return currentStatus; }
}
