-- Apply schema to existing thotho database
USE thotho;

-- ============================================================================
-- USERS TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'PHARMACIST') NOT NULL DEFAULT 'PHARMACIST',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    
    INDEX idx_username (username),
    INDEX idx_status (status),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- MEDICINES TABLE
-- ============================================================================

CREATE TABLE IF NOT EXISTS medicines (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_name VARCHAR(200) NOT NULL,
    generic_name VARCHAR(200) NOT NULL,
    active_ingredient VARCHAR(200) NOT NULL,
    brand_name VARCHAR(100),
    manufacturer VARCHAR(100),
    dosage_form VARCHAR(50) NOT NULL,
    strength VARCHAR(50) NOT NULL,
    category VARCHAR(50),
    barcode VARCHAR(100) UNIQUE,
    selling_price DECIMAL(10, 2) NOT NULL CHECK (selling_price > 0),
    prescription_required BOOLEAN DEFAULT FALSE,
    description TEXT,
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_medicine_name (medicine_name),
    INDEX idx_generic_name (generic_name),
    INDEX idx_active_ingredient (active_ingredient),
    INDEX idx_barcode (barcode),
    INDEX idx_status (status),
    INDEX idx_category (category),
    INDEX idx_composite_substitute (active_ingredient, strength, dosage_form)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- SEED DATA - USERS
-- ============================================================================
-- Password for both users: "password123" (BCrypt hashed)

INSERT IGNORE INTO users (username, password_hash, full_name, role, status) VALUES
('admin', '$2a$10$n8jz39mNztnIkAyXkVM1iuUSNW7afswd4zwa9ma0r7sEa1s7J2tW6', 'Admin User', 'ADMIN', 'ACTIVE'),
('pharmacist1', '$2a$10$n8jz39mNztnIkAyXkVM1iuUSNW7afswd4zwa9ma0r7sEa1s7J2tW6', 'John Pharmacist', 'PHARMACIST', 'ACTIVE');

-- ============================================================================
-- SEED DATA - MEDICINES
-- ============================================================================

INSERT IGNORE INTO medicines (
    medicine_name, generic_name, active_ingredient, brand_name, manufacturer, 
    dosage_form, strength, category, barcode, selling_price, 
    prescription_required, description, status
) VALUES
('Paracetamol 500mg Tablet', 'Paracetamol', 'Paracetamol', 'Dolo', 'Micro Labs', 'Tablet', '500mg', 'Analgesic', '8901234567890', 5.50, FALSE, 'Pain relief and fever reducer', 'ACTIVE'),
('Crocin Advance', 'Paracetamol', 'Paracetamol', 'Crocin', 'GSK', 'Tablet', '500mg', 'Analgesic', '8901234567891', 6.00, FALSE, 'Fast relief from pain and fever', 'ACTIVE'),
('Ibuprofen 400mg Tablet', 'Ibuprofen', 'Ibuprofen', 'Brufen', 'Abbott', 'Tablet', '400mg', 'NSAID', '8901234567892', 12.50, FALSE, 'Anti-inflammatory pain reliever', 'ACTIVE'),
('Amoxicillin 500mg Capsule', 'Amoxicillin', 'Amoxicillin', 'Mox', 'Ranbaxy', 'Capsule', '500mg', 'Antibiotic', '8901234567893', 45.00, TRUE, 'Bacterial infection treatment', 'ACTIVE'),
('Azithromycin 500mg Tablet', 'Azithromycin', 'Azithromycin', 'Azithral', 'Alembic', 'Tablet', '500mg', 'Antibiotic', '8901234567894', 95.00, TRUE, 'Broad-spectrum antibiotic', 'ACTIVE'),
('Cetirizine 10mg Tablet', 'Cetirizine', 'Cetirizine HCl', 'Zyrtec', 'UCB India', 'Tablet', '10mg', 'Antihistamine', '8901234567895', 8.50, FALSE, 'Allergy relief', 'ACTIVE'),
('Omeprazole 20mg Capsule', 'Omeprazole', 'Omeprazole', 'Omez', 'Dr. Reddy''s', 'Capsule', '20mg', 'PPI', '8901234567896', 22.00, FALSE, 'Acid reflux treatment', 'ACTIVE'),
('Metformin 500mg Tablet', 'Metformin', 'Metformin HCl', 'Glycomet', 'USV Ltd', 'Tablet', '500mg', 'Antidiabetic', '8901234567897', 18.50, TRUE, 'Blood sugar control', 'ACTIVE'),
('Amlodipine 5mg Tablet', 'Amlodipine', 'Amlodipine Besylate', 'Amlong', 'Micro Labs', 'Tablet', '5mg', 'Antihypertensive', '8901234567898', 15.00, TRUE, 'Blood pressure control', 'ACTIVE'),
('Salbutamol Inhaler 100mcg', 'Salbutamol', 'Salbutamol Sulfate', 'Asthalin', 'Cipla', 'Inhaler', '100mcg', 'Bronchodilator', '8901234567899', 120.00, FALSE, 'Asthma relief', 'ACTIVE');

SELECT 'Schema applied successfully!' AS Status;
SELECT COUNT(*) AS total_users FROM users;
SELECT COUNT(*) AS total_medicines FROM medicines;
