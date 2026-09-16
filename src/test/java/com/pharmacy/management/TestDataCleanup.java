package com.pharmacy.management;

import com.pharmacy.management.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TestDataCleanup {
    public static void main(String[] args) {
        try {
            System.out.println("🧹 Cleaning up test data...");
            
            try (Connection conn = DatabaseConfig.getConnection()) {
                // Clean up test medicines
                String cleanup = "DELETE FROM medicines WHERE barcode = 'TEST123456789' OR medicine_name LIKE 'Test%' OR medicine_name LIKE 'Updated Test%'";
                try (PreparedStatement ps = conn.prepareStatement(cleanup)) {
                    int deleted = ps.executeUpdate();
                    System.out.println("✅ Deleted " + deleted + " test medicine records");
                }
                
                // Verify database has seed data
                String count = "SELECT COUNT(*) as total FROM medicines WHERE status = 'ACTIVE'";
                try (PreparedStatement ps = conn.prepareStatement(count);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("✅ Database has " + rs.getInt("total") + " active medicines");
                    }
                }
                
                // Check users table
                String userCount = "SELECT COUNT(*) as total FROM users";
                try (PreparedStatement ps = conn.prepareStatement(userCount);
                     ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        System.out.println("✅ Database has " + rs.getInt("total") + " users");
                    }
                }
                
                System.out.println("✅ Database cleanup complete! Ready to run tests.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error during cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
