-- Cleanup test data before running tests
USE thotho;

-- Delete test medicines added during testing
DELETE FROM medicines WHERE barcode = 'TEST123456789';
DELETE FROM medicines WHERE medicine_name LIKE 'Test%';
DELETE FROM medicines WHERE medicine_name LIKE 'Updated Test%';

SELECT 'Test data cleaned up successfully!' AS Status;
