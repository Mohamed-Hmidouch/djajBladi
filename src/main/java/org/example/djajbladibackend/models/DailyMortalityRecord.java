package org.example.djajbladibackend.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "daily_mortality_records", indexes = {
        @Index(name = "idx_mortality_batch", columnList = "batch_id"),
        @Index(name = "idx_mortality_date", columnList = "record_date"),
        @Index(name = "idx_mortality_recorded_by", columnList = "recorded_by_id")
}, uniqueConstraints = @UniqueConstraint(name = "uq_mortality_batch_date", columnNames = {"batch_id", "record_date"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyMortalityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "mortality_count", nullable = false)
    private Integer mortalityCount;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_id", nullable = false)
    private User recordedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DailyMortalityRecord)) return false;
        DailyMortalityRecord that = (DailyMortalityRecord) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
