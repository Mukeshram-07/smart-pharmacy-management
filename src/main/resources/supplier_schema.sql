-- Module 5: Supplier Management & Smart Dashboard Schema
-- Run against the 'thotho' database

USE thotho;

-- ============================================================================
-- SUPPLIERS TABLE
-- ============================================================================
CREATE TABLE IF NOT EXISTS suppliers (
    supplier_id   INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(200) NOT NULL,
    company_name  VARCHAR(200) NOT NULL,
    phone         VARCHAR(20)  NOT NULL,
    email         VARCHAR(200),
    address       TEXT,
    gst_number    VARCHAR(50),
    status        ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_supplier_name  (supplier_name),
    INDEX idx_company_name   (company_name),
    INDEX idx_status         (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MEDICINE PURCHASES TABLE
-- Stores every purchase / stock-receiving event.
-- medicine_id is a FK to medicines (master record stays one per product).
-- A separate stock row is inserted / updated for each batch.
-- ============================================================================
CREATE TABLE IF NOT EXISTS medicine_purchases (
    purchase_id     INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id     INT NOT NULL,
    medicine_id     INT NOT NULL,
    batch_number    VARCHAR(100) NOT NULL,
    quantity        INT NOT NULL,
    purchase_price  DECIMAL(10,2) NOT NULL,
    selling_price   DECIMAL(10,2),
    manufacturing_date DATE,
    expiry_date     DATE NOT NULL,
    minimum_stock_level INT NOT NULL DEFAULT 10,
    purchase_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    performed_by    VARCHAR(100),
    notes           VARCHAR(500),

    CONSTRAINT fk_purchase_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE RESTRICT,
    CONSTRAINT fk_purchase_medicine FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)  ON DELETE RESTRICT,

    INDEX idx_purchase_supplier (supplier_id),
    INDEX idx_purchase_medicine (medicine_id),
    INDEX idx_purchase_batch    (batch_number),
    INDEX idx_purchase_date     (purchase_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- ADD supplier_id COLUMN TO STOCK TABLE (if not present)
-- Links each stock batch back to the supplier it came from.
-- ============================================================================
ALTER TABLE stock
    ADD COLUMN IF NOT EXISTS supplier_id INT NULL,
    ADD CONSTRAINT fk_stock_supplier
        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL;

-- ============================================================================
-- SEED DATA
-- ============================================================================
INSERT IGNORE INTO suppliers (supplier_name, company_name, phone, email, address, gst_number, status) VALUES
('ABC Pharma',   'ABC Pharmaceuticals Pvt Ltd',    '9876543210', 'contact@abcpharma.com', '123 Pharma Street, Chennai', '33AABCA1234B1Z5', 'ACTIVE'),
('XYZ Pharma',   'XYZ Medical Supplies Ltd',       '9123456789', 'info@xyzmedical.com',   '456 Medical Lane, Mumbai',   '27AABXZ5678C2A3', 'ACTIVE'),
('MediCare',     'MediCare Distributors',           '9988776655', NULL,                    '789 Health Road, Delhi',     NULL,                'ACTIVE');

SELECT 'Supplier schema applied successfully!' AS Status;
SELECT COUNT(*) AS total_suppliers FROM suppliers;
