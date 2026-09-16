package com.pharmacy.management.util;

import com.pharmacy.management.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Initialises the Module 3 (Stock Management) database schema on application startup.
 * Uses CREATE TABLE IF NOT EXISTS so it is safe to run every startup.
 */
public class SchemaInitializer {

    private static final Logger logger = LoggerFactory.getLogger(SchemaInitializer.class);

    public static void initializeStockSchema() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // --- stock table ---
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS stock (" +
                "  stock_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  medicine_id INT NOT NULL," +
                "  batch_number VARCHAR(100) NOT NULL," +
                "  quantity INT NOT NULL DEFAULT 0," +
                "  minimum_stock_level INT NOT NULL DEFAULT 10," +
                "  expiry_date DATE NOT NULL," +
                "  supplier VARCHAR(200)," +
                "  purchase_price DECIMAL(10,2)," +
                "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP," +
                "  CONSTRAINT fk_stock_medicine FOREIGN KEY (medicine_id)" +
                "    REFERENCES medicines(medicine_id) ON DELETE RESTRICT," +
                "  INDEX idx_medicine_id (medicine_id)," +
                "  INDEX idx_expiry_date (expiry_date)," +
                "  INDEX idx_quantity (quantity)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            );

            // --- stock_transactions table ---
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS stock_transactions (" +
                "  transaction_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  medicine_id INT NOT NULL," +
                "  transaction_type ENUM('ADD','REMOVE','CORRECTION') NOT NULL," +
                "  quantity_changed INT NOT NULL," +
                "  previous_quantity INT NOT NULL," +
                "  new_quantity INT NOT NULL," +
                "  transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  performed_by VARCHAR(100) NOT NULL," +
                "  notes VARCHAR(500)," +
                "  CONSTRAINT fk_transaction_medicine FOREIGN KEY (medicine_id)" +
                "    REFERENCES medicines(medicine_id) ON DELETE RESTRICT," +
                "  INDEX idx_transaction_medicine (medicine_id)," +
                "  INDEX idx_transaction_date (transaction_date)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"
            );

            // --- Seed sample data only if stock table is empty ---
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM stock")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.execute(
                        "INSERT IGNORE INTO stock " +
                        "  (medicine_id, batch_number, quantity, minimum_stock_level, expiry_date, supplier, purchase_price) " +
                        "SELECT medicine_id, " +
                        "       CONCAT('BATCH-', LPAD(medicine_id, 4, '0'), '-2024'), " +
                        "       FLOOR(RAND() * 200 + 50), " +
                        "       20, " +
                        "       DATE_ADD(CURDATE(), INTERVAL FLOOR(RAND() * 365 + 30) DAY), " +
                        "       'Default Supplier', " +
                        "       selling_price * 0.7 " +
                        "FROM medicines WHERE status = 'ACTIVE'"
                    );
                    logger.info("Stock seed data inserted successfully");
                }
            }

            logger.info("Stock schema initialised successfully");

        } catch (Exception e) {
            // Non-fatal — log and continue so login still works
            logger.warn("Stock schema initialisation warning: {}", e.getMessage());
        }
    }

    /**
     * Initialises the Module 4 (Billing) database schema on application startup.
     * Uses CREATE TABLE IF NOT EXISTS so it is safe to run every startup.
     */
    public static void initializeBillingSchema() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            // --- bills table ---
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS bills (" +
                "  bill_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  bill_number VARCHAR(30) UNIQUE NOT NULL," +
                "  user_id INT NOT NULL," +
                "  bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "  subtotal DECIMAL(12,2) NOT NULL," +
                "  discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00," +
                "  discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                "  tax_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00," +
                "  tax_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00," +
                "  grand_total DECIMAL(12,2) NOT NULL," +
                "  payment_method ENUM('CASH','CARD','UPI') NOT NULL DEFAULT 'CASH'," +
                "  amount_paid DECIMAL(12,2) NOT NULL," +
                "  balance DECIMAL(12,2) NOT NULL," +
                "  performed_by VARCHAR(100) NOT NULL," +
                "  CONSTRAINT fk_bill_user FOREIGN KEY (user_id) REFERENCES users(id)," +
                "  INDEX idx_bill_number (bill_number)," +
                "  INDEX idx_bill_date (bill_date)," +
                "  INDEX idx_performed_by (performed_by)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            // --- bill_items table ---
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS bill_items (" +
                "  bill_item_id INT AUTO_INCREMENT PRIMARY KEY," +
                "  bill_id INT NOT NULL," +
                "  medicine_id INT NOT NULL," +
                "  medicine_name VARCHAR(200) NOT NULL," +
                "  batch_number VARCHAR(100)," +
                "  quantity INT NOT NULL," +
                "  unit_price DECIMAL(10,2) NOT NULL," +
                "  discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00," +
                "  tax_percent DECIMAL(5,2) NOT NULL DEFAULT 0.00," +
                "  total DECIMAL(12,2) NOT NULL," +
                "  CONSTRAINT fk_bill_item_bill FOREIGN KEY (bill_id)" +
                "    REFERENCES bills(bill_id) ON DELETE CASCADE," +
                "  CONSTRAINT fk_bill_item_medicine FOREIGN KEY (medicine_id)" +
                "    REFERENCES medicines(medicine_id)," +
                "  INDEX idx_bill_item_bill (bill_id)," +
                "  INDEX idx_bill_item_medicine (medicine_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"
            );

            logger.info("Billing schema initialised successfully");

        } catch (Exception e) {
            logger.warn("Billing schema initialisation warning: {}", e.getMessage());
        }
    }

    private SchemaInitializer() {
        throw new UnsupportedOperationException("Utility class");
    }
}
