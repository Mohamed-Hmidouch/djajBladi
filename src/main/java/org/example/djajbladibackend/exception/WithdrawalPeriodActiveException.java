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
            "Le lot %d est en période de retrait. Vente impossible avant le %s.",
            batchId, expirationDate
        ));
        this.batchId = batchId;
        this.expirationDate = expirationDate;
    }

    public Long getBatchId() { return batchId; }
    public LocalDate getExpirationDate() { return expirationDate; }
}
