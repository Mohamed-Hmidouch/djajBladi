package org.example.djajbladibackend.dto.client;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour les lots disponibles a l'achat par les clients.
 * Expose uniquement les informations pertinentes pour l'achat,
 * sans reveler les couts internes ou donnees sensibles.
 */
@Data
@Builder
public class AvailableBatchResponse {

    private Long batchId;
    private String batchNumber;
    private String strain;
    private Integer availableQuantity;
    private BigDecimal pricePerUnit;
    private Integer minimumOrderQuantity;
    private LocalDate arrivalDate;
    private Integer ageInDays;
    private String buildingName;

    /** Prix estime pour la quantite minimum */
    private BigDecimal minimumOrderPrice;
}
