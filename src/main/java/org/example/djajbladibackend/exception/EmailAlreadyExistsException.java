package org.example.djajbladibackend.exception;

/**
 * Thrown when attempting to create a user with an email that already exists.
 */
public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
