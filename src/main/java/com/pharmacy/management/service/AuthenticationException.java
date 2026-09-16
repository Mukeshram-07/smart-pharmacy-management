package com.pharmacy.management.service;

/**
 * Exception thrown when authentication fails.
 * This exception is thrown for various authentication failures:
 * - Invalid credentials
 * - User not found
 * - Account inactive
 * - System errors during authentication
 * 
 * Validates Requirement 1.2: Display error messages for authentication failures
 */
public class AuthenticationException extends Exception {
    
    /**
     * Constructs a new AuthenticationException with the specified detail message.
     * 
     * @param message the detail message explaining the authentication failure
     */
    public AuthenticationException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new AuthenticationException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
