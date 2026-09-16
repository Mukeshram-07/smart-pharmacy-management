-- Module 3: Stock Management Schema
-- Run against the 'thotho' database

USE thotho;

-- Stock table (links to medicines table)
CREATE TABLE IF NOT EXISTS stock (
    stock_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    batch_number VARCHAR(100) NOT NULL,
    quantity INT NOT NULL DEFAULT 0,
    minimum_stock_level INT NOT NULL DEFAULT 10,
    expiry_date DATE NOT NULL,
    supplier VARCHAR(200),
    purchase_price DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_stock_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE RESTRICT,
    INDEX idx_medicine_id (medicine_id),
    INDEX idx_batch_number (batch_number),
    INDEX idx_expiry_date (expiry_date),
    INDEX idx_quantity (quantity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Stock transactions history table
CREATE TABLE IF NOT EXISTS stock_transactions (
    transaction_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    transaction_type ENUM('ADD', 'REMOVE', 'CORRECTION') NOT NULL,
    quantity_changed INT NOT NULL,
    previous_quantity INT NOT NULL,
    new_quantity INT NOT NULL,
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    performed_by VARCHAR(100) NOT NULL,
    notes VARCHAR(500),
    CONSTRAINT fk_transaction_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id) ON DELETE RESTRICT,
    INDEX idx_transaction_medicine (medicine_id),
    INDEX idx_transaction_date (transaction_date),
    INDEX idx_transaction_type (transaction_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Insert sample stock data for existing medicines
INSERT IGNORE INTO stock (medicine_id, batch_number, quantity, minimum_stock_level, expiry_date, supplier, purchase_price)
SELECT medicine_id,
       CONCAT('BATCH-', LPAD(medicine_id, 4, '0'), '-2024'),
       FLOOR(RAND() * 200 + 50),
       20,
       DATE_ADD(CURDATE(), INTERVAL FLOOR(RAND() * 365 + 30) DAY),
       'Default Supplier',
       selling_price * 0.7
FROM medicines WHERE status = 'ACTIVE';
