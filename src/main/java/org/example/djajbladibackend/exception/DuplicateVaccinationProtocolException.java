package org.example.djajbladibackend.exception;

/**
 * Thrown when attempting to create a vaccination protocol that already exists
 * for the same (strain, vaccineName, dayOfLife) combination.
 * HTTP 409 Conflict.
 */
public class DuplicateVaccinationProtocolException extends RuntimeException {

    private final String strain;
    private final String vaccineName;
    private final Integer dayOfLife;

    public DuplicateVaccinationProtocolException(String strain, String vaccineName, Integer dayOfLife) {
        super(String.format(
            "Vaccination protocol already exists for strain='%s', vaccine='%s', dayOfLife=%d",
            strain, vaccineName, dayOfLife
        ));
        this.strain = strain;
        this.vaccineName = vaccineName;
        this.dayOfLife = dayOfLife;
    }

    public String getStrain() { return strain; }
    public String getVaccineName() { return vaccineName; }
    public Integer getDayOfLife() { return dayOfLife; }
}
