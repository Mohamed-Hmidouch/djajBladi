package org.example.djajbladibackend.dto.client;

import lombok.Builder;
import lombok.Data;
import org.example.djajbladibackend.models.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Reponse apres passage/consultation d'une commande client.
 */
@Data
@Builder
public class PurchaseOrderResponse {

    private Long orderId;
    private String batchNumber;
    private String strain;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private LocalDate saleDate;
    private PaymentStatus paymentStatus;
    private String deliveryAddress;
    private String notes;
    private LocalDateTime createdAt;
}
