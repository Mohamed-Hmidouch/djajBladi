package org.example.djajbladibackend.services.financial;

import org.example.djajbladibackend.dto.financial.BatchCostBreakdownResponse;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.*;
import org.example.djajbladibackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BatchCostService Unit Tests")
class BatchCostServiceTest {

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private FeedingRecordRepository feedingRecordRepository;

    @Mock
    private HealthRecordRepository healthRecordRepository;

    @Mock
    private DailyMortalityRecordRepository mortalityRecordRepository;

    @Mock
    private SaleRepository saleRepository;

    @InjectMocks
    private BatchCostService batchCostService;

    private Batch batch;
    private StockItem stockItemFeed;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(batchCostService, "defaultFixedChargesDH", BigDecimal.valueOf(500));

        batch = Batch.builder()
                .id(1L)
                .batchNumber("BATCH-001")
                .strain("Ross 308")
                .arrivalDate(LocalDate.now().minusDays(30))
                .status(BatchStatus.Active)
                .chickenCount(1000)
                .purchasePrice(BigDecimal.valueOf(15))  // 15 DH/poussin * 1000 = 15000 DH
                .build();

        stockItemFeed = StockItem.builder()
                .id(10L)
                .name("Aliment Demarrage")
                .type(StockType.FEED)
                .unit("kg")
                .unitPrice(BigDecimal.valueOf(3.5))
                .build();
    }

    @Test
    @DisplayName("doit calculer le cout complet avec toutes les composantes")
    void calculateCost_shouldReturnFullBreakdown() {
        FeedingRecord feeding1 = FeedingRecord.builder()
                .id(1L)
                .batch(batch)
                .stockItem(stockItemFeed)
                .feedType("Demarrage")
                .quantity(BigDecimal.valueOf(200))
                .feedingDate(LocalDate.now().minusDays(10))
                .build();
        FeedingRecord feeding2 = FeedingRecord.builder()
                .id(2L)
                .batch(batch)
                .stockItem(stockItemFeed)
                .feedType("Demarrage")
                .quantity(BigDecimal.valueOf(300))
                .feedingDate(LocalDate.now().minusDays(5))
                .build();

        when(batchRepository.findByIdWithCreatedByAndBuilding(1L)).thenReturn(Optional.of(batch));
        when(feedingRecordRepository.findByBatchIdWithStockItem(1L)).thenReturn(List.of(feeding1, feeding2));
        when(healthRecordRepository.sumTreatmentCostByBatchId(1L)).thenReturn(BigDecimal.valueOf(800));
        when(mortalityRecordRepository.sumMortalityByBatchId(1L)).thenReturn(50);
        when(saleRepository.sumRevenuByBatchId(1L)).thenReturn(BigDecimal.valueOf(30000));

        BatchCostBreakdownResponse response = batchCostService.calculateCost(1L, null);

        // chickenCost = 15000
        assertThat(response.getChickenCostDH()).isEqualByComparingTo("15000.00");

        // feedCost = (200 + 300) * 3.5 = 1750
        assertThat(response.getFeedCostDH()).isEqualByComparingTo("1750.00");

        // medicationCost = 800
        assertThat(response.getMedicationCostDH()).isEqualByComparingTo("800.00");

        // fixedCharges = 500 (default from @Value)
        assertThat(response.getFixedChargesDH()).isEqualByComparingTo("500.00");

        // totalCost = 15000 + 1750 + 800 + 500 = 18050
        assertThat(response.getTotalCostDH()).isEqualByComparingTo("18050.00");

        // aliveChickens = 1000 - 50 = 950
        assertThat(response.getAliveChickens()).isEqualTo(950);
        assertThat(response.getTotalMortality()).isEqualTo(50);

        // costPerChicken = 18050 / 950 = 19.00
        assertThat(response.getCostPerChickenDH()).isNotNull();

        // revenue = 30000, profit = 30000 - 18050 = 11950
        assertThat(response.getTotalRevenueDH()).isEqualByComparingTo("30000.00");
        assertThat(response.getEstimatedProfitDH()).isEqualByComparingTo("11950.00");

        // profitMarginPct = (11950 / 18050) * 100 = 66.20
        assertThat(response.getProfitMarginPct()).isNotNull();
        assertThat(response.getProfitMarginPct()).isPositive();

        // feedLines aggregated: 1 line for stockItemId=10, feedType=Demarrage
        assertThat(response.getFeedLines()).hasSize(1);
        BatchCostBreakdownResponse.FeedLineItem line = response.getFeedLines().get(0);
        assertThat(line.getStockItemId()).isEqualTo(10L);
        assertThat(line.getTotalQuantityKg()).isEqualByComparingTo("500.00");
        assertThat(line.getUnitPriceDH()).isEqualByComparingTo("3.5");
        assertThat(line.getSubtotalDH()).isEqualByComparingTo("1750.00");
    }

    @Test
    @DisplayName("doit utiliser l'override des charges fixes si fourni")
    void calculateCost_withFixedChargesOverride_shouldUseThatValue() {
        when(batchRepository.findByIdWithCreatedByAndBuilding(1L)).thenReturn(Optional.of(batch));
        when(feedingRecordRepository.findByBatchIdWithStockItem(1L)).thenReturn(List.of());
        when(healthRecordRepository.sumTreatmentCostByBatchId(1L)).thenReturn(BigDecimal.ZERO);
        when(mortalityRecordRepository.sumMortalityByBatchId(1L)).thenReturn(0);
        when(saleRepository.sumRevenuByBatchId(1L)).thenReturn(BigDecimal.ZERO);

        BatchCostBreakdownResponse response = batchCostService.calculateCost(1L, BigDecimal.valueOf(1200));

        assertThat(response.getFixedChargesDH()).isEqualByComparingTo("1200.00");
        // totalCost = 15 * 1000 + 0 + 0 + 1200 = 16200
        assertThat(response.getTotalCostDH()).isEqualByComparingTo("16200.00");
    }

    @Test
    @DisplayName("doit exclure les enregistrements d'alimentation sans prix unitaire")
    void calculateCost_feedRecordWithoutUnitPrice_shouldBeExcluded() {
        StockItem itemWithoutPrice = StockItem.builder()
                .id(20L)
                .name("Aliment sans prix")
                .type(StockType.FEED)
                .unit("kg")
                .unitPrice(null)
                .build();
        FeedingRecord feedingNoPrice = FeedingRecord.builder()
                .id(5L)
                .batch(batch)
                .stockItem(itemWithoutPrice)
                .feedType("Croissance")
                .quantity(BigDecimal.valueOf(100))
                .feedingDate(LocalDate.now().minusDays(2))
                .build();

        when(batchRepository.findByIdWithCreatedByAndBuilding(1L)).thenReturn(Optional.of(batch));
        when(feedingRecordRepository.findByBatchIdWithStockItem(1L)).thenReturn(List.of(feedingNoPrice));
        when(healthRecordRepository.sumTreatmentCostByBatchId(1L)).thenReturn(BigDecimal.ZERO);
        when(mortalityRecordRepository.sumMortalityByBatchId(1L)).thenReturn(0);
        when(saleRepository.sumRevenuByBatchId(1L)).thenReturn(BigDecimal.ZERO);

        BatchCostBreakdownResponse response = batchCostService.calculateCost(1L, BigDecimal.ZERO);

        assertThat(response.getFeedCostDH()).isEqualByComparingTo("0.00");
        assertThat(response.getFeedLines()).isEmpty();
    }

    @Test
    @DisplayName("doit retourner cout nul pour alimentation si aucun enregistrement")
    void calculateCost_noFeedingRecords_feedCostShouldBeZero() {
        when(batchRepository.findByIdWithCreatedByAndBuilding(1L)).thenReturn(Optional.of(batch));
        when(feedingRecordRepository.findByBatchIdWithStockItem(1L)).thenReturn(List.of());
        when(healthRecordRepository.sumTreatmentCostByBatchId(1L)).thenReturn(BigDecimal.ZERO);
        when(mortalityRecordRepository.sumMortalityByBatchId(1L)).thenReturn(0);
        when(saleRepository.sumRevenuByBatchId(1L)).thenReturn(BigDecimal.ZERO);

        BatchCostBreakdownResponse response = batchCostService.calculateCost(1L, BigDecimal.ZERO);

        assertThat(response.getFeedCostDH()).isEqualByComparingTo("0.00");
        assertThat(response.getFeedLines()).isEmpty();
    }

    @Test
    @DisplayName("doit retourner benefice negatif si le cout depasse les revenus")
    void calculateCost_profitNegative_whenCostExceedsRevenue() {
        when(batchRepository.findByIdWithCreatedByAndBuilding(1L)).thenReturn(Optional.of(batch));
        when(feedingRecordRepository.findByBatchIdWithStockItem(1L)).thenReturn(List.of());
        when(healthRecordRepository.sumTreatmentCostByBatchId(1L)).thenReturn(BigDecimal.ZERO);
        when(mortalityRecordRepository.sumMortalityByBatchId(1L)).thenReturn(0);
        // Revenue < purchase price
        when(saleRepository.sumRevenuByBatchId(1L)).thenReturn(BigDecimal.valueOf(5000));

        BatchCostBreakdownResponse response = batchCostService.calculateCost(1L, BigDecimal.ZERO);

        assertThat(response.getEstimatedProfitDH()).isNegative();
        assertThat(response.getProfitMarginPct()).isNegative();
    }

    @Test
    @DisplayName("doit lever ResourceNotFoundException si le lot n'existe pas")
    void calculateCost_batchNotFound_shouldThrow() {
        when(batchRepository.findByIdWithCreatedByAndBuilding(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> batchCostService.calculateCost(999L, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("doit gerer le cas ou purchasePrice est null (valeur zero)")
    void calculateCost_nullPurchasePrice_shouldDefaultToZero() {
        Batch batchNullPrice = Batch.builder()
                .id(2L)
                .batchNumber("BATCH-002")
                .chickenCount(500)
                .arrivalDate(LocalDate.now().minusDays(10))
                .status(BatchStatus.Active)
                .purchasePrice(null)
                .build();

        when(batchRepository.findByIdWithCreatedByAndBuilding(2L)).thenReturn(Optional.of(batchNullPrice));
        when(feedingRecordRepository.findByBatchIdWithStockItem(2L)).thenReturn(List.of());
        when(healthRecordRepository.sumTreatmentCostByBatchId(2L)).thenReturn(null);
        when(mortalityRecordRepository.sumMortalityByBatchId(2L)).thenReturn(null);
        when(saleRepository.sumRevenuByBatchId(2L)).thenReturn(null);

        BatchCostBreakdownResponse response = batchCostService.calculateCost(2L, BigDecimal.valueOf(200));

        assertThat(response.getChickenCostDH()).isEqualByComparingTo("0.00");
        assertThat(response.getMedicationCostDH()).isEqualByComparingTo("0.00");
        assertThat(response.getTotalMortality()).isEqualTo(0);
        assertThat(response.getTotalRevenueDH()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("doit agreger plusieurs lignes d'alimentation du meme item de stock")
    void calculateCost_multipleFeedRecordsSameStockItem_shouldAggregate() {
        FeedingRecord f1 = FeedingRecord.builder().id(1L).batch(batch)
                .stockItem(stockItemFeed).feedType("Demarrage")
                .quantity(BigDecimal.valueOf(100)).feedingDate(LocalDate.now().minusDays(3)).build();
        FeedingRecord f2 = FeedingRecord.builder().id(2L).batch(batch)
                .stockItem(stockItemFeed).feedType("Demarrage")
                .quantity(BigDecimal.valueOf(150)).feedingDate(LocalDate.now().minusDays(2)).build();
        FeedingRecord f3 = FeedingRecord.builder().id(3L).batch(batch)
                .stockItem(stockItemFeed).feedType("Demarrage")
                .quantity(BigDecimal.valueOf(50)).feedingDate(LocalDate.now().minusDays(1)).build();

        when(batchRepository.findByIdWithCreatedByAndBuilding(1L)).thenReturn(Optional.of(batch));
        when(feedingRecordRepository.findByBatchIdWithStockItem(1L)).thenReturn(List.of(f1, f2, f3));
        when(healthRecordRepository.sumTreatmentCostByBatchId(1L)).thenReturn(BigDecimal.ZERO);
        when(mortalityRecordRepository.sumMortalityByBatchId(1L)).thenReturn(0);
        when(saleRepository.sumRevenuByBatchId(1L)).thenReturn(BigDecimal.ZERO);

        BatchCostBreakdownResponse response = batchCostService.calculateCost(1L, BigDecimal.ZERO);

        // Aggregated into one line: qty = 300, cost = 300 * 3.5 = 1050
        assertThat(response.getFeedLines()).hasSize(1);
        assertThat(response.getFeedLines().get(0).getTotalQuantityKg()).isEqualByComparingTo("300.00");
        assertThat(response.getFeedLines().get(0).getSubtotalDH()).isEqualByComparingTo("1050.00");
        assertThat(response.getFeedCostDH()).isEqualByComparingTo("1050.00");
    }
}
