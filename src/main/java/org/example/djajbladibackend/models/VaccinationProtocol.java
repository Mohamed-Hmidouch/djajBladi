package org.example.djajbladibackend.models;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a strain-specific vaccination protocol.
 * Each protocol defines on which day of life a specific vaccine should be administered to a given chicken strain.
 */
@Entity
@Table(
    name = "vaccination_protocols",
    indexes = {
        @Index(name = "idx_vac_protocol_strain", columnList = "strain"),
        @Index(name = "idx_vac_protocol_day", columnList = "day_of_life")
    },
    uniqueConstraints = @UniqueConstraint(
        name = "uq_vac_protocol_strain_vaccine_day",
        columnNames = {"strain", "vaccine_name", "day_of_life"}
    )
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccinationProtocol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String strain;

    @Column(name = "vaccine_name", nullable = false, length = 200)
    private String vaccineName;

    @Column(name = "day_of_life", nullable = false)
    private Integer dayOfLife;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VaccinationProtocol)) return false;
        VaccinationProtocol that = (VaccinationProtocol) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
