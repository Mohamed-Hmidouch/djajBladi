package org.example.djajbladibackend.repository;

import org.example.djajbladibackend.models.DailyMortalityRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyMortalityRecordRepository extends JpaRepository<DailyMortalityRecord, Long> {

    @Query("SELECT d FROM DailyMortalityRecord d " +
            "LEFT JOIN FETCH d.batch " +
            "LEFT JOIN FETCH d.recordedBy " +
            "WHERE d.id = :id")
    Optional<DailyMortalityRecord> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT d FROM DailyMortalityRecord d " +
            "LEFT JOIN FETCH d.batch " +
            "LEFT JOIN FETCH d.recordedBy " +
            "WHERE d.batch.id = :batchId AND d.recordDate = :recordDate")
    Optional<DailyMortalityRecord> findByBatchIdAndRecordDate(
            @Param("batchId") Long batchId,
            @Param("recordDate") LocalDate recordDate);

    boolean existsByBatchIdAndRecordDate(Long batchId, LocalDate recordDate);

    @Query("SELECT d FROM DailyMortalityRecord d " +
            "LEFT JOIN FETCH d.batch " +
            "LEFT JOIN FETCH d.recordedBy " +
            "WHERE d.recordDate BETWEEN :startDate AND :endDate " +
            "ORDER BY d.recordDate DESC, d.batch.id")
    List<DailyMortalityRecord> findByRecordDateBetweenWithRelations(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT d FROM DailyMortalityRecord d " +
            "LEFT JOIN FETCH d.batch " +
            "LEFT JOIN FETCH d.recordedBy " +
            "WHERE d.batch.id = :batchId " +
            "AND d.recordDate BETWEEN :startDate AND :endDate " +
            "ORDER BY d.recordDate DESC")
    List<DailyMortalityRecord> findByBatchIdAndRecordDateBetweenWithRelations(
            @Param("batchId") Long batchId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
