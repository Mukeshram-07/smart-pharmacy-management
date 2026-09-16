package com.pharmacy.management.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Simple test class to verify database connection configuration.
 */
public class DatabaseConfigTest {
    
    public static void main(String[] args) {
        System.out.println("Testing Database Connection...\n");
        
        try {
            // Test 1: Get a connection from the pool
            System.out.println("Test 1: Getting connection from pool...");
            Connection conn = DatabaseConfig.getConnection();
            System.out.println("✓ Connection obtained successfully!");
            System.out.println("  Connection: " + conn);
            System.out.println("  Pool Stats: " + DatabaseConfig.getPoolStats());
            
            // Test 2: Execute a simple query
            System.out.println("\nTest 2: Executing test query...");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1 AS test");
            if (rs.next()) {
                System.out.println("✓ Query executed successfully!");
                System.out.println("  Result: " + rs.getInt("test"));
            }
            
            // Test 3: Check database tables
            System.out.println("\nTest 3: Checking database tables...");
            rs = stmt.executeQuery("SHOW TABLES");
            System.out.println("✓ Tables in pharmacy_db:");
            while (rs.next()) {
                System.out.println("  - " + rs.getString(1));
            }
            
            // Test 4: Get connection pool statistics
            System.out.println("\nTest 4: Connection pool statistics...");
            System.out.println("  " + DatabaseConfig.getPoolStats());
            
            // Clean up
            rs.close();
            stmt.close();
            conn.close();
            
            System.out.println("\n✓ All tests passed! Database configuration is working correctly.");
            
            // Close the pool
            DatabaseConfig.closePool();
            System.out.println("✓ Connection pool closed successfully.");
            
        } catch (Exception e) {
            System.err.println("✗ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
