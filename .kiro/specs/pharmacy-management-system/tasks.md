# Implementation Plan: Smart Pharmacy Management System

## Overview

This implementation plan breaks down the Smart Pharmacy Management System into discrete, actionable tasks following a clean layered architecture (config, model, dao, service, ui, util). The plan follows the specified implementation order: Foundation → Module 1 (Users & Authentication) → Module 2 (Medicines) → Integration & Testing. Each task builds incrementally, ensuring the application remains functional and testable at every checkpoint.

## Tasks

### Phase 1: Foundation Setup

- [x] 1. Set up Maven project structure and dependencies
  - Create Maven project with groupId `com.pharmacy` and artifactId `pharmacy-management`
  - Configure pom.xml with JDK 17, MySQL connector, HikariCP, FlatLaf, Tess4J, ZXing, BCrypt, SLF4J/Logback
  - Create package structure: `config`, `model`, `dao`, `service`, `ui`, `ui/components`, `ui/dialogs`, `util`
  - Add Maven Shade Plugin for fat JAR creation
  - _Requirements: 12.5, 12.6_

- [x] 2. Create MySQL database schema and seed data
  - Execute schema.sql to create `pharmacy_db` database
  - Create `users` table with columns: id, username, password_hash, full_name, role, status, created_at, last_login
  - Create `medicines` table with all required columns and indexes
  - Insert seed data: 2 users (admin/pharmacist1) with BCrypt hashed passwords
  - Insert seed data: 10 sample medicines with various categories
  - _Requirements: 10.1, 10.2, 10.3, 10.4, 10.5_

- [x] 3. Implement database connection configuration
  - Create `DatabaseConfig.java` in config package
  - Implement HikariCP connection pool with MySQL configuration
  - Set pool parameters: maxPoolSize=10, minIdle=2, connectionTimeout=30s
  - Add getConnection() method returning Connection from pool
  - _Requirements: 12.4_

- [x] 4. Set up FlatLaf UI theme and constants
  - Create `Main.java` entry point with FlatLightLaf initialization
  - Create `UIConstants.java` in util package with color scheme (primary, success, danger, warning, background, sidebar colors)
  - Configure UIManager properties for rounded components (Button.arc=10, Component.arc=10)
  - _Requirements: 9.1_

### Phase 2: Module 1 - User Authentication & Session Management

- [x] 5. Create User model and enums
  - [x] 5.1 Create `UserRole.java` enum with values ADMIN, PHARMACIST
    - _Requirements: 2.1_
  
  - [x] 5.2 Create `User.java` model class
    - Add fields: id, username, passwordHash, fullName, role, status, createdAt, lastLogin
    - Add getters, setters, and constructors
    - _Requirements: 1.1, 10.1_
  
  - [x] 5.3 Create `Session.java` singleton class
    - Implement singleton pattern with getInstance() and clear() methods
    - Add fields: username, fullName, role, loginTime
    - _Requirements: 3.1, 3.2_

- [x] 6. Implement password hashing utility
  - Create `PasswordUtil.java` in util package
  - Implement hashPassword() using BCrypt with work factor 12
  - Implement verifyPassword() for password comparison
  - _Requirements: 1.3, 11.4_

- [x] 7. Implement User DAO layer
  - [x] 7.1 Create `BaseDAO.java` abstract class
    - Implement getConnection() and closeResources() helper methods
    - _Requirements: 12.2_
  
  - [x] 7.2 Create `UserDAO.java` extending BaseDAO
    - Implement findByUsername() using PreparedStatement
    - Implement updateLastLogin() to update last_login timestamp
    - Use try-with-resources for all JDBC operations
    - _Requirements: 1.1, 1.4, 1.5, 11.1, 11.3_

- [x] 8. Implement Authentication Service
  - Create `AuthenticationService.java` in service package
  - Implement authenticate() method with credential verification via UserDAO and PasswordUtil
  - Create Session on successful authentication
  - Implement logout() method to clear session
  - Implement hasRole() method for authorization checks
  - Throw AuthenticationException for invalid credentials
  - _Requirements: 1.1, 1.2, 2.1, 3.1, 3.3_

