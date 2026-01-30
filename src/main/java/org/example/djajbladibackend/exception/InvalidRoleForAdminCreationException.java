package org.example.djajbladibackend.exception;

/**
 * Thrown when creating a user via admin API with a role other than Ouvrier or Veterinaire.
 */
public class InvalidRoleForAdminCreationException extends RuntimeException {

    public InvalidRoleForAdminCreationException(String message) {
        super(message);
    }
}
