package org.example.djajbladibackend.repository;

import org.example.djajbladibackend.models.HealthRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Boot Best Practice: Repository avec JOIN FETCH pour éviter N+1
 */
@Repository
public interface HealthRecordRepository extends JpaRepository<HealthRecord, Long> {

    // Spring Boot Best Practice: JOIN FETCH pour charger relations
    @Query("SELECT h FROM HealthRecord h " +
            "LEFT JOIN FETCH h.batch " +
            "LEFT JOIN FETCH h.veterinarian " +
            "WHERE h.id = :id")
    Optional<HealthRecord> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT h FROM HealthRecord h " +
            "LEFT JOIN FETCH h.batch " +
            "LEFT JOIN FETCH h.veterinarian " +
            "WHERE h.batch.id = :batchId")
    List<HealthRecord> findByBatchIdWithRelations(@Param("batchId") Long batchId);

    @Query("SELECT h FROM HealthRecord h " +
            "LEFT JOIN FETCH h.batch " +
            "LEFT JOIN FETCH h.veterinarian " +
            "WHERE h.veterinarian.id = :veterinarianId")
    List<HealthRecord> findByVeterinarianIdWithRelations(@Param("veterinarianId") Long veterinarianId);

    @Query("SELECT h FROM HealthRecord h " +
            "LEFT JOIN FETCH h.batch " +
            "LEFT JOIN FETCH h.veterinarian " +
            "WHERE h.examinationDate BETWEEN :startDate AND :endDate")
    List<HealthRecord> findByExaminationDateBetweenWithRelations(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT h FROM HealthRecord h " +
            "LEFT JOIN FETCH h.batch " +
            "LEFT JOIN FETCH h.veterinarian " +
            "WHERE h.nextVisitDate IS NOT NULL " +
            "AND h.nextVisitDate BETWEEN :startDate AND :endDate")
    List<HealthRecord> findUpcomingVisitsWithRelations(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT h FROM HealthRecord h " +
            "LEFT JOIN FETCH h.batch " +
            "LEFT JOIN FETCH h.veterinarian " +
            "WHERE h.mortalityCount > 0")
    List<HealthRecord> findRecordsWithMortalityWithRelations();

    @Query("SELECT h FROM HealthRecord h " +
            "LEFT JOIN FETCH h.batch " +
            "LEFT JOIN FETCH h.veterinarian " +
            "WHERE h.requiresApproval = true AND h.approvalStatus = 'PENDING_APPROVAL' " +
            "ORDER BY h.createdAt ASC")
    List<HealthRecord> findPendingApprovalWithRelations();

    List<HealthRecord> findByBatchId(Long batchId);

    List<HealthRecord> findByVeterinarianId(Long veterinarianId);

    List<HealthRecord> findByExaminationDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COALESCE(SUM(h.treatmentCost), 0) FROM HealthRecord h WHERE h.batch.id = :batchId")
    java.math.BigDecimal sumTreatmentCostByBatchId(@Param("batchId") Long batchId);

    /**
     * Returns health records for a batch that still have an active withdrawal period today.
     * Only non-vaccination records with withdrawalDays > 0 where today < examinationDate + withdrawalDays.
     */
    @Query("SELECT h FROM HealthRecord h " +
            "WHERE h.batch.id = :batchId " +
            "AND h.withdrawalDays IS NOT NULL " +
            "AND h.withdrawalDays > 0 " +
            "AND h.isVaccination = false " +
            "AND FUNCTION('ADDDATE', h.examinationDate, h.withdrawalDays) > CURRENT_DATE")
    List<HealthRecord> findActiveWithdrawalPeriods(@Param("batchId") Long batchId);

    /**
     * Returns the latest withdrawal expiration date across all non-vaccination health records for a batch.
     */
    @Query("SELECT MAX(FUNCTION('ADDDATE', h.examinationDate, h.withdrawalDays)) " +
            "FROM HealthRecord h " +
            "WHERE h.batch.id = :batchId " +
            "AND h.withdrawalDays IS NOT NULL " +
            "AND h.withdrawalDays > 0 " +
            "AND h.isVaccination = false")
    java.time.LocalDate findLatestWithdrawalExpiration(@Param("batchId") Long batchId);

    /**
     * Returns vaccination health records for a batch matching a given vaccine name.
     */
    @Query("SELECT h FROM HealthRecord h " +
            "WHERE h.batch.id = :batchId " +
            "AND h.isVaccination = true " +
            "AND LOWER(h.diagnosis) LIKE LOWER(CONCAT('%', :vaccineName, '%'))")
    List<HealthRecord> findVaccinationRecords(@Param("batchId") Long batchId,
                                              @Param("vaccineName") String vaccineName);
}
