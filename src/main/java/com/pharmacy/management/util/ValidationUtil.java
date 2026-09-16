package com.pharmacy.management.util;

import com.pharmacy.management.model.Medicine;
import com.pharmacy.management.service.ValidationException;

import java.math.BigDecimal;

/**
 * Validation utility for input validation across the application.
 * Validates Requirements 5.2 and 11.2
 */
public class ValidationUtil {
    
    private ValidationUtil() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    /**
     * Validate medicine data before saving.
     * Requirement 5.2
     */
    public static void validateMedicine(Medicine medicine) throws ValidationException {
        if (medicine == null) {
            throw new ValidationException("Medicine cannot be null");
        }
        
        // Required fields
        if (isNullOrEmpty(medicine.getMedicineName())) {
            throw new ValidationException("Medicine name is required");
        }
        if (isNullOrEmpty(medicine.getGenericName())) {
            throw new ValidationException("Generic name is required");
        }
        if (isNullOrEmpty(medicine.getActiveIngredient())) {
            throw new ValidationException("Active ingredient is required");
        }
        if (isNullOrEmpty(medicine.getDosageForm())) {
            throw new ValidationException("Dosage form is required");
        }
        if (isNullOrEmpty(medicine.getStrength())) {
            throw new ValidationException("Strength is required");
        }
        
        // Validate price
        if (medicine.getSellingPrice() == null) {
            throw new ValidationException("Selling price is required");
        }
        if (medicine.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Selling price must be greater than zero");
        }
    }
    
    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
