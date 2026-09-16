package com.pharmacy.management.service;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends Exception {
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
