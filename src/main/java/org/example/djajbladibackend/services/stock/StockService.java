package org.example.djajbladibackend.services.stock;

import lombok.extern.slf4j.Slf4j;
import org.example.djajbladibackend.dto.common.PageResponse;
import org.example.djajbladibackend.dto.stock.StockItemCreateRequest;
import org.example.djajbladibackend.dto.stock.StockItemResponse;
import org.example.djajbladibackend.exception.InsufficientStockException;
import org.example.djajbladibackend.exception.InvalidDataException;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.StockItem;
import org.example.djajbladibackend.models.StockType;
import org.example.djajbladibackend.repository.StockItemRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final UserRepository userRepository;

    public StockService(StockItemRepository stockItemRepository, UserRepository userRepository) {
        this.stockItemRepository = stockItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public StockItemResponse add(StockItemCreateRequest req, String adminEmail) {
        var createdBy = userRepository.findByEmail(adminEmail).orElse(null);
        StockItem item = StockItem.builder()
                .type(req.getType())
                .stockType(req.getType())
                .name(req.getName())
                .quantity(req.getQuantity())
                .unit(req.getUnit())
                .unitPrice(req.getUnitPrice())
                .createdBy(createdBy)
                .build();
        StockItem saved = stockItemRepository.save(item);
        return toResponse(saved);
    }

    public StockItemResponse findById(Long id) {
        StockItem item = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockItem", "id", id));
        return toResponse(item);
    }

    public PageResponse<StockItemResponse> findAll(int page, int size) {
        return PageResponse.from(
                stockItemRepository.findAllByOrderByTypeAscNameAsc(PageRequest.of(page, size))
                        .map(this::toResponse)
        );
    }

    public List<StockItemResponse> findByType(StockType stockType) {
        return stockItemRepository.findByTypeOrderByNameAsc(stockType).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Checks whether at least the given quantity is available for the stock item.
     */
    public boolean isAvailable(Long stockItemId, BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new InvalidDataException("Quantity must be > 0");
        }
        StockItem item = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new ResourceNotFoundException("StockItem", "id", stockItemId));
        return item.getQuantity() != null && item.getQuantity().compareTo(quantity) >= 0;
    }

    /**
     * Returns the unit price of a stock item.
     */
    public BigDecimal getUnitPrice(Long stockItemId) {
        StockItem item = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new ResourceNotFoundException("StockItem", "id", stockItemId));
        return item.getUnitPrice();
    }

    /**
     * Atomically deducts the given quantity from the stock item using a pessimistic write lock.
     * Throws InsufficientStockException if stock is insufficient.
     */
    @Transactional
    public void deductQuantity(Long stockItemId, BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new InvalidDataException("Quantity must be > 0");
        }
        StockItem item = stockItemRepository.findByIdForUpdate(stockItemId)
                .orElseThrow(() -> new ResourceNotFoundException("StockItem", "id", stockItemId));
        if (item.getQuantity() == null || item.getQuantity().compareTo(quantity) < 0) {
            throw new InsufficientStockException(
                    stockItemId,
                    item.getName(),
                    item.getQuantity() != null ? item.getQuantity() : BigDecimal.ZERO,
                    quantity
            );
        }
        item.setQuantity(item.getQuantity().subtract(quantity));
        stockItemRepository.save(item);
        log.info("Stock deducted: stockItemId={}, name={}, quantity={}, remaining={}",
                stockItemId, item.getName(), quantity, item.getQuantity());
    }

    private StockItemResponse toResponse(StockItem item) {
        return StockItemResponse.builder()
                .id(item.getId())
                .type(item.getType())
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .unitPrice(item.getUnitPrice())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
