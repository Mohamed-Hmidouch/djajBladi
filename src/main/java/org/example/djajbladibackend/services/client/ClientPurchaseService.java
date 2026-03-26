package org.example.djajbladibackend.services.client;

import lombok.extern.slf4j.Slf4j;
import org.example.djajbladibackend.dto.client.*;
import org.example.djajbladibackend.exception.*;
import org.example.djajbladibackend.models.*;
import org.example.djajbladibackend.models.enums.RoleEnum;
import org.example.djajbladibackend.repository.BatchRepository;
import org.example.djajbladibackend.repository.HealthRecordRepository;
import org.example.djajbladibackend.repository.SaleRepository;
import org.example.djajbladibackend.repository.auth.UserRepository;
import org.example.djajbladibackend.services.financial.BatchCostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service metier pour les achats clients.
 *
 * Regles metier appliquees :
 * - Seuls les lots READY_FOR_SALE avec stock > 0 et prix defini sont visibles
 * - Verification des periodes de retrait (withdrawal) avant toute vente
 * - Validation de la quantite minimum par lot
 * - Decrementation atomique du stock (evite les surventes concurrentes)
 * - Un client ne peut annuler que ses propres commandes en statut Pending
 * - Lors de l'annulation, le stock est restaure au lot
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class ClientPurchaseService {

    private final BatchRepository batchRepository;
    private final SaleRepository saleRepository;
    private final UserRepository userRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final BatchCostService batchCostService;

    public ClientPurchaseService(BatchRepository batchRepository,
                                  SaleRepository saleRepository,
                                  UserRepository userRepository,
                                  HealthRecordRepository healthRecordRepository,
                                  BatchCostService batchCostService) {
        this.batchRepository = batchRepository;
        this.saleRepository = saleRepository;
        this.userRepository = userRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.batchCostService = batchCostService;
    }

    // -----------------------------------------------------------------------
    // 1. Catalogue : lots disponibles a l'achat
    // -----------------------------------------------------------------------

    /**
     * Liste les lots disponibles a la vente.
     * Filtre : status = READY_FOR_SALE, currentCount > 0, sellingPricePerUnit non null,
     * et aucune periode de retrait active.
     */
    public List<AvailableBatchResponse> getAvailableBatches() {
        List<Batch> batches = batchRepository.findAvailableForSale();

        return batches.stream()
                .filter(b -> isWithdrawalClear(b.getId()))
                .map(this::toAvailableBatchResponse)
                .collect(Collectors.toList());
    }

    /**
     * Detail d'un lot disponible. Leve une exception si le lot n'est pas en vente.
     */
    public AvailableBatchResponse getAvailableBatch(Long batchId) {
        Batch batch = batchRepository.findByIdWithCreatedByAndBuilding(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", batchId));

        if (batch.getStatus() != BatchStatus.READY_FOR_SALE) {
            throw new BatchNotForSaleException(batchId, batch.getStatus().name());
        }
        if (batch.getSellingPricePerUnit() == null) {
            throw new BatchNotForSaleException(batchId, "Prix de vente non defini");
        }
        if (!isWithdrawalClear(batchId)) {
            throw new BatchNotForSaleException(batchId, "Periode de retrait active");
        }

        return toAvailableBatchResponse(batch);
    }

    // -----------------------------------------------------------------------
    // 2. Passage de commande
    // -----------------------------------------------------------------------

    /**
     * Passe une commande d'achat.
     *
     * Regles metier :
     * 1. Le lot doit etre READY_FOR_SALE
     * 2. Aucune periode de retrait active
     * 3. La quantite doit respecter le minimum du lot
     * 4. La quantite ne doit pas depasser le stock disponible
     * 5. Le stock est decremente de maniere atomique (protection concurrence)
     * 6. Le statut initial de la commande est Pending
     */
    @Transactional
    public PurchaseOrderResponse placeOrder(PurchaseOrderRequest request, String clientEmail) {
        User client = userRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + clientEmail));

        Batch batch = batchRepository.findByIdWithCreatedByAndBuilding(request.getBatchId())
                .orElseThrow(() -> new ResourceNotFoundException("Batch", "id", request.getBatchId()));

        // Regle 1 : lot en vente
        if (batch.getStatus() != BatchStatus.READY_FOR_SALE) {
            throw new BatchNotForSaleException(batch.getId(), batch.getStatus().name());
        }

        // Regle 2 : pas de periode de retrait active
        if (!isWithdrawalClear(batch.getId())) {
            LocalDate expiration = healthRecordRepository.findLatestWithdrawalExpiration(batch.getId());
            throw new WithdrawalPeriodActiveException(batch.getId(),
                    expiration != null ? expiration : LocalDate.now().plusDays(1));
        }

        // Regle 3 : quantite minimum
        int minQty = batch.getMinimumOrderQuantity() != null ? batch.getMinimumOrderQuantity() : 1;
        if (request.getQuantity() < minQty) {
            throw new MinimumOrderQuantityException(batch.getId(), request.getQuantity(), minQty);
        }

        // Regle 4 : stock suffisant (calcul avec ventes deja enregistrees)
        int soldAlready = saleRepository.sumSoldQuantityByBatchId(batch.getId());
        int effectiveStock = batch.getCurrentCount() - soldAlready;
        if (request.getQuantity() > effectiveStock) {
            throw new OrderQuantityExceedsStockException(batch.getId(), request.getQuantity(), effectiveStock);
        }

        // Regle 5 : decrementation atomique
        BigDecimal unitPrice = batch.getSellingPricePerUnit();
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(request.getQuantity()))
                .setScale(2, RoundingMode.HALF_UP);

        int updated = batchRepository.decrementCurrentCount(batch.getId(), request.getQuantity());
        if (updated == 0) {
            throw new OrderQuantityExceedsStockException(batch.getId(), request.getQuantity(),
                    batch.getCurrentCount());
        }

        // Creation de la vente
        Sale sale = Sale.builder()
                .batch(batch)
                .client(client)
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .saleDate(LocalDate.now())
                .paymentStatus(PaymentStatus.Pending)
                .deliveryAddress(request.getDeliveryAddress())
                .notes(request.getNotes())
                .recordedBy(client)
                .build();

        Sale saved = saleRepository.save(sale);

        // Si le lot est epuise, passer en SOLD
        Batch refreshedBatch = batchRepository.findById(batch.getId()).orElse(batch);
        if (refreshedBatch.getCurrentCount() != null && refreshedBatch.getCurrentCount() <= 0) {
            refreshedBatch.setStatus(BatchStatus.SOLD);
            batchRepository.save(refreshedBatch);
            log.info("Lot {} epuise apres commande, statut passe a SOLD", batch.getBatchNumber());
        }

        log.info("Commande {} creee : client={}, lot={}, qty={}, total={} DH",
                saved.getId(), clientEmail, batch.getBatchNumber(), request.getQuantity(), totalPrice);

        return toOrderResponse(saved);
    }

    // -----------------------------------------------------------------------
    // 3. Consultation des commandes
    // -----------------------------------------------------------------------

    /**
     * Liste toutes les commandes d'un client, triees par date decroissante.
     */
    public List<PurchaseOrderResponse> getMyOrders(String clientEmail) {
        User client = findClient(clientEmail);
        return saleRepository.findByClientIdOrderByCreatedAtDesc(client.getId())
                .stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Liste les commandes d'un client filtrees par statut de paiement.
     */
    public List<PurchaseOrderResponse> getMyOrdersByStatus(String clientEmail, PaymentStatus status) {
        User client = findClient(clientEmail);
        return saleRepository.findByClientIdAndPaymentStatusWithRelations(client.getId(), status)
                .stream()
                .map(this::toOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Detail d'une commande. Verifie que la commande appartient bien au client.
     */
    public PurchaseOrderResponse getMyOrder(Long orderId, String clientEmail) {
        User client = findClient(clientEmail);
        Sale sale = saleRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", orderId));

        if (!sale.getClient().getId().equals(client.getId())) {
            throw new OrderOwnershipException(orderId);
        }

        return toOrderResponse(sale);
    }

    // -----------------------------------------------------------------------
    // 4. Annulation de commande
    // -----------------------------------------------------------------------

    /**
     * Annule une commande en attente (Pending).
     *
     * Regles :
     * - Seul le proprietaire peut annuler
     * - Seules les commandes Pending peuvent etre annulees
     * - Le stock est restaure au lot
     * - Si le lot etait SOLD, il repasse en READY_FOR_SALE
     */
    @Transactional
    public PurchaseOrderResponse cancelOrder(Long orderId, String clientEmail) {
        User client = findClient(clientEmail);
        Sale sale = saleRepository.findByIdWithRelations(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", "id", orderId));

        // Verification de propriete
        if (!sale.getClient().getId().equals(client.getId())) {
            throw new OrderOwnershipException(orderId);
        }

        // Seules les commandes Pending sont annulables
        if (sale.getPaymentStatus() != PaymentStatus.Pending) {
            throw new OrderNotCancellableException(orderId, sale.getPaymentStatus().name());
        }

        // Annulation
        sale.setPaymentStatus(PaymentStatus.Cancelled);
        saleRepository.save(sale);

        // Restauration du stock
        Batch batch = sale.getBatch();
        batch.setCurrentCount(batch.getCurrentCount() + sale.getQuantity());

        // Si le lot etait epuise (SOLD), le remettre en vente
        if (batch.getStatus() == BatchStatus.SOLD) {
            batch.setStatus(BatchStatus.READY_FOR_SALE);
            log.info("Lot {} remis en vente apres annulation de commande {}", batch.getBatchNumber(), orderId);
        }
        batchRepository.save(batch);

        log.info("Commande {} annulee par client {}, {} poulets restaures au lot {}",
                orderId, clientEmail, sale.getQuantity(), batch.getBatchNumber());

        return toOrderResponse(sale);
    }

    // -----------------------------------------------------------------------
    // 5. Tableau de bord client
    // -----------------------------------------------------------------------

    /**
     * Resume des commandes du client avec indicateurs metier.
     */
    public ClientOrderSummary getOrderSummary(String clientEmail) {
        User client = findClient(clientEmail);
        Long clientId = client.getId();

        long totalOrders = saleRepository.countByClientIdAndPaymentStatus(clientId, PaymentStatus.Pending)
                + saleRepository.countByClientIdAndPaymentStatus(clientId, PaymentStatus.Paid)
                + saleRepository.countByClientIdAndPaymentStatus(clientId, PaymentStatus.Cancelled);

        long pendingOrders = saleRepository.countByClientIdAndPaymentStatus(clientId, PaymentStatus.Pending);
        long paidOrders = saleRepository.countByClientIdAndPaymentStatus(clientId, PaymentStatus.Paid);
        long cancelledOrders = saleRepository.countByClientIdAndPaymentStatus(clientId, PaymentStatus.Cancelled);

        BigDecimal totalSpent = saleRepository.sumTotalSpentByClientId(clientId);
        BigDecimal pendingAmount = saleRepository.sumPendingAmountByClientId(clientId);
        int totalChickens = saleRepository.sumQuantityByClientId(clientId);

        List<Sale> recentSales = saleRepository.findByClientIdOrderByCreatedAtDesc(clientId);
        List<PurchaseOrderResponse> recentOrders = recentSales.stream()
                .limit(5)
                .map(this::toOrderResponse)
                .collect(Collectors.toList());

        PurchaseOrderResponse latestOrder = recentOrders.isEmpty() ? null : recentOrders.get(0);

        return ClientOrderSummary.builder()
                .totalOrders((int) totalOrders)
                .pendingOrders((int) pendingOrders)
                .paidOrders((int) paidOrders)
                .cancelledOrders((int) cancelledOrders)
                .totalSpent(totalSpent != null ? totalSpent.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .pendingAmount(pendingAmount != null ? pendingAmount.setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .totalChickensPurchased(totalChickens)
                .latestOrder(latestOrder)
                .recentOrders(recentOrders)
                .build();
    }

    // -----------------------------------------------------------------------
    // 6. Suggestion de prix (interne, appele par le service admin)
    // -----------------------------------------------------------------------

    /**
     * Calcule un prix de vente suggere basé sur le cout de revient + marge.
     * Formule : (cout total / poulets vivants) * (1 + margePercent/100)
     *
     * @param batchId       identifiant du lot
     * @param marginPercent marge souhaitee en pourcentage (ex: 20 pour 20%)
     * @return prix suggere par poulet en DH
     */
    public BigDecimal suggestSellingPrice(Long batchId, BigDecimal marginPercent) {
        var costBreakdown = batchCostService.calculateCost(batchId, null);
        BigDecimal costPerChicken = costBreakdown.getCostPerChickenDH();
        if (costPerChicken == null || costPerChicken.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Impossible de calculer le prix suggere pour le lot {} : cout par poulet = 0", batchId);
            return BigDecimal.ZERO;
        }

        BigDecimal margin = BigDecimal.ONE.add(marginPercent.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
        return costPerChicken.multiply(margin).setScale(2, RoundingMode.HALF_UP);
    }

    // -----------------------------------------------------------------------
    // Methodes privees
    // -----------------------------------------------------------------------

    private User findClient(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private boolean isWithdrawalClear(Long batchId) {
        List<HealthRecord> activeWithdrawals = healthRecordRepository.findActiveWithdrawalPeriods(batchId);
        return activeWithdrawals.isEmpty();
    }

    private AvailableBatchResponse toAvailableBatchResponse(Batch batch) {
        int soldAlready = saleRepository.sumSoldQuantityByBatchId(batch.getId());
        int availableQty = Math.max(0, batch.getCurrentCount() - soldAlready);
        int minQty = batch.getMinimumOrderQuantity() != null ? batch.getMinimumOrderQuantity() : 1;
        BigDecimal price = batch.getSellingPricePerUnit();
        long ageInDays = ChronoUnit.DAYS.between(batch.getArrivalDate(), LocalDate.now());

        return AvailableBatchResponse.builder()
                .batchId(batch.getId())
                .batchNumber(batch.getBatchNumber())
                .strain(batch.getStrain())
                .availableQuantity(availableQty)
                .pricePerUnit(price)
                .minimumOrderQuantity(minQty)
                .arrivalDate(batch.getArrivalDate())
                .ageInDays((int) ageInDays)
                .buildingName(batch.getBuilding() != null ? batch.getBuilding().getName() : null)
                .minimumOrderPrice(price != null ? price.multiply(BigDecimal.valueOf(minQty))
                        .setScale(2, RoundingMode.HALF_UP) : null)
                .build();
    }

    private PurchaseOrderResponse toOrderResponse(Sale sale) {
        return PurchaseOrderResponse.builder()
                .orderId(sale.getId())
                .batchNumber(sale.getBatch() != null ? sale.getBatch().getBatchNumber() : null)
                .strain(sale.getBatch() != null ? sale.getBatch().getStrain() : null)
                .quantity(sale.getQuantity())
                .unitPrice(sale.getUnitPrice())
                .totalPrice(sale.getTotalPrice())
                .saleDate(sale.getSaleDate())
                .paymentStatus(sale.getPaymentStatus())
                .deliveryAddress(sale.getDeliveryAddress())
                .notes(sale.getNotes())
                .createdAt(sale.getCreatedAt())
                .build();
    }
}
