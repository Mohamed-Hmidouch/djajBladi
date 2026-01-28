package org.example.djajbladibackend.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Spring Boot Best Practice: Entity avec @Builder, LAZY relations, auditing
 * Représente une vente de poules (sale)
 */
@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sale_batch", columnList = "batch_id"),
        @Index(name = "idx_sale_client", columnList = "client_id"),
        @Index(name = "idx_sale_date", columnList = "sale_date"),
        @Index(name = "idx_sale_payment_status", columnList = "payment_status"),
        @Index(name = "idx_sale_recorded_by", columnList = "recorded_by_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Spring Boot Best Practice: LAZY fetch pour éviter N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    // Spring Boot Best Practice: LAZY fetch pour éviter N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Spring Boot Best Practice: LAZY fetch pour éviter N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Spring Boot Best Practice: equals/hashCode basés sur ID
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sale)) return false;
        Sale sale = (Sale) o;
        return Objects.equals(id, sale.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Méthodes utilitaires
    public void calculateTotalPrice() {
        if (unitPrice != null && quantity != null) {
            this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public boolean isPending() {
        return paymentStatus == PaymentStatus.Pending;
    }

    public boolean isPaid() {
        return paymentStatus == PaymentStatus.Paid;
    }

    public boolean isCancelled() {
        return paymentStatus == PaymentStatus.Cancelled;
    }
}
