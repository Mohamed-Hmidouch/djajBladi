package org.example.djajbladibackend.exception;

/**
 * Thrown when registration is not allowed for the requested role (e.g. Admin).
 * Admins are only created via DB seed/migration or by an existing admin.
 */
public class RegistrationNotAllowedException extends RuntimeException {

    public RegistrationNotAllowedException(String message) {
        super(message);
    }
}
