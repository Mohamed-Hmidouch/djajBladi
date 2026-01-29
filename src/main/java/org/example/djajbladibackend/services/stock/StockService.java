package org.example.djajbladibackend.services.stock;

import org.example.djajbladibackend.dto.stock.StockItemCreateRequest;
import org.example.djajbladibackend.dto.stock.StockItemResponse;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.StockItem;
import org.example.djajbladibackend.repository.StockItemRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
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
                .name(req.getName())
                .quantity(req.getQuantity())
                .unit(req.getUnit())
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

    public List<StockItemResponse> findAll() {
        return stockItemRepository.findAllByOrderByTypeAscNameAsc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private StockItemResponse toResponse(StockItem item) {
        return StockItemResponse.builder()
                .id(item.getId())
                .type(item.getType())
                .name(item.getName())
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