- [x] 9. Create Login UI
  - [x] 9.1 Create custom UI components
    - Create `CustomButton.java` in ui/components with hover effects and rounded borders
    - Create `CustomTextField.java` with rounded borders
    - Create `RoundedPanel.java` with rounded corners and shadow
    - _Requirements: 9.1, 9.4, 9.5_
  
  - [x] 9.2 Create `LoginFrame.java` in ui package
    - Design centered card layout with logo/title, username field, password field, login button
    - Add event handler for login button calling AuthenticationService
    - Display error messages for authentication failures
    - Validate that username and password are not empty before submission
    - On success, close LoginFrame and open MainFrame
    - _Requirements: 1.1, 1.2, 9.6_

- [x] 10. Create main dashboard structure
  - [x] 10.1 Create `MainFrame.java` with sidebar and content area
    - Implement sidebar with navigation buttons: Dashboard, Medicines, Prescription Scanner, Logout
    - Add header with application title, current user name and role display
    - Implement switchPanel() method to swap content dynamically
    - Add logout handler to clear session and return to LoginFrame
    - _Requirements: 9.2, 9.3, 3.2, 3.3, 14.3_
  
  - [x] 10.2 Create `DashboardPanel.java` with summary cards
    - Display total medicines count
    - Display active users count
    - Use RoundedPanel for card design
    - _Requirements: 14.1, 14.2, 14.4_

- [x] 11. Integrate Tesseract OCR configuration
  - [x] 11.1 Create `ImageUtil.java` for image preprocessing
    - Implement preprocessForOCR() with grayscale conversion, contrast enhancement, noise reduction, binarization
    - _Requirements: 4.2_
  
  - [x] 11.2 Create `OCRService.java` in service package
    - Initialize Tesseract with datapath, language=eng, PSM mode, OEM engine
    - Implement extractText() method using Tess4J
    - Implement identifyMedicineNames() using pattern matching/regex
    - Return OCRResult with rawText, identifiedMedicines, unmatchedText
    - Throw OCRException on failures with user-friendly messages
    - _Requirements: 4.1, 4.2, 4.3, 4.6, 13.1, 13.2, 13.3_

- [x] 12. Create Prescription Scanner UI
  - Create `PrescriptionPanel.java` in ui package
  - Add file upload button accepting PNG, JPG, JPEG formats
  - Display uploaded image preview
  - Add "Process Prescription" button calling OCRService
  - Display matched medicines in table
  - Display unmatched text in separate text area
  - Show loading indicator during OCR processing using SwingWorker
  - _Requirements: 4.1, 4.4, 4.5, 9.7_

- [x] 13. Checkpoint - Test authentication and OCR modules
  - Ensure all tests pass, ask the user if questions arise.

### Phase 3: Module 2 - Medicine Management

- [x] 14. Create Medicine model and validation
  - [x] 14.1 Create `Medicine.java` model class
    - Add all fields: medicineId, medicineName, genericName, activeIngredient, brandName, manufacturer, dosageForm, strength, category, barcode, sellingPrice, prescriptionRequired, description, status, createdAt, updatedAt
    - Add getters, setters, constructors
    - _Requirements: 5.1, 10.2_
  
  - [x] 14.2 Create `ValidationUtil.java` in util package
    - Implement validateMedicine() checking required fields (medicineName, genericName, activeIngredient, dosageForm, strength, sellingPrice)
    - Validate sellingPrice > 0
    - Throw ValidationException with specific error messages
    - _Requirements: 5.2, 11.2_

- [x] 15. Implement Medicine DAO layer
  - Create `MedicineDAO.java` extending BaseDAO
  - Implement insert() with PreparedStatement and auto-generated key retrieval
  - Implement findById() returning Medicine or null
  - Implement findAll() returning List<Medicine>
  - Implement search() with LIKE queries on medicine_name, generic_name, active_ingredient, manufacturer
  - Implement update() with PreparedStatement updating all fields and updated_at timestamp
  - Implement delete() or soft delete (set status='INACTIVE')
  - Implement findByBarcode() for barcode lookup
  - Implement findSubstitutes() with query on active_ingredient, strength, dosageForm
  - Use try-with-resources for all JDBC operations
  - _Requirements: 5.1, 5.3, 5.4, 5.5, 6.2, 7.1, 8.2, 11.1, 11.3_

- [x] 16. Implement Medicine Service layer
  - Create `MedicineService.java` in service package
  - Implement addMedicine() calling ValidationUtil then MedicineDAO.insert()
  - Implement getMedicineById() calling MedicineDAO.findById()
  - Implement getAllMedicines() calling MedicineDAO.findAll()
  - Implement searchMedicines() calling MedicineDAO.search()
  - Implement updateMedicine() with validation and calling MedicineDAO.update()
  - Implement deleteMedicine() with role authorization check using Session.getInstance().getRole()
  - Throw AuthorizationException if role is not ADMIN
  - Handle SQLException and throw ServiceException with user-friendly messages
  - _Requirements: 5.2, 5.5, 6.1, 6.3_

