# Requirements Document

## Introduction

The Smart Pharmacy Management System is a Java desktop application designed to streamline pharmacy operations through secure authentication, prescription processing via OCR, and comprehensive medicine management with intelligent substitute suggestions. The system targets pharmacists and administrators, providing role-based access control, modern UI/UX, and integration with barcode scanning and OCR technologies.

## Glossary

- **System**: The Smart Pharmacy Management System application
- **User**: A person interacting with the system (Admin or Pharmacist)
- **Admin**: A user with administrative privileges (full CRUD access)
- **Pharmacist**: A user with standard privileges (limited access)
- **Medicine_Record**: A complete medicine entry in the database
- **Prescription_Image**: A digital image of a medical prescription
- **OCR_Engine**: Tesseract OCR component for text extraction
- **Active_Ingredient**: The pharmacologically active compound in a medicine
- **Substitute_Medicine**: An alternative medicine with identical active ingredient, strength, and dosage form
- **Barcode_Scanner**: ZXing component for barcode/QR code reading
- **Session**: An authenticated user's active connection to the system
- **Database**: MySQL database storing all application data
- **Password_Hash**: Cryptographically hashed password (never plaintext)
- **Medicine_Database**: The medicines table in the MySQL database

## Requirements

### Requirement 1: User Authentication

**User Story:** As a pharmacy staff member, I want to securely log in to the system, so that only authorized personnel can access sensitive pharmacy data.

#### Acceptance Criteria

1. WHEN a User submits valid credentials, THE System SHALL authenticate the User and create a Session
2. WHEN a User submits invalid credentials, THE System SHALL reject the login attempt and display an error message
3. THE System SHALL store passwords as Password_Hash values in the Database
4. WHEN a User successfully authenticates, THE System SHALL update the last_login timestamp in the Database
5. THE System SHALL use PreparedStatement for all authentication queries to prevent SQL injection

### Requirement 2: Role-Based Access Control

**User Story:** As an administrator, I want role-based permissions, so that pharmacists and admins have appropriate access levels.

#### Acceptance Criteria

1. WHEN a User logs in, THE System SHALL assign permissions based on the User's role (ADMIN or PHARMACIST)
2. WHERE the User role is ADMIN, THE System SHALL grant access to delete Medicine_Record operations
3. WHERE the User role is PHARMACIST, THE System SHALL restrict access to delete Medicine_Record operations
4. THE System SHALL validate role permissions before executing restricted operations

### Requirement 3: Session Management

**User Story:** As a user, I want my session to persist during my work session, so that I don't need to re-authenticate frequently.

#### Acceptance Criteria

1. WHEN a User successfully authenticates, THE System SHALL create a Session containing username, full name, and role
2. WHILE a Session is active, THE System SHALL display the current User's name and role in the dashboard header
3. WHEN a User clicks logout, THE System SHALL terminate the Session and return to the login screen

### Requirement 4: Prescription OCR Processing

**User Story:** As a pharmacist, I want to upload prescription images and extract medicine names, so that I can quickly process patient prescriptions.

#### Acceptance Criteria

1. WHEN a User uploads a Prescription_Image, THE System SHALL accept common image formats (PNG, JPG, JPEG)
2. WHEN a Prescription_Image is uploaded, THE OCR_Engine SHALL extract text from the image
3. WHEN text is extracted, THE System SHALL identify medicine names from the extracted text
4. WHEN medicine names are identified, THE System SHALL match them against the Medicine_Database
5. WHEN matching is complete, THE System SHALL display matched medicines and unmatched text separately
6. IF OCR extraction fails, THEN THE System SHALL display an error message with guidance

### Requirement 5: Medicine CRUD Operations

**User Story:** As a pharmacist, I want to add, view, update, and search medicines, so that I can maintain accurate inventory records.

#### Acceptance Criteria

1. WHEN a User submits a valid medicine form, THE System SHALL create a new Medicine_Record in the Database
2. THE System SHALL validate all required fields (medicine_name, generic_name, active_ingredient, dosage_form, strength, selling_price) before saving
3. WHEN a User requests medicine list, THE System SHALL retrieve all Medicine_Record entries from the Database
4. WHEN a User searches by any field, THE System SHALL return Medicine_Record entries matching the search criteria with partial matching support
5. WHEN a User updates a Medicine_Record, THE System SHALL validate the changes and update the updated_at timestamp
6. THE System SHALL display all Medicine_Record entries in a paginated, sortable table format

### Requirement 6: Medicine Deletion

**User Story:** As an administrator, I want to delete or deactivate medicines safely, so that incorrect records can be removed with proper authorization.

#### Acceptance Criteria

1. WHERE the User role is ADMIN, WHEN a User requests deletion of a Medicine_Record, THE System SHALL display a confirmation dialog
2. WHERE the User role is ADMIN, WHEN deletion is confirmed, THE System SHALL deactivate or remove the Medicine_Record from the Database
3. WHERE the User role is PHARMACIST, THE System SHALL deny deletion requests and display an authorization error
4. WHEN a Medicine_Record is deleted, THE System SHALL refresh the medicines table view

### Requirement 7: Medicine Substitute Suggestions

