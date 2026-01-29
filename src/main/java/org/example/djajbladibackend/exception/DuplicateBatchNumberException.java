package org.example.djajbladibackend.exception;

/**
 * Thrown when creating a batch with a batch number that already exists.
 */
public class DuplicateBatchNumberException extends RuntimeException {

    public DuplicateBatchNumberException(String message) {
        super(message);
    }
}
