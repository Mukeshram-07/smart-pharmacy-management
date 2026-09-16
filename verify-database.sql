-- ============================================================================
-- Database Verification Script
-- ============================================================================
-- Run this script after executing schema.sql to verify everything is set up
-- ============================================================================

USE pharmacy_db;

-- Display banner
SELECT '============================================' AS '';
SELECT '  PHARMACY DATABASE VERIFICATION REPORT    ' AS '';
SELECT '============================================' AS '';
SELECT '' AS '';

-- Check database
SELECT '1. DATABASE CHECK' AS '';
SELECT SCHEMA_NAME AS 'Database Name', 
       DEFAULT_CHARACTER_SET_NAME AS 'Character Set',
       DEFAULT_COLLATION_NAME AS 'Collation'
FROM INFORMATION_SCHEMA.SCHEMATA 
WHERE SCHEMA_NAME = 'pharmacy_db';
SELECT '' AS '';

-- Check tables
SELECT '2. TABLES CHECK' AS '';
SELECT TABLE_NAME AS 'Table Name',
       TABLE_ROWS AS 'Row Count',
       ENGINE AS 'Storage Engine'
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = 'pharmacy_db'
ORDER BY TABLE_NAME;
SELECT '' AS '';

-- Check users table
SELECT '3. USERS TABLE' AS '';
SELECT COUNT(*) AS 'Total Users' FROM users;
SELECT username, full_name, role, status, created_at 
FROM users 
ORDER BY role DESC;
SELECT '' AS '';

-- Check medicines table
SELECT '4. MEDICINES TABLE' AS '';
SELECT COUNT(*) AS 'Total Medicines' FROM medicines;
SELECT 
    status,
    COUNT(*) AS 'Count'
FROM medicines
GROUP BY status;
SELECT '' AS '';

-- Check medicine categories
SELECT '5. MEDICINE CATEGORIES' AS '';
SELECT 
    category,
    COUNT(*) AS 'Count',
    AVG(selling_price) AS 'Avg Price'
FROM medicines
GROUP BY category
ORDER BY category;
SELECT '' AS '';

-- Check prescription required medicines
SELECT '6. PRESCRIPTION REQUIRED MEDICINES' AS '';
SELECT 
    CASE WHEN prescription_required THEN 'Yes' ELSE 'No' END AS 'Prescription Required',
    COUNT(*) AS 'Count'
FROM medicines
GROUP BY prescription_required;
SELECT '' AS '';

-- Check indexes on users table
SELECT '7. USERS TABLE INDEXES' AS '';
SELECT 
    INDEX_NAME AS 'Index Name',
    COLUMN_NAME AS 'Column',
    NON_UNIQUE AS 'Non-Unique'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'pharmacy_db' 
  AND TABLE_NAME = 'users'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;
SELECT '' AS '';

-- Check indexes on medicines table
SELECT '8. MEDICINES TABLE INDEXES' AS '';
SELECT 
    INDEX_NAME AS 'Index Name',
    COLUMN_NAME AS 'Column',
    NON_UNIQUE AS 'Non-Unique'
FROM INFORMATION_SCHEMA.STATISTICS
WHERE TABLE_SCHEMA = 'pharmacy_db' 
  AND TABLE_NAME = 'medicines'
ORDER BY INDEX_NAME, SEQ_IN_INDEX;
SELECT '' AS '';

-- Sample medicine details
SELECT '9. SAMPLE MEDICINES (First 5)' AS '';
SELECT 
    medicine_name AS 'Medicine',
    category AS 'Category',
    dosage_form AS 'Form',
    strength AS 'Strength',
    selling_price AS 'Price',
    CASE WHEN prescription_required THEN 'Yes' ELSE 'No' END AS 'Rx Required'
FROM medicines
LIMIT 5;
SELECT '' AS '';