- [x] 17. Create Medicine UI - View and Search
  - [x] 17.1 Create `MedicineTableModel.java` extending AbstractTableModel
    - Define columns: ID, Name, Generic Name, Active Ingredient, Strength, Price, Status, Actions
    - Implement getColumnCount(), getRowCount(), getValueAt()
    - _Requirements: 5.3, 5.6_
  
  - [x] 17.2 Create `StatusBadge.java` component
    - Implement colored badge: Green for ACTIVE, Red for INACTIVE
    - _Requirements: 9.4_
  
  - [x] 17.3 Create `MedicinePanel.java` in ui package
    - Display medicines table with custom MedicineTableModel
    - Add custom renderer for Status column using StatusBadge
    - Add search bar at top with real-time filtering (call searchMedicines on keypress with debounce)
    - Add "Add Medicine" button at top
    - Add "Refresh" button to reload data
    - Implement loadMedicines() using SwingWorker for background loading
    - Add loading indicator during data fetch
    - _Requirements: 5.3, 5.4, 5.6, 9.7, 9.8_

- [x] 18. Create Medicine UI - Add/Edit Dialog
  - Create `MedicineFormDialog.java` in ui/dialogs
  - Use GridBagLayout for form fields: medicine name, generic name, active ingredient, brand name, manufacturer, dosage form, strength, category, barcode, selling price, prescription required (checkbox), description, status
  - Add Save and Cancel buttons
  - Implement validateForm() checking required fields before submission
  - Display validation errors inline or via JOptionPane
  - Support both Add and Edit modes (pass Medicine object for edit mode)
  - Call MedicineService.addMedicine() or updateMedicine()
  - _Requirements: 5.1, 5.2, 5.5, 9.6_

- [x] 19. Add Edit and Delete actions to Medicine table
  - Add Edit and Delete buttons in Actions column of MedicinePanel table
  - Edit button opens MedicineFormDialog in edit mode with selected medicine
  - Delete button shows confirmation dialog (ConfirmDialog)
  - Call MedicineService.deleteMedicine() on confirmation
  - Display AuthorizationException message if user is not ADMIN
  - Refresh table after successful edit or delete
  - _Requirements: 5.5, 6.1, 6.2, 6.3, 6.4_

- [x] 20. Implement substitute suggestion feature
  - [x] 20.1 Create `SubstituteService.java` in service package
    - Implement findSubstitutes() calling MedicineDAO.findSubstitutes()
    - Sort results by selling price (ascending)
    - _Requirements: 7.1, 7.2, 7.4_
  
  - [x] 20.2 Add "Find Substitutes" button to MedicinePanel
    - Add button in Actions column or toolbar
    - On click, call SubstituteService.findSubstitutes() with selected medicine
    - Display results in a dialog showing brand name, manufacturer, selling price
    - Display "No substitutes found" message if list is empty
    - _Requirements: 7.2, 7.3, 7.4_

- [x] 21. Implement barcode lookup feature
  - [x] 21.1 Create `BarcodeService.java` in service package
    - Implement decodeBarcode() using ZXing MultiFormatReader
    - Support CODE_128, CODE_39, EAN_13, QR_CODE formats
    - Implement findByBarcode() calling MedicineDAO.findByBarcode()
    - Throw BarcodeException on decoding failures
    - Throw NotFoundException if medicine not found
    - _Requirements: 8.1, 8.2, 8.4, 8.5_
  
  - [x] 21.2 Add barcode lookup to MedicinePanel
    - Add "Scan Barcode" button to toolbar
    - Open file chooser for barcode image upload
    - Call BarcodeService.decodeBarcode() and findByBarcode()
    - Display medicine details in dialog or highlight in table
    - Display error message if not found or decoding fails
    - _Requirements: 8.1, 8.2, 8.3, 8.4, 8.5_

- [x] 22. Checkpoint - Test Medicine CRUD and features
  - Ensure all tests pass, ask the user if questions arise.

### Phase 4: Integration, Testing, and Polish

- [x] 23. Integrate dashboard statistics
  - Update DashboardPanel to call MedicineService.getAllMedicines() and count active medicines
  - Display total medicines, total active medicines, total inactive medicines
  - Add recent activities placeholder for future enhancement
  - _Requirements: 14.2_

