package com.pharmacy.management.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtil class.
 * Validates Requirements 1.3 and 11.4 for secure password handling.
 */
class PasswordUtilTest {
    
    @Test
    void testHashPassword_CreatesValidHash() {
        // Test that hashPassword creates a valid BCrypt hash
        String plainPassword = "password123";
        String hash = PasswordUtil.hashPassword(plainPassword);
        
        assertNotNull(hash, "Hash should not be null");
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"), 
                   "Hash should start with BCrypt identifier");
        assertNotEquals(plainPassword, hash, "Hash should not equal plaintext password");
    }
    
    @Test
    void testHashPassword_GeneratesUniqueHashes() {
        // Test that same password generates different hashes (due to salt)
        String plainPassword = "testPassword";
        String hash1 = PasswordUtil.hashPassword(plainPassword);
        String hash2 = PasswordUtil.hashPassword(plainPassword);
        
        assertNotEquals(hash1, hash2, "Same password should generate different hashes");
    }
    
    @Test
    void testVerifyPassword_CorrectPasswordReturnsTrue() {
        // Test that verifyPassword returns true for correct password
        String plainPassword = "mySecurePassword123";
        String hash = PasswordUtil.hashPassword(plainPassword);
        
        boolean result = PasswordUtil.verifyPassword(plainPassword, hash);
        assertTrue(result, "Correct password should verify successfully");
    }
    
    @Test
    void testVerifyPassword_IncorrectPasswordReturnsFalse() {
        // Test that verifyPassword returns false for incorrect password
        String correctPassword = "correctPassword";
        String incorrectPassword = "wrongPassword";
        String hash = PasswordUtil.hashPassword(correctPassword);
        
        boolean result = PasswordUtil.verifyPassword(incorrectPassword, hash);
        assertFalse(result, "Incorrect password should not verify");
    }
    
    @Test
    void testHashPassword_NullPasswordThrowsException() {
        // Test that null password throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword(null);
        }, "Null password should throw IllegalArgumentException");
    }
    
    @Test
    void testHashPassword_EmptyPasswordThrowsException() {
        // Test that empty password throws exception
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword("");
        }, "Empty password should throw IllegalArgumentException");
        
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.hashPassword("   ");
        }, "Whitespace password should throw IllegalArgumentException");
    }
    
    @Test
    void testVerifyPassword_NullPasswordThrowsException() {
        // Test that null parameters throw exception
        String hash = PasswordUtil.hashPassword("test");
        
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.verifyPassword(null, hash);
        }, "Null password should throw IllegalArgumentException");
        
        assertThrows(IllegalArgumentException.class, () -> {
            PasswordUtil.verifyPassword("test", null);
        }, "Null hash should throw IllegalArgumentException");
    }
    
    @Test
    void testVerifyPassword_WithSeedDataHash() {
        // Test verification with the actual seed data hash from schema.sql
        String seedPassword = "password123";
        String seedHash = "$2a$10$n8jz39mNztnIkAyXkVM1iuUSNW7afswd4zwa9ma0r7sEa1s7J2tW6";
        
        boolean result = PasswordUtil.verifyPassword(seedPassword, seedHash);
        assertTrue(result, "Seed password should verify against seed hash");
    }
}
