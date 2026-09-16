package com.pharmacy.management.service;

/**
 * Exception thrown when OCR processing fails.
 */
public class OCRException extends Exception {
    public OCRException(String message) {
        super(message);
    }
    
    public OCRException(String message, Throwable cause) {
        super(message, cause);
    }
}