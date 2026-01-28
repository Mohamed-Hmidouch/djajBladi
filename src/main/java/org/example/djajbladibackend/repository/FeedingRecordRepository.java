package org.example.djajbladibackend.repository;

import org.example.djajbladibackend.models.FeedingRecord;
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
public interface FeedingRecordRepository extends JpaRepository<FeedingRecord, Long> {

    // Spring Boot Best Practice: JOIN FETCH pour charger relations
    @Query("SELECT f FROM FeedingRecord f " +
            "LEFT JOIN FETCH f.batch " +
            "LEFT JOIN FETCH f.recordedBy " +
            "WHERE f.id = :id")
    Optional<FeedingRecord> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT f FROM FeedingRecord f " +
            "LEFT JOIN FETCH f.batch " +
            "LEFT JOIN FETCH f.recordedBy " +
            "WHERE f.batch.id = :batchId")
    List<FeedingRecord> findByBatchIdWithRelations(@Param("batchId") Long batchId);

    @Query("SELECT f FROM FeedingRecord f " +
            "LEFT JOIN FETCH f.batch " +
            "LEFT JOIN FETCH f.recordedBy " +
            "WHERE f.feedingDate BETWEEN :startDate AND :endDate")
    List<FeedingRecord> findByFeedingDateBetweenWithRelations(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT f FROM FeedingRecord f " +
            "LEFT JOIN FETCH f.batch " +
            "LEFT JOIN FETCH f.recordedBy " +
            "WHERE f.batch.id = :batchId " +
            "AND f.feedingDate BETWEEN :startDate AND :endDate")
    List<FeedingRecord> findByBatchIdAndDateRangeWithRelations(
            @Param("batchId") Long batchId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    List<FeedingRecord> findByBatchId(Long batchId);

    List<FeedingRecord> findByFeedingDateBetween(LocalDate startDate, LocalDate endDate);
}
