package org.example.djajbladibackend.dto.financial;

import lombok.Builder;
import lombok.Data;
import org.example.djajbladibackend.models.BatchStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Decomposition complete du cout de revient d'un lot.
 *
 * Formula:
 *   totalCostDH = chickenCostDH + feedCostDH + medicationCostDH + fixedChargesDH
 *
 * Revenues (si ventes enregistrees):
 *   totalRevenueDH    = SUM(sale.total_price) pour les ventes non annulees
 *   estimatedProfitDH = totalRevenueDH - totalCostDH
 */
@Data
@Builder
public class BatchCostBreakdownResponse {

    private Long batchId;
    private String batchNumber;
    private String strain;
    private LocalDate arrivalDate;
    private BatchStatus status;
    private Integer initialChickenCount;
    private Integer aliveChickens;
    private Integer totalMortality;

    // --- Composantes du cout de revient ---

    /** Prix d'achat des poussins (Batch.purchasePrice) */
    private BigDecimal chickenCostDH;

    /** Cout alimentation = SUM(feeding_record.quantity * stock_item.unit_price) */
    private BigDecimal feedCostDH;

    /** Cout medicaments/veterinaire = SUM(health_record.treatment_cost) */
    private BigDecimal medicationCostDH;

    /**
     * Charges fixes (eau, electricite, main d'oeuvre).
     * Valeur configuree via app.financial.fixed-charges-per-batch-dh.
     * Peut etre overridee par l'admin via le parametre de la requete.
     */
    private BigDecimal fixedChargesDH;

    /** Cout total = chickenCost + feedCost + medicationCost + fixedCharges */
    private BigDecimal totalCostDH;

    /** Cout de revient par poussin vivant (totalCostDH / aliveChickens) */
    private BigDecimal costPerChickenDH;

    // --- Revenues ---

    /** Revenu total des ventes non annulees = SUM(sale.total_price) */
    private BigDecimal totalRevenueDH;

    /** Benefice estime = totalRevenueDH - totalCostDH (peut etre negatif) */
    private BigDecimal estimatedProfitDH;

    /** Marge en pourcentage = (estimatedProfitDH / totalCostDH) * 100 */
    private BigDecimal profitMarginPct;

    // --- Detail par ligne d'alimentation ---
    private List<FeedLineItem> feedLines;

    @Data
    @Builder
    public static class FeedLineItem {
        private Long stockItemId;
        private String stockItemName;
        private String feedType;
        /** Quantite totale consommee (kg) */
        private BigDecimal totalQuantityKg;
        /** Prix unitaire au moment du calcul (DH/kg) */
        private BigDecimal unitPriceDH;
        /** Sous-total = totalQuantityKg * unitPriceDH */
        private BigDecimal subtotalDH;
    }
}
