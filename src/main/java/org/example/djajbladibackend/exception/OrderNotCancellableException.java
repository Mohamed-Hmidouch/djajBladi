package org.example.djajbladibackend.exception;

public class OrderNotCancellableException extends RuntimeException {

    private final Long orderId;
    private final String currentStatus;

    public OrderNotCancellableException(Long orderId, String currentStatus) {
        super(String.format(
            "La commande %d ne peut pas etre annulee. Statut actuel : %s",
            orderId, currentStatus
        ));
        this.orderId = orderId;
        this.currentStatus = currentStatus;
    }

    public Long getOrderId() { return orderId; }
    public String getCurrentStatus() { return currentStatus; }
}
