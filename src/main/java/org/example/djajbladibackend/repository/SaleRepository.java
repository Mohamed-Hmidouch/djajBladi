package org.example.djajbladibackend.repository;

import org.example.djajbladibackend.models.PaymentStatus;
import org.example.djajbladibackend.models.Sale;
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
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // Spring Boot Best Practice: JOIN FETCH pour charger relations
    @Query("SELECT s FROM Sale s " +
            "LEFT JOIN FETCH s.batch " +
            "LEFT JOIN FETCH s.client " +
            "LEFT JOIN FETCH s.recordedBy " +
            "WHERE s.id = :id")
    Optional<Sale> findByIdWithRelations(@Param("id") Long id);

    @Query("SELECT s FROM Sale s " +
            "LEFT JOIN FETCH s.batch " +
            "LEFT JOIN FETCH s.client " +
            "LEFT JOIN FETCH s.recordedBy " +
            "WHERE s.batch.id = :batchId")
    List<Sale> findByBatchIdWithRelations(@Param("batchId") Long batchId);

    @Query("SELECT s FROM Sale s " +
            "LEFT JOIN FETCH s.batch " +
            "LEFT JOIN FETCH s.client " +
            "LEFT JOIN FETCH s.recordedBy " +
            "WHERE s.client.id = :clientId")
    List<Sale> findByClientIdWithRelations(@Param("clientId") Long clientId);

    @Query("SELECT s FROM Sale s " +
            "LEFT JOIN FETCH s.batch " +
            "LEFT JOIN FETCH s.client " +
            "LEFT JOIN FETCH s.recordedBy " +
            "WHERE s.paymentStatus = :paymentStatus")
    List<Sale> findByPaymentStatusWithRelations(@Param("paymentStatus") PaymentStatus paymentStatus);

    @Query("SELECT s FROM Sale s " +
            "LEFT JOIN FETCH s.batch " +
            "LEFT JOIN FETCH s.client " +
            "LEFT JOIN FETCH s.recordedBy " +
            "WHERE s.saleDate BETWEEN :startDate AND :endDate")
    List<Sale> findBySaleDateBetweenWithRelations(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT s FROM Sale s " +
            "LEFT JOIN FETCH s.batch " +
            "LEFT JOIN FETCH s.client " +
            "LEFT JOIN FETCH s.recordedBy " +
            "WHERE s.client.id = :clientId " +
            "AND s.paymentStatus = :paymentStatus")
    List<Sale> findByClientIdAndPaymentStatusWithRelations(
            @Param("clientId") Long clientId,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    List<Sale> findByBatchId(Long batchId);

    List<Sale> findByClientId(Long clientId);

    List<Sale> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Sale> findBySaleDateBetween(LocalDate startDate, LocalDate endDate);
}