-- Test substitute medicine query (should find Paracetamol substitutes)
SELECT '10. SUBSTITUTE TEST - Paracetamol 500mg Tablets' AS '';
SELECT 
    medicine_name AS 'Medicine',
    brand_name AS 'Brand',
    manufacturer AS 'Manufacturer',
    selling_price AS 'Price'
FROM medicines
WHERE active_ingredient = 'Paracetamol'
  AND strength = '500mg'
  AND dosage_form = 'Tablet'
  AND status = 'ACTIVE'
ORDER BY selling_price ASC;
SELECT '' AS '';

-- Test barcode lookup
SELECT '11. BARCODE LOOKUP TEST - 8901234567890' AS '';
SELECT 
    medicine_name AS 'Medicine',
    barcode AS 'Barcode',
    selling_price AS 'Price'
FROM medicines
WHERE barcode = '8901234567890';
SELECT '' AS '';

-- Test search functionality
SELECT '12. SEARCH TEST - Medicines containing "para"' AS '';
SELECT 
    medicine_name AS 'Medicine',
    category AS 'Category'
FROM medicines
WHERE medicine_name LIKE '%para%' 
   OR generic_name LIKE '%para%'
   OR active_ingredient LIKE '%para%';
SELECT '' AS '';

-- Summary
SELECT '============================================' AS '';
SELECT '           VERIFICATION SUMMARY             ' AS '';
SELECT '============================================' AS '';

SELECT 
    'Users' AS 'Component',
    CASE 
        WHEN (SELECT COUNT(*) FROM users) >= 2 THEN '✓ PASS'
        ELSE '✗ FAIL'
    END AS 'Status',
    CONCAT((SELECT COUNT(*) FROM users), ' users created') AS 'Details'
UNION ALL
SELECT 
    'Medicines' AS 'Component',
    CASE 
        WHEN (SELECT COUNT(*) FROM medicines) >= 10 THEN '✓ PASS'
        ELSE '✗ FAIL'
    END AS 'Status',
    CONCAT((SELECT COUNT(*) FROM medicines), ' medicines created') AS 'Details'
UNION ALL
SELECT 
    'Admin User' AS 'Component',
    CASE 
        WHEN EXISTS(SELECT 1 FROM users WHERE role = 'ADMIN') THEN '✓ PASS'
        ELSE '✗ FAIL'
    END AS 'Status',
    CONCAT((SELECT COUNT(*) FROM users WHERE role = 'ADMIN'), ' admin found') AS 'Details'
UNION ALL
SELECT 
    'Pharmacist User' AS 'Component',
    CASE 
        WHEN EXISTS(SELECT 1 FROM users WHERE role = 'PHARMACIST') THEN '✓ PASS'
        ELSE '✗ FAIL'
    END AS 'Status',
    CONCAT((SELECT COUNT(*) FROM users WHERE role = 'PHARMACIST'), ' pharmacist found') AS 'Details'
UNION ALL
SELECT 
    'Unique Barcodes' AS 'Component',
    CASE 
        WHEN (SELECT COUNT(DISTINCT barcode) FROM medicines WHERE barcode IS NOT NULL) = 
             (SELECT COUNT(barcode) FROM medicines WHERE barcode IS NOT NULL) THEN '✓ PASS'
        ELSE '✗ FAIL'
    END AS 'Status',
    'All barcodes unique' AS 'Details'
UNION ALL
SELECT 
    'Active Medicines' AS 'Component',
    CASE 
        WHEN EXISTS(SELECT 1 FROM medicines WHERE status = 'ACTIVE') THEN '✓ PASS'
        ELSE '✗ FAIL'
    END AS 'Status',
    CONCAT((SELECT COUNT(*) FROM medicines WHERE status = 'ACTIVE'), ' active medicines') AS 'Details';

SELECT '' AS '';
SELECT '============================================' AS '';
SELECT 'Verification Complete!' AS '';
SELECT 'If all checks show ✓ PASS, database is ready!' AS '';
SELECT '============================================' AS '';
