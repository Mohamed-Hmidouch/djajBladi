package org.example.djajbladibackend.services.fcr;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Service de courbes de poids standard par souche avicole.
 *
 * Principe : transformation d'un ageInDays -> poids vif estimé (kg/oiseau).
 * Source : tables de performance officielles des souches (Aviagen, Cobb-Vantress, Hubbard).
 *
 * Interpolation linéaire entre les points de contrôle connus.
 * Si la souche est inconnue, on utilise la courbe générique Ross 308.
 */
@Service
public class StrainWeightCurveService {


    private static final Map<String, NavigableMap<Integer, BigDecimal>> CURVES = Map.of(

        // Ross 308 — Aviagen Guide de Performance 2024
        // Source: https://www.aviagen.com/tech-center/performance-objectives
        "Ross 308", buildCurve(new double[][]{
            {0,  0.042},
            {7,  0.170},
            {14, 0.483},
            {21, 0.924},
            {28, 1.452},
            {35, 2.056},
            {42, 2.651},
            {49, 3.220}
        }),

        // Cobb 500 — Cobb-Vantress Guide de Performance 2024
        "Cobb 500", buildCurve(new double[][]{
            {0,  0.042},
            {7,  0.162},
            {14, 0.462},
            {21, 0.892},
            {28, 1.423},
            {35, 2.028},
            {42, 2.650},
            {49, 3.210}
        }),

        // Hubbard F15 — Hubbard Broiler Guide 2024
        "Hubbard", buildCurve(new double[][]{
            {0,  0.042},
            {7,  0.155},
            {14, 0.430},
            {21, 0.855},
            {28, 1.380},
            {35, 1.980},
            {42, 2.580},
            {49, 3.120}
        })
    );

    /**
     * Retourne le poids estimé (kg) par oiseau pour une souche et un âge donnés.
     * Si la souche est inconnue, utilise Ross 308 comme référence.
     *
     * @param strain     Souche (ex: "Ross 308", "Cobb 500", "Hubbard")
     * @param ageInDays  Age du lot en jours (>= 0)
     * @return Poids vif estimé en kg par oiseau
     */
    public BigDecimal getEstimatedWeightKg(String strain, int ageInDays) {
        if (ageInDays <= 0) {
            return BigDecimal.valueOf(0.042);
        }

        NavigableMap<Integer, BigDecimal> curve = resolveCurve(strain);

        // Cas exact
        if (curve.containsKey(ageInDays)) {
            return curve.get(ageInDays);
        }

        // Interpolation linéaire entre les deux points encadrants
        Map.Entry<Integer, BigDecimal> lower = curve.floorEntry(ageInDays);
        Map.Entry<Integer, BigDecimal> upper = curve.ceilingEntry(ageInDays);

        if (lower == null) return upper.getValue();
        if (upper == null) return lower.getValue();

        // t = position relative entre lower et upper
        double t = (double)(ageInDays - lower.getKey()) / (upper.getKey() - lower.getKey());
        double interpolated = lower.getValue().doubleValue()
                + t * (upper.getValue().doubleValue() - lower.getValue().doubleValue());

        return BigDecimal.valueOf(interpolated).setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Résolution de la courbe : normalisation du nom de souche, fallback sur Ross 308.
     */
    private NavigableMap<Integer, BigDecimal> resolveCurve(String strain) {
        if (strain == null) {
            return CURVES.get("Ross 308");
        }
        String normalized = strain.trim();
        // Lookup exact
        if (CURVES.containsKey(normalized)) {
            return CURVES.get(normalized);
        }
        // Lookup partiel (ex: "ross 308" -> "Ross 308")
        for (Map.Entry<String, NavigableMap<Integer, BigDecimal>> entry : CURVES.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(normalized)) {
                return entry.getValue();
            }
        }
        // Fallback Ross 308
        return CURVES.get("Ross 308");
    }

    private static NavigableMap<Integer, BigDecimal> buildCurve(double[][] points) {
        NavigableMap<Integer, BigDecimal> map = new TreeMap<>();
        for (double[] point : points) {
            map.put((int) point[0], BigDecimal.valueOf(point[1]));
        }
        return map;
    }
}
