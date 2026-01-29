package org.example.djajbladibackend.repository;

import org.example.djajbladibackend.models.StockItem;
import org.example.djajbladibackend.models.StockType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    List<StockItem> findByTypeOrderByNameAsc(StockType type);

    List<StockItem> findAllByOrderByTypeAscNameAsc();
}
