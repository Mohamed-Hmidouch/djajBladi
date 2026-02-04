package org.example.djajbladibackend.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Spring Boot Best Practice: Entity avec @Builder, LAZY relations, auditing
 * Représente un enregistrement de santé/visite vétérinaire (health record)
 */
@Entity
@Table(name = "health_records", indexes = {
        @Index(name = "idx_health_batch", columnList = "batch_id"),
        @Index(name = "idx_health_veterinarian", columnList = "veterinarian_id"),
        @Index(name = "idx_health_examination_date", columnList = "examination_date"),
        @Index(name = "idx_health_next_visit", columnList = "next_visit_date")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Spring Boot Best Practice: LAZY fetch pour éviter N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    // Spring Boot Best Practice: LAZY fetch pour éviter N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id", nullable = false)
    private User veterinarian;

    @Column(nullable = false)
    private String diagnosis;

    @Column(columnDefinition = "TEXT")
    private String treatment;

    @Column(name = "examination_date", nullable = false)
    private LocalDate examinationDate;

    @Column(name = "next_visit_date")
    private LocalDate nextVisitDate;

    @Column(name = "mortality_count")
    @Builder.Default
    private Integer mortalityCount = 0;

    @Column(name = "treatment_cost", precision = 12, scale = 2)
    private BigDecimal treatmentCost;

    @Column(name = "requires_approval", nullable = false)
    @Builder.Default
    private Boolean requiresApproval = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", length = 20)
    private ApprovalStatus approvalStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(columnDefinition = "TEXT")
    private String notes;

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
        if (!(o instanceof HealthRecord)) return false;
        HealthRecord that = (HealthRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Méthodes utilitaires
    public boolean hasNextVisit() {
        return nextVisitDate != null;
    }

    public boolean hasMortality() {
        return mortalityCount != null && mortalityCount > 0;
    }
}
