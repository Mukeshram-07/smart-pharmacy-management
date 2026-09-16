package com.pharmacy.management.util;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.junit.jupiter.api.Test;

/**
 * Test to debug the hash verification issue
 */
class HashVerificationTest {
    
    @Test
    void testSeedHashVerification() {
        String password = "password123";
        String seedHash = "$2a$10$N9qo8uLOickgx2ZMRZoMye/SJ4y9hN7cU3VpQqPdIjVwLpEaJ1V5G";
        
        // Test direct BCrypt verification
        BCrypt.Result result = BCrypt.verifyer().verify(password.toCharArray(), seedHash);
        System.out.println("Direct verification result: " + result.verified);
        System.out.println("Details: " + result.details);
        
        // Test with our PasswordUtil
        boolean utilResult = PasswordUtil.verifyPassword(password, seedHash);
        System.out.println("PasswordUtil result: " + utilResult);
        
        // Generate a new hash with cost 10 and test
        String newHash10 = BCrypt.withDefaults().hashToString(10, password.toCharArray());
        System.out.println("Generated hash (cost 10): " + newHash10);
        
        boolean newHashResult = PasswordUtil.verifyPassword(password, newHash10);
        System.out.println("New hash verification: " + newHashResult);
    }
}