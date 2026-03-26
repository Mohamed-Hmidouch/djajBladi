package org.example.djajbladibackend.controller.client;

import jakarta.validation.Valid;
import org.example.djajbladibackend.dto.client.*;
import org.example.djajbladibackend.models.PaymentStatus;
import org.example.djajbladibackend.services.client.ClientPurchaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints pour le systeme d'achat client.
 *
 * Tous les endpoints sont proteges par le role CLIENT.
 * Le client est identifie automatiquement via le token JWT.
 */
@RestController
@RequestMapping("/api/client")
@PreAuthorize("hasRole('CLIENT')")
public class ClientPurchaseController {

    private final ClientPurchaseService purchaseService;

    public ClientPurchaseController(ClientPurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    // -----------------------------------------------------------------------
    // Catalogue
    // -----------------------------------------------------------------------

    /**
     * GET /api/client/batches
     * Liste les lots disponibles a l'achat.
     */
    @GetMapping("/batches")
    public ResponseEntity<List<AvailableBatchResponse>> getAvailableBatches() {
        return ResponseEntity.ok(purchaseService.getAvailableBatches());
    }

    /**
     * GET /api/client/batches/{batchId}
     * Detail d'un lot disponible.
     */
    @GetMapping("/batches/{batchId}")
    public ResponseEntity<AvailableBatchResponse> getAvailableBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(purchaseService.getAvailableBatch(batchId));
    }

    // -----------------------------------------------------------------------
    // Commandes
    // -----------------------------------------------------------------------

    /**
     * POST /api/client/orders
     * Passe une commande d'achat.
     */
    @PostMapping("/orders")
    public ResponseEntity<PurchaseOrderResponse> placeOrder(
            @Valid @RequestBody PurchaseOrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PurchaseOrderResponse order = purchaseService.placeOrder(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    /**
     * GET /api/client/orders
     * Liste toutes les commandes du client connecte.
     */
    @GetMapping("/orders")
    public ResponseEntity<List<PurchaseOrderResponse>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) PaymentStatus status) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<PurchaseOrderResponse> orders;
        if (status != null) {
            orders = purchaseService.getMyOrdersByStatus(userDetails.getUsername(), status);
        } else {
            orders = purchaseService.getMyOrders(userDetails.getUsername());
        }
        return ResponseEntity.ok(orders);
    }

    /**
     * GET /api/client/orders/{orderId}
     * Detail d'une commande specifique.
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PurchaseOrderResponse> getMyOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(purchaseService.getMyOrder(orderId, userDetails.getUsername()));
    }

    /**
     * DELETE /api/client/orders/{orderId}
     * Annule une commande en attente.
     */
    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<PurchaseOrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(purchaseService.cancelOrder(orderId, userDetails.getUsername()));
    }

    // -----------------------------------------------------------------------
    // Tableau de bord
    // -----------------------------------------------------------------------

    /**
     * GET /api/client/dashboard
     * Resume des commandes du client avec indicateurs.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ClientOrderSummary> getDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(purchaseService.getOrderSummary(userDetails.getUsername()));
    }
}
