package org.example.djajbladibackend.exception;

import java.time.LocalDate;

/**
 * Thrown when a batch cannot be sold or marked ready-for-sale
 * because an active antibiotic withdrawal period is still in effect.
 * HTTP 409 Conflict.
 */
public class WithdrawalPeriodActiveException extends RuntimeException {

    private final Long batchId;
    private final LocalDate expirationDate;

    public WithdrawalPeriodActiveException(Long batchId, LocalDate expirationDate) {
        super(String.format(
            "Batch %d has an active withdrawal period. Cannot sell until %s.",
            batchId, expirationDate
        ));
        this.batchId = batchId;
        this.expirationDate = expirationDate;
    }

    public Long getBatchId() { return batchId; }
    public LocalDate getExpirationDate() { return expirationDate; }
}
