package org.example.djajbladibackend.repository;

import org.example.djajbladibackend.models.FeedingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = "SELECT f FROM FeedingRecord f " +
            "LEFT JOIN FETCH f.batch " +
            "LEFT JOIN FETCH f.recordedBy " +
            "WHERE f.feedingDate BETWEEN :startDate AND :endDate " +
            "ORDER BY f.feedingDate DESC",
           countQuery = "SELECT COUNT(f) FROM FeedingRecord f " +
            "WHERE f.feedingDate BETWEEN :startDate AND :endDate")
    Page<FeedingRecord> findByDateRangePageable(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query(value = "SELECT f FROM FeedingRecord f " +
            "LEFT JOIN FETCH f.batch " +
            "LEFT JOIN FETCH f.recordedBy " +
            "WHERE f.batch.id = :batchId " +
            "AND f.feedingDate BETWEEN :startDate AND :endDate " +
            "ORDER BY f.feedingDate DESC",
           countQuery = "SELECT COUNT(f) FROM FeedingRecord f " +
            "WHERE f.batch.id = :batchId " +
            "AND f.feedingDate BETWEEN :startDate AND :endDate")
    Page<FeedingRecord> findByBatchIdAndDateRangePageable(
            @Param("batchId") Long batchId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    List<FeedingRecord> findByBatchId(Long batchId);

    List<FeedingRecord> findByFeedingDateBetween(LocalDate startDate, LocalDate endDate);

    /**
     * Charge les enregistrements d'alimentation d'un lot avec le stock item lie
     * pour le calcul du cout de revient alimentaire.
     */
    @Query("SELECT f FROM FeedingRecord f " +
            "LEFT JOIN FETCH f.batch " +
            "LEFT JOIN FETCH f.stockItem " +
            "LEFT JOIN FETCH f.recordedBy " +
            "WHERE f.batch.id = :batchId")
    List<FeedingRecord> findByBatchIdWithStockItem(@Param("batchId") Long batchId);
}
