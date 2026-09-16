package com.pharmacy.management.util;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Utility class for password hashing and verification using BCrypt.
 * Validates Requirements 1.3 (Secure Password Storage) and 11.4 (Secure Hashing Algorithm).
 * 
 * This class uses BCrypt with a work factor of 12 for secure password hashing.
 * BCrypt is a slow hashing algorithm designed specifically for passwords.
 */
public class PasswordUtil {
    
    /**
     * BCrypt work factor (cost factor).
     * Higher values make hashing slower and more secure.
     * Recommended: 10-12 for production systems.
     */
    private static final int BCRYPT_COST = 12;
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private PasswordUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Hash a plaintext password using BCrypt with work factor 12.
     * 
     * Requirements:
     * - 1.3: THE System SHALL store passwords as Password_Hash values in the Database
     * - 11.4: THE System SHALL hash passwords using a secure hashing algorithm
     * 
     * @param plainPassword the plaintext password to hash
     * @return BCrypt hash of the password
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        // Hash the password with BCrypt using cost factor 12
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }
    
    /**
     * Verify a plaintext password against a BCrypt hash.
     * 
     * This method is used during authentication to check if the provided password
     * matches the stored hash.
     * 
     * @param plainPassword the plaintext password to verify
     * @param passwordHash the BCrypt hash to verify against
     * @return true if the password matches the hash, false otherwise
     * @throws IllegalArgumentException if either parameter is null
     */
    public static boolean verifyPassword(String plainPassword, String passwordHash) {
        if (plainPassword == null || passwordHash == null) {
            throw new IllegalArgumentException("Password and hash cannot be null");
        }
        
        // Verify the password against the hash
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), passwordHash);
        return result.verified;
    }
}