- [x] 24. Integrate OCR results with Medicine database
  - Update PrescriptionPanel to call MedicineDAO.search() for each identified medicine from OCR
  - Display matched medicines with complete details (price, manufacturer, etc.)
  - Add "Add to Bill" button for each matched medicine (placeholder for future billing module)
  - Prepare OCR data structure suitable for billing integration
  - _Requirements: 4.4, 13.4_

- [x] 25. Test role-based permissions
  - Test Admin login: verify delete medicine functionality works
  - Test Pharmacist login: verify delete medicine shows authorization error
  - Test both roles can perform add, edit, view, search operations
  - _Requirements: 2.1, 2.2, 2.3, 2.4, 6.3_

- [x] 26. Test all CRUD operations with edge cases
  - Test adding medicine with missing required fields (should show validation error)
  - Test adding medicine with negative price (should show validation error)
  - Test adding medicine with duplicate barcode (should show error)
  - Test searching with empty results
  - Test editing medicine and verify updated_at timestamp changes
  - Test deleting medicine and verify it's removed or status=INACTIVE
  - _Requirements: 5.2, 5.4, 11.2_

- [x] 27. Test OCR and Barcode failure scenarios
  - Test uploading corrupted image for OCR (should show error message)
  - Test uploading image with no text (should show "no medicines found")
  - Test barcode scan with invalid barcode (should show error message)
  - Test barcode lookup with non-existent barcode (should show "not in database")
  - _Requirements: 4.6, 8.5_

- [x] 28. UI polish and error handling
  - Add empty state messages for empty medicine table ("No medicines found. Click Add to get started.")
  - Add loading indicators using SwingWorker for all long-running operations
  - Test hover states on all buttons
  - Ensure consistent spacing and alignment across all panels
  - Verify all dialogs are modal and centered
  - Test window resizing and ensure layouts adapt properly
  - _Requirements: 9.4, 9.5, 9.7, 9.8_

- [x] 29. Create README and documentation
  - Create README.md with project description, prerequisites (JDK 17, MySQL 8, Tesseract, Maven)
  - Document installation steps (clone, create DB, update config, install Tesseract, build with Maven)
  - Document how to run application: `mvn clean package` then `java -jar target/pharmacy-management-1.0.0.jar`
  - Add default credentials for testing (admin/password123, pharmacist1/password123)
  - Document project structure and package organization
  - Add screenshots or description of key features
  - _Requirements: 12.5, 12.6_

- [x] 30. Final checkpoint - End-to-end testing
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- All tasks are designed to be implemented by a coding agent without manual user intervention
- Each task builds incrementally on previous tasks to maintain functional application state
- Testing tasks are integrated into implementation flow rather than isolated
- Checkpoint tasks provide natural breakpoints for validation
- All database operations use PreparedStatement for SQL injection prevention
- All JDBC resources use try-with-resources for proper cleanup
- All passwords are hashed using BCrypt before storage
- Role-based authorization is enforced at the service layer
- UI operations that involve I/O use SwingWorker to prevent UI blocking
- The architecture supports future modules (Stock, Billing, Suppliers) through clean separation of concerns

## Task Dependency Graph

```json
{
  "waves": [
    { "id": 0, "tasks": ["1", "2"] },
    { "id": 1, "tasks": ["3", "4"] },
    { "id": 2, "tasks": ["5.1", "5.2", "5.3", "6"] },
    { "id": 3, "tasks": ["7.1", "7.2", "8"] },
    { "id": 4, "tasks": ["9.1", "10.1"] },
    { "id": 5, "tasks": ["9.2", "10.2", "11.1"] },
    { "id": 6, "tasks": ["11.2", "12"] },
    { "id": 7, "tasks": ["14.1", "14.2"] },
    { "id": 8, "tasks": ["15"] },
    { "id": 9, "tasks": ["16", "17.1", "17.2"] },
    { "id": 10, "tasks": ["17.3", "18"] },
    { "id": 11, "tasks": ["19", "20.1"] },
    { "id": 12, "tasks": ["20.2", "21.1"] },
    { "id": 13, "tasks": ["21.2"] },
    { "id": 14, "tasks": ["23", "24"] },
    { "id": 15, "tasks": ["25", "26", "27"] },
    { "id": 16, "tasks": ["28", "29"] }
  ]
}
```
