package org.example.djajbladibackend.repository;

import jakarta.persistence.LockModeType;
import org.example.djajbladibackend.models.StockItem;
import org.example.djajbladibackend.models.StockType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    List<StockItem> findByTypeOrderByNameAsc(StockType type);

    List<StockItem> findAllByOrderByTypeAscNameAsc();

    Page<StockItem> findAllByOrderByTypeAscNameAsc(Pageable pageable);

    /**
     * Pessimistic write lock : bloque la ligne en base jusqu'a la fin de la transaction.
     * Indispensable pour eviter les race conditions quand plusieurs ouvriers
     * consomment le meme item de stock simultanement.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockItem s WHERE s.id = :id")
    Optional<StockItem> findByIdForUpdate(@Param("id") Long id);
}
