package org.example.djajbladibackend.exception;

/**
 * Thrown when a vaccination protocol is not found for a given strain / vaccine name.
 * HTTP 404 Not Found.
 */
public class VaccinationProtocolNotFoundException extends ResourceNotFoundException {

    public VaccinationProtocolNotFoundException(Long id) {
        super("VaccinationProtocol", "id", id);
    }

    public VaccinationProtocolNotFoundException(String strain, String vaccineName) {
        super(String.format("Protocole de vaccination introuvable pour la souche '%s', vaccin '%s'.", strain, vaccineName));
    }
}
