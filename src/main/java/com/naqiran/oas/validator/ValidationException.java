package com.naqiran.oas.validator;

public class ValidationException extends RuntimeException {
    public ValidationException(final String message) {
        super(message);
    }
}
