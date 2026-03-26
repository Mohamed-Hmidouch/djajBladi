package org.example.djajbladibackend.dto.client;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resume global des commandes d'un client avec indicateurs metier.
 */
@Data
@Builder
public class ClientOrderSummary {

    /** Nombre total de commandes */
    private int totalOrders;

    /** Commandes en attente de paiement */
    private int pendingOrders;

    /** Commandes payees */
    private int paidOrders;

    /** Commandes annulees */
    private int cancelledOrders;

    /** Montant total des commandes payees */
    private BigDecimal totalSpent;

    /** Montant total des commandes en attente */
    private BigDecimal pendingAmount;

    /** Nombre total de poulets achetes (hors annulees) */
    private int totalChickensPurchased;

    /** Derniere commande passee */
    private PurchaseOrderResponse latestOrder;

    /** Historique des commandes recentes */
    private List<PurchaseOrderResponse> recentOrders;
}
