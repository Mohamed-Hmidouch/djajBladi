package org.example.djajbladibackend.exception;

public class MortalityExceedsBatchSizeException extends RuntimeException {

    public MortalityExceedsBatchSizeException(String message) {
        super(message);
    }
}
