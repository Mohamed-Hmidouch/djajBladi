package org.example.djajbladibackend.services.stock;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import org.example.djajbladibackend.exception.InsufficientStockException;
import org.example.djajbladibackend.models.StockItem;
import org.example.djajbladibackend.models.StockType;
import org.example.djajbladibackend.repository.StockItemRepository;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Property-based tests for StockService stock management.
 * Requirements: 4.1, 4.2, 4.3
 */
class StockServicePropertyTest {

    private final StockItemRepository stockItemRepository = Mockito.mock(StockItemRepository.class);
    private final org.example.djajbladibackend.repository.auth.UserRepository userRepository =
            Mockito.mock(org.example.djajbladibackend.repository.auth.UserRepository.class);
    private final StockService stockService = new StockService(stockItemRepository, userRepository);

    /**
     * Property 12: isAvailable returns false when requested > available.
     * Requirements: 4.1
     */
    @Property
    @Label("Property 12: isAvailable returns false when stock insufficient")
    void isAvailableReturnsFalseWhenInsufficient(
            @ForAll @IntRange(min = 1, max = 100) int available,
            @ForAll @IntRange(min = 1, max = 50) int extra) {

        Long itemId = 1L;
        BigDecimal availableQty = BigDecimal.valueOf(available);
        BigDecimal requestedQty = availableQty.add(BigDecimal.valueOf(extra));

        StockItem item = StockItem.builder()
                .id(itemId)
                .name("TestMed")
                .quantity(availableQty)
                .stockType(StockType.MEDICATION)
                .build();

        when(stockItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        boolean result = stockService.isAvailable(itemId, requestedQty);

        assertThat(result).isFalse();
    }

    /**
     * Property 13: isAvailable returns true when requested <= available.
     * Requirements: 4.2
     */
    @Property
    @Label("Property 13: isAvailable returns true when stock sufficient")
    void isAvailableReturnsTrueWhenSufficient(
            @ForAll @IntRange(min = 10, max = 100) int available,
            @ForAll @IntRange(min = 1, max = 9) int requested) {

        Long itemId = 2L;
        BigDecimal availableQty = BigDecimal.valueOf(available);
        BigDecimal requestedQty = BigDecimal.valueOf(requested);

        StockItem item = StockItem.builder()
                .id(itemId)
                .name("TestVaccine")
                .quantity(availableQty)
                .stockType(StockType.VACCINE)
                .build();

        when(stockItemRepository.findById(itemId)).thenReturn(Optional.of(item));

        boolean result = stockService.isAvailable(itemId, requestedQty);

        assertThat(result).isTrue();
    }

    /**
     * Property: deductQuantity throws InsufficientStockException when stock insufficient.
     * Requirements: 4.3
     */
    @Property
    @Label("Property: deductQuantity throws InsufficientStockException when insufficient")
    void deductQuantityThrowsWhenInsufficient(
            @ForAll @IntRange(min = 1, max = 50) int available,
            @ForAll @IntRange(min = 1, max = 50) int extra) {

        Long itemId = 3L;
        BigDecimal availableQty = BigDecimal.valueOf(available);
        BigDecimal requestedQty = availableQty.add(BigDecimal.valueOf(extra));

        StockItem item = StockItem.builder()
                .id(itemId)
                .name("TestFeed")
                .quantity(availableQty)
                .unitPrice(BigDecimal.ONE)
                .stockType(StockType.FEED)
                .build();

        when(stockItemRepository.findByIdForUpdate(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> stockService.deductQuantity(itemId, requestedQty))
                .isInstanceOf(InsufficientStockException.class);
    }
}
