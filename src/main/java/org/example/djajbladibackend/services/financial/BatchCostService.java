package org.example.djajbladibackend.services.financial;

import lombok.extern.slf4j.Slf4j;
import org.example.djajbladibackend.dto.financial.BatchCostBreakdownResponse;
import org.example.djajbladibackend.exception.ResourceNotFoundException;
import org.example.djajbladibackend.models.Batch;
import org.example.djajbladibackend.models.FeedingRecord;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.DailyMortalityRecordRepository;
import org.example.djajbladibackend.repository.FeedingRecordRepository;
import org.example.djajbladibackend.repository.HealthRecordRepository;
import org.example.djajbladibackend.repository.SaleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calcule le cout de revient dynamique d'un lot de poules.
 *
 * Formula:
 *   totalCost = chickenCost + feedCost + medicationCost + fixedCharges
 *
 * - chickenCost   : Batch.purchasePrice (prix total du lot de poussins)
 * - feedCost      : SUM(feeding_record.quantity * stock_item.unit_price)
 *                   Les enregistrements sans prix unitaire sont exclus du calcul
 *                   (stock_item.unit_price IS NULL).
 * - medicationCost: SUM(health_record.treatment_cost)
 * - fixedCharges  : valeur configuree (eau, electricite, main d'oeuvre)
 *                   overrideable par parametre admin.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class BatchCostService {

    private final BatchRepository batchRepository;
    private final FeedingRecordRepository feedingRecordRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final DailyMortalityRecordRepository mortalityRecordRepository;
    private final SaleRepository saleRepository;

    @Value("${app.financial.fixed-charges-per-batch-dh:0}")
    private BigDecimal defaultFixedChargesDH;

    public BatchCostService(BatchRepository batchRepository,
                            FeedingRecordRepository feedingRecordRepository,
                            HealthRecordRepository healthRecordRepository,
                            DailyMortalityRecordRepository mortalityRecordRepository,
                            SaleRepository saleRepository) {
        this.batchRepository = batchRepository;
        this.feedingRecordRepository = feedingRecordRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.mortalityRecordRepository = mortalityRecordRepository;
        this.saleRepository = saleRepository;
    }

    /**
     * Calcule le cout de revient complet d'un lot.
     *
     * @param batchId       identifiant du lot
     * @param fixedChargesOverride charges fixes a utiliser (null = valeur configuree)
     * @return decomposition complete du cout de revient
     */
    public BatchCostBreakdownResponse calculateCost(Long batchId, BigDecimal fixedChargesOverride) {
        Batch batch = batchRepository.findByIdWithCreatedByAndBuilding(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        // --- 1. Investissement initial : unitPurchasePrice * initialCount ---
        // Batch.purchasePrice = prix UNITAIRE par poussin (ex. 3.30 DH/tete)
        // initialInvestment = 3.30 * 6000 = 19 800 DH
        int initialCount = batch.getChickenCount() != null ? batch.getChickenCount() : 0;
        BigDecimal unitPurchasePrice = batch.getPurchasePrice() != null
                ? batch.getPurchasePrice()
                : BigDecimal.ZERO;
        BigDecimal initialInvestment = unitPurchasePrice.multiply(BigDecimal.valueOf(initialCount));
        BigDecimal chickenCostDH = initialInvestment;

        // --- 2. Cout alimentation ---
        List<FeedingRecord> feedRecords = feedingRecordRepository.findByBatchIdWithStockItem(batchId);
        BigDecimal feedCostDH = BigDecimal.ZERO;
        Map<String, FeedAggregation> feedByItem = new LinkedHashMap<>();

        for (FeedingRecord record : feedRecords) {
            if (record.getStockItem() == null || record.getStockItem().getUnitPrice() == null) {
                log.debug("Feeding record id={} excluded from cost: no unit price on stock item", record.getId());
                continue;
            }
            BigDecimal lineCost = record.getQuantity().multiply(record.getStockItem().getUnitPrice());
            feedCostDH = feedCostDH.add(lineCost);

            String key = record.getStockItem().getId() + "|" + record.getFeedType();
            feedByItem.merge(
                    key,
                    new FeedAggregation(
                            record.getStockItem().getId(),
                            record.getStockItem().getName(),
                            record.getFeedType(),
                            record.getQuantity(),
                            record.getStockItem().getUnitPrice(),
                            lineCost
                    ),
                    (a, b) -> new FeedAggregation(
                            a.stockItemId,
                            a.stockItemName,
                            a.feedType,
                            a.totalQty.add(b.totalQty),
                            a.unitPrice,
                            a.subtotal.add(b.subtotal)
                    )
            );
        }

        List<BatchCostBreakdownResponse.FeedLineItem> feedLines = new ArrayList<>();
        for (FeedAggregation agg : feedByItem.values()) {
            feedLines.add(BatchCostBreakdownResponse.FeedLineItem.builder()
                    .stockItemId(agg.stockItemId)
                    .stockItemName(agg.stockItemName)
                    .feedType(agg.feedType)
                    .totalQuantityKg(agg.totalQty.setScale(2, RoundingMode.HALF_UP))
                    .unitPriceDH(agg.unitPrice)
                    .subtotalDH(agg.subtotal.setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        // --- 3. Cout medicaments/veterinaire ---
        BigDecimal medicationCostDH = healthRecordRepository.sumTreatmentCostByBatchId(batchId);
        if (medicationCostDH == null) {
            medicationCostDH = BigDecimal.ZERO;
        }

        // --- 4. Charges fixes ---
        BigDecimal fixedChargesDH = fixedChargesOverride != null
                ? fixedChargesOverride
                : defaultFixedChargesDH;
        if (fixedChargesDH == null) {
            fixedChargesDH = BigDecimal.ZERO;
        }

        // --- 5. Total cout de revient ---
        BigDecimal totalCostDH = chickenCostDH
                .add(feedCostDH)
                .add(medicationCostDH)
                .add(fixedChargesDH)
                .setScale(2, RoundingMode.HALF_UP);

        // --- 6. Mortalite & poules vivantes ---
        Integer totalMortality = mortalityRecordRepository.sumMortalityByBatchId(batchId);
        if (totalMortality == null) {
            totalMortality = 0;
        }
        int aliveChickens = Math.max(0, initialCount - totalMortality);

        // --- 7. Cout par poussin vivant ---
        BigDecimal costPerChickenDH = null;
        if (aliveChickens > 0 && totalCostDH.compareTo(BigDecimal.ZERO) > 0) {
            costPerChickenDH = totalCostDH
                    .divide(BigDecimal.valueOf(aliveChickens), 2, RoundingMode.HALF_UP);
        }

        // --- 8. Revenues des ventes ---
        BigDecimal totalRevenueDH = saleRepository.sumRevenuByBatchId(batchId);
        if (totalRevenueDH == null) {
            totalRevenueDH = BigDecimal.ZERO;
        }
        totalRevenueDH = totalRevenueDH.setScale(2, RoundingMode.HALF_UP);

        BigDecimal estimatedProfitDH = totalRevenueDH.subtract(totalCostDH).setScale(2, RoundingMode.HALF_UP);

        BigDecimal profitMarginPct = null;
        if (totalCostDH.compareTo(BigDecimal.ZERO) > 0) {
            profitMarginPct = estimatedProfitDH
                    .divide(totalCostDH, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        log.info("Cost calculated for batchId={}: unitPrice={}, initialCount={}, chicken={}, feed={}, medication={}, fixed={}, total={}",
                batchId, unitPurchasePrice, initialCount,
                chickenCostDH, feedCostDH.setScale(2, RoundingMode.HALF_UP),
                medicationCostDH.setScale(2, RoundingMode.HALF_UP), fixedChargesDH, totalCostDH);

        return BatchCostBreakdownResponse.builder()
                .batchId(batch.getId())
                .batchNumber(batch.getBatchNumber())
                .strain(batch.getStrain())
                .arrivalDate(batch.getArrivalDate())
                .status(batch.getStatus())
                .initialChickenCount(initialCount)
                .aliveChickens(aliveChickens)
                .totalMortality(totalMortality)
                .chickenCostDH(chickenCostDH.setScale(2, RoundingMode.HALF_UP))
                .feedCostDH(feedCostDH.setScale(2, RoundingMode.HALF_UP))
                .medicationCostDH(medicationCostDH.setScale(2, RoundingMode.HALF_UP))
                .fixedChargesDH(fixedChargesDH.setScale(2, RoundingMode.HALF_UP))
                .totalCostDH(totalCostDH)
                .costPerChickenDH(costPerChickenDH)
                .totalRevenueDH(totalRevenueDH)
                .estimatedProfitDH(estimatedProfitDH)
                .profitMarginPct(profitMarginPct)
                .feedLines(feedLines)
                .build();
    }

    /** DTO interne pour l'aggregation des lignes d'alimentation. */
    private record FeedAggregation(
            Long stockItemId,
            String stockItemName,
            String feedType,
            BigDecimal totalQty,
            BigDecimal unitPrice,
            BigDecimal subtotal
    ) {}
}
