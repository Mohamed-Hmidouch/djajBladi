package org.example.djajbladibackend.exception;

public class OrderOwnershipException extends RuntimeException {

    private final Long orderId;

    public OrderOwnershipException(Long orderId) {
        super(String.format(
            "Acces refuse : la commande %d ne vous appartient pas",
            orderId
        ));
        this.orderId = orderId;
    }

    public Long getOrderId() { return orderId; }
}
