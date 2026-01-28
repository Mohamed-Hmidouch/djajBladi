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
 * Représente un enregistrement d'alimentation (feeding record)
 */
@Entity
@Table(name = "feeding_records", indexes = {
        @Index(name = "idx_feeding_batch", columnList = "batch_id"),
        @Index(name = "idx_feeding_date", columnList = "feeding_date"),
        @Index(name = "idx_feeding_recorded_by", columnList = "recorded_by_id")
})
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeedingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Spring Boot Best Practice: LAZY fetch pour éviter N+1
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(name = "feed_type", nullable = false, length = 100)
    private String feedType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity;

    @Column(name = "feeding_date", nullable = false)
    private LocalDate feedingDate;

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
        if (!(o instanceof FeedingRecord)) return false;
        FeedingRecord that = (FeedingRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
