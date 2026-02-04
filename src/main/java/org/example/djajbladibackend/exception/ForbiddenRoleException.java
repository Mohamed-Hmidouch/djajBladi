package org.example.djajbladibackend.exception;

public class ForbiddenRoleException extends RuntimeException {

    public ForbiddenRoleException(String message) {
        super(message);
    }
}
