package org.example.djajbladibackend.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Requete de passage de commande par un client.
 */
@Data
public class PurchaseOrderRequest {

    @NotNull(message = "L'identifiant du lot est obligatoire")
    private Long batchId;

    @NotNull(message = "La quantite est obligatoire")
    @Positive(message = "La quantite doit etre positive")
    private Integer quantity;

    @NotBlank(message = "L'adresse de livraison est obligatoire")
    @Size(max = 2000, message = "L'adresse de livraison ne doit pas depasser 2000 caracteres")
    private String deliveryAddress;

    @Size(max = 2000, message = "Les notes ne doivent pas depasser 2000 caracteres")
    private String notes;
}