**User Story:** As a pharmacist, I want to find substitute medicines with the same active ingredient, so that I can offer alternatives when a medicine is unavailable.

#### Acceptance Criteria

1. WHEN a User requests substitutes for a Medicine_Record, THE System SHALL query the Database for medicines with identical Active_Ingredient, strength, and dosage_form
2. WHEN substitute medicines are found, THE System SHALL display all matching Substitute_Medicine entries excluding the original medicine
3. WHEN no substitutes are found, THE System SHALL display a message indicating no alternatives are available
4. THE System SHALL display substitute medicine details including brand name, manufacturer, and selling price

### Requirement 8: Barcode-Based Medicine Lookup

**User Story:** As a pharmacist, I want to scan medicine barcodes to quickly find medicine information, so that I can serve customers efficiently.

#### Acceptance Criteria

1. WHEN a User scans a barcode using the Barcode_Scanner, THE System SHALL decode the barcode value
2. WHEN a barcode is decoded, THE System SHALL query the Medicine_Database using the barcode value
3. WHEN a matching Medicine_Record is found, THE System SHALL display the complete medicine details
4. WHEN no match is found, THE System SHALL display a message indicating the medicine is not in the Database
5. IF barcode scanning fails, THEN THE System SHALL display an error message

### Requirement 9: User Interface Design

**User Story:** As a user, I want a modern, professional interface, so that the application is pleasant and efficient to use.

#### Acceptance Criteria

1. THE System SHALL use FlatLaf Look and Feel for modern UI rendering
2. THE System SHALL display a sidebar navigation with Dashboard, Medicines, Prescription Scanner, and Logout options
3. THE System SHALL display a top header with current page title, username, role, and profile menu
4. THE System SHALL use rounded cards, consistent spacing, modern typography, and status badges
5. WHEN a User hovers over interactive elements, THE System SHALL display hover states
6. WHEN a User submits forms, THE System SHALL validate inputs and display error messages for invalid data
7. WHEN operations are in progress, THE System SHALL display loading indicators
8. WHEN data sets are empty, THE System SHALL display meaningful empty state messages

### Requirement 10: Database Schema and Data Integrity

**User Story:** As a system administrator, I want a well-structured database with proper constraints, so that data integrity is maintained.

#### Acceptance Criteria

1. THE Database SHALL contain a users table with columns: id, username, password_hash, full_name, role, status, created_at, last_login
2. THE Database SHALL contain a medicines table with columns: medicine_id, medicine_name, generic_name, active_ingredient, brand_name, manufacturer, dosage_form, strength, category, barcode, selling_price, prescription_required, description, status, created_at, updated_at
3. THE Database SHALL enforce unique constraints on username in users table and barcode in medicines table
4. THE Database SHALL include indexes on frequently queried fields (medicine_name, generic_name, active_ingredient, barcode)
5. THE Database SHALL include sample seed data with at least 1 Admin, 1 Pharmacist, and 10 medicines

### Requirement 11: Security and Input Validation

**User Story:** As a system administrator, I want secure data handling and input validation, so that the system is protected from common vulnerabilities.

#### Acceptance Criteria

1. THE System SHALL use PreparedStatement for all SQL queries to prevent SQL injection attacks
2. THE System SHALL validate all user inputs before processing
3. THE System SHALL use try-with-resources for all JDBC connections to ensure proper resource cleanup
4. THE System SHALL hash passwords using a secure hashing algorithm before storing in the Database
5. THE System SHALL sanitize file paths when handling Prescription_Image uploads
6. IF an exception occurs, THEN THE System SHALL handle it gracefully without exposing sensitive information

### Requirement 12: Application Architecture

**User Story:** As a developer, I want a clean, maintainable architecture, so that the system is easy to extend and maintain.

#### Acceptance Criteria

1. THE System SHALL organize code into packages: config, model, dao, service, ui, util
2. THE System SHALL separate database access logic (DAO layer) from business logic (Service layer)
3. THE System SHALL separate business logic (Service layer) from presentation logic (UI layer)
4. THE System SHALL provide a reusable database connection utility in the config package
5. THE System SHALL use Maven for dependency management and build automation
6. THE System SHALL target JDK 17 or higher

### Requirement 13: OCR Configuration and Medicine Extraction

**User Story:** As a developer, I want properly configured OCR and medicine name extraction, so that prescription processing is accurate.

#### Acceptance Criteria

1. THE System SHALL integrate Tesseract OCR engine via Tess4J library
2. WHEN the OCR_Engine processes a Prescription_Image, THE System SHALL configure Tesseract with appropriate language data (English)
3. WHEN medicine names are extracted, THE System SHALL use pattern matching or keyword recognition to identify medicine names from OCR text
4. THE System SHALL prepare extracted medicine data in a structure suitable for future billing integration

### Requirement 14: Dashboard and Navigation

**User Story:** As a user, I want a central dashboard with navigation, so that I can easily access all system features.

#### Acceptance Criteria

1. WHEN a User logs in successfully, THE System SHALL display the main dashboard
2. THE System SHALL display summary statistics on the dashboard (total medicines, active users, recent activities)
3. WHEN a User clicks a navigation item, THE System SHALL switch to the corresponding view
4. THE System SHALL highlight the current active page in the sidebar navigation
