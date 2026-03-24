package org.example.djajbladibackend.repository;

import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.BatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy LEFT JOIN FETCH b.assignedTo WHERE b.id = :id")
    Optional<Batch> findByIdWithCreatedBy(@Param("id") Long id);

    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy LEFT JOIN FETCH b.assignedTo LEFT JOIN FETCH b.building WHERE b.id = :id")
    Optional<Batch> findByIdWithCreatedByAndBuilding(@Param("id") Long id);

    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy LEFT JOIN FETCH b.assignedTo WHERE b.batchNumber = :batchNumber")
    Optional<Batch> findByBatchNumberWithCreatedBy(@Param("batchNumber") String batchNumber);

    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy LEFT JOIN FETCH b.assignedTo LEFT JOIN FETCH b.building WHERE b.status = :status")
    List<Batch> findByStatusWithCreatedBy(@Param("status") BatchStatus status);

    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy WHERE b.arrivalDate BETWEEN :startDate AND :endDate")
    List<Batch> findByArrivalDateBetweenWithCreatedBy(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT b FROM Batch b " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.assignedTo " +
            "LEFT JOIN FETCH b.feedingRecords " +
            "WHERE b.id = :id")
    Optional<Batch> findByIdWithFeedingRecords(@Param("id") Long id);

    @Query("SELECT b FROM Batch b " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.assignedTo " +
            "LEFT JOIN FETCH b.healthRecords " +
            "WHERE b.id = :id")
    Optional<Batch> findByIdWithHealthRecords(@Param("id") Long id);

    @Query("SELECT b FROM Batch b " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.assignedTo " +
            "LEFT JOIN FETCH b.sales " +
            "WHERE b.id = :id")
    Optional<Batch> findByIdWithSales(@Param("id") Long id);

    @Query("SELECT b FROM Batch b " +
            "LEFT JOIN FETCH b.createdBy " +
            "LEFT JOIN FETCH b.assignedTo " +
            "LEFT JOIN FETCH b.feedingRecords " +
            "LEFT JOIN FETCH b.healthRecords " +
            "LEFT JOIN FETCH b.sales " +
            "WHERE b.id = :id")
    Optional<Batch> findByIdWithAllRelations(@Param("id") Long id);

    boolean existsByBatchNumber(String batchNumber);

    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy LEFT JOIN FETCH b.assignedTo LEFT JOIN FETCH b.building ORDER BY b.createdAt DESC")
    List<Batch> findAllWithCreatedByAndBuildingOrderByCreatedAtDesc();

    List<Batch> findByStatus(BatchStatus status);

    @Query(value = "SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy LEFT JOIN FETCH b.assignedTo LEFT JOIN FETCH b.building ORDER BY b.createdAt DESC",
           countQuery = "SELECT COUNT(b) FROM Batch b")
    Page<Batch> findAllWithRelationsPageable(Pageable pageable);

    @Query(value = "SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy LEFT JOIN FETCH b.assignedTo LEFT JOIN FETCH b.building WHERE b.assignedTo.id = :assignedToId ORDER BY b.createdAt DESC",
           countQuery = "SELECT COUNT(b) FROM Batch b WHERE b.assignedTo.id = :assignedToId")
    Page<Batch> findByAssignedToIdWithRelationsPageable(@Param("assignedToId") Long assignedToId, Pageable pageable);
    
    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy LEFT JOIN FETCH b.assignedTo LEFT JOIN FETCH b.building WHERE b.assignedTo.id = :assignedToId ORDER BY b.createdAt DESC")
    List<Batch> findByAssignedToIdWithRelations(@Param("assignedToId") Long assignedToId);

    /**
     * Atomically decrements current_count only when sufficient stock is available.
     * Returns 1 if update succeeded, 0 if current_count < mortalityCount.
     */
    @Modifying
    @Query("UPDATE Batch b SET b.currentCount = b.currentCount - :mortalityCount " +
            "WHERE b.id = :batchId AND b.currentCount >= :mortalityCount")
    int decrementCurrentCount(@Param("batchId") Long batchId,
                              @Param("mortalityCount") Integer mortalityCount);

    @Query("SELECT b FROM Batch b LEFT JOIN FETCH b.createdBy LEFT JOIN FETCH b.assignedTo LEFT JOIN FETCH b.building WHERE b.status IN :statuses ORDER BY b.createdAt DESC")
    List<Batch>  findByStatusInWithRelations(@Param("statuses") java.util.List<org.example.djajbladibackend.models.BatchStatus> statuses);
}
