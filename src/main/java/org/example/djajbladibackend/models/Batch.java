package org.example.djajbladibackend.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Spring Boot Best Practice: Entity avec @Builder, LAZY relations, auditing
 * Représente un lot de poules (batch)
 */
@Entity
@Table(name = "batches", indexes = {
        @Index(name = "idx_batch_number", columnList = "batch_number"),
        @Index(name = "idx_batch_status", columnList = "status"),
        @Index(name = "idx_batch_arrival_date", columnList = "arrival_date"),
        @Index(name = "idx_batch_created_by", columnList = "created_by_id"),
        @Index(name = "idx_batch_building", columnList = "building_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_number", unique = true, nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "chicken_count", nullable = false)
    private Integer chickenCount;

    @Column(name = "arrival_date", nullable = false)
    private LocalDate arrivalDate;

    @Column(length = 100)
    private String strain;

    @Column(name = "purchase_price", precision = 10, scale = 2)
    private BigDecimal purchasePrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id")
    private Building building;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BatchStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    // Spring Boot Best Practice: LAZY fetch pour collections
    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<FeedingRecord> feedingRecords = new ArrayList<>();

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<HealthRecord> healthRecords = new ArrayList<>();

    @OneToMany(mappedBy = "batch", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Sale> sales = new ArrayList<>();

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
        if (!(o instanceof Batch)) return false;
        Batch batch = (Batch) o;
        return Objects.equals(id, batch.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Méthodes utilitaires
    public boolean isActive() {
        return status == BatchStatus.Active;
    }

    public boolean isCompleted() {
        return status == BatchStatus.Completed;
    }

    public boolean isArchived() {
        return status == BatchStatus.Archived;
    }

    public void addFeedingRecord(FeedingRecord feedingRecord) {
        feedingRecords.add(feedingRecord);
        feedingRecord.setBatch(this);
    }

    public void addHealthRecord(HealthRecord healthRecord) {
        healthRecords.add(healthRecord);
        healthRecord.setBatch(this);
    }

    public void addSale(Sale sale) {
        sales.add(sale);
        sale.setBatch(this);
    }
}
