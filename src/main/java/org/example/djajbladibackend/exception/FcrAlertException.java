package org.example.djajbladibackend.exception;

/**
 * Levée lorsque l'ICR (Indice de Consommation / FCR) d'un lot dépasse le seuil configuré.
 * Non propagée comme erreur HTTP — utilisée pour le logging et le flagging interne.
 */
public class FcrAlertException extends RuntimeException {

    private final Long batchId;
    private final double fcrValue;
    private final double threshold;

    public FcrAlertException(Long batchId, double fcrValue, double threshold) {
        super(String.format("Alerte ICR pour le lot %d : ICR=%.3f dépasse le seuil de %.2f.", batchId, fcrValue, threshold));
        this.batchId = batchId;
        this.fcrValue = fcrValue;
        this.threshold = threshold;
    }

    public Long getBatchId() {
        return batchId;
    }

    public double getFcrValue() {
        return fcrValue;
    }

    public double getThreshold() {
        return threshold;
    }
}
