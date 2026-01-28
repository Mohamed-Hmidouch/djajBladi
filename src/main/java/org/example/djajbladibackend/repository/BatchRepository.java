package org.example.djajbladibackend.repository;

import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.BatchStatus;
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
public interface BatchRepository extends JpaRepository<Batch, Long> {

    // Spring Boot Best Practice: JOIN FETCH pour charger relations
    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy WHERE b.id = :id")
    Optional<Batch> findByIdWithCreatedBy(@Param("id") Long id);

    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy WHERE b.batchNumber = :batchNumber")
    Optional<Batch> findByBatchNumberWithCreatedBy(@Param("batchNumber") String batchNumber);

    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy WHERE b.status = :status")
    List<Batch> findByStatusWithCreatedBy(@Param("status") BatchStatus status);

    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy WHERE b.arrivalDate BETWEEN :startDate AND :endDate")
    List<Batch> findByArrivalDateBetweenWithCreatedBy(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT b FROM Batch b " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.feedingRecords " +
            "WHERE b.id = :id")
    Optional<Batch> findByIdWithFeedingRecords(@Param("id") Long id);

    @Query("SELECT b FROM Batch b " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.healthRecords " +
            "WHERE b.id = :id")
    Optional<Batch> findByIdWithHealthRecords(@Param("id") Long id);

    @Query("SELECT b FROM Batch b " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.sales " +
            "WHERE b.id = :id")
    Optional<Batch> findByIdWithSales(@Param("id") Long id);

    @Query("SELECT b FROM Batch b " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.feedingRecords " +
            "LEFT JOIN FETCH b.healthRecords " +
            "LEFT JOIN FETCH b.sales " +
            "WHERE b.id = :id")
    Optional<Batch> findByIdWithAllRelations(@Param("id") Long id);

    boolean existsByBatchNumber(String batchNumber);

    List<Batch> findByStatus(BatchStatus status);
}
