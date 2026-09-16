# Design Document: Smart Pharmacy Management System

## Overview

The Smart Pharmacy Management System is a desktop application built with Java Swing that provides comprehensive pharmacy operations management. The system implements a layered MVC architecture with clear separation of concerns across configuration, data access, business logic, and presentation layers.

### Technology Stack

- **Frontend**: Java Swing with FlatLaf Look and Feel
- **Backend**: Java 17+
- **Database**: MySQL 8.0+
- **OCR Engine**: Tesseract OCR via Tess4J library
- **Barcode Processing**: ZXing (Zebra Crossing) library
- **Build Tool**: Maven
- **Security**: BCrypt or PBKDF2 for password hashing

### Core Features

1. **Authentication & Authorization**: Secure login with role-based access control (Admin/Pharmacist)
2. **Medicine Management**: Full CRUD operations with search, filtering, and pagination
3. **Prescription OCR**: Upload and process prescription images to extract medicine names
4. **Substitute Suggestions**: Find alternative medicines based on active ingredients
5. **Barcode Lookup**: Quick medicine retrieval via barcode scanning
6. **Modern UI/UX**: Professional interface with FlatLaf theming

### Design Principles

- **Separation of Concerns**: Clear boundaries between layers (UI, Service, DAO, Config)
- **Single Responsibility**: Each class has one well-defined purpose
- **Dependency Injection**: Services receive dependencies through constructors
- **Fail-Safe Operations**: Graceful error handling with user-friendly messages
- **Security by Default**: PreparedStatements, password hashing, input validation
- **Extensibility**: Architecture supports future modules (Stock, Billing, Suppliers)

## Architecture

### High-Level Architecture

The system follows a **4-tier layered architecture**:

```
┌─────────────────────────────────────────────────────────────┐
│                      Presentation Layer                      │
│  (UI Package - Swing Components, Panels, Dialogs)           │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                       Service Layer                          │
│  (Business Logic, Validation, OCR/Barcode Processing)       │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    Data Access Layer                         │
│  (DAO Pattern - Database Operations, SQL Queries)           │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                      Database Layer                          │
│  (MySQL - users, medicines tables)                          │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.pharmacy.management/
├── config/
│   ├── DatabaseConfig.java          # Database connection management
│   └── AppConfig.java                # Application-wide configuration
├── model/
│   ├── User.java                     # User entity
│   ├── Medicine.java                 # Medicine entity
│   ├── Session.java                  # Session management
│   └── UserRole.java                 # Enum for roles (ADMIN, PHARMACIST)
├── dao/
│   ├── UserDAO.java                  # User data access operations
│   ├── MedicineDAO.java              # Medicine data access operations
│   └── BaseDAO.java                  # Common DAO utilities
├── service/
│   ├── AuthenticationService.java   # Login/logout logic
│   ├── MedicineService.java         # Medicine business logic
│   ├── OCRService.java              # Prescription OCR processing
│   ├── BarcodeService.java          # Barcode scanning logic
│   └── SubstituteService.java       # Substitute suggestion logic
├── ui/
│   ├── LoginFrame.java              # Login screen
│   ├── MainFrame.java               # Main dashboard container
│   ├── DashboardPanel.java          # Dashboard summary view
│   ├── MedicinePanel.java           # Medicine management UI
│   ├── PrescriptionPanel.java       # Prescription scanner UI
│   ├── components/                  # Reusable UI components
│   │   ├── CustomButton.java
│   │   ├── CustomTextField.java
│   │   ├── RoundedPanel.java
│   │   └── StatusBadge.java
│   └── dialogs/
│       ├── MedicineFormDialog.java
│       └── ConfirmDialog.java
├── util/
│   ├── PasswordUtil.java            # Password hashing utilities
│   ├── ValidationUtil.java          # Input validation
│   ├── ImageUtil.java               # Image preprocessing
│   └── Constants.java               # Application constants
└── Main.java                        # Application entry point
```

### Layer Responsibilities

#### 1. Presentation Layer (UI Package)
- **Responsibility**: User interface, event handling, data presentation
- **Components**: Swing frames, panels, dialogs, custom components
- **Dependencies**: Service layer only (no direct DAO access)
- **Key Classes**:
  - `LoginFrame`: Authentication screen
  - `MainFrame`: Container with sidebar and content area
  - `MedicinePanel`: Medicine CRUD operations UI
  - `PrescriptionPanel`: OCR prescription processing UI

#### 2. Service Layer
- **Responsibility**: Business logic, validation, orchestration
- **Components**: Service classes for each domain
- **Dependencies**: DAO layer, utility classes
- **Key Classes**:
  - `AuthenticationService`: Credential verification, session management
  - `MedicineService`: Medicine CRUD validation and coordination
  - `OCRService`: Image processing and text extraction
  - `SubstituteService`: Substitute matching algorithm

#### 3. Data Access Layer (DAO Package)
- **Responsibility**: Database operations, SQL execution
- **Components**: DAO classes implementing CRUD operations
- **Dependencies**: Config layer, Model classes
- **Key Classes**:
  - `UserDAO`: User-related database operations
  - `MedicineDAO`: Medicine-related database operations
  - `BaseDAO`: Shared database utilities

#### 4. Configuration Layer
- **Responsibility**: Database connections, app settings
- **Components**: Connection pooling, configuration management
- **Key Classes**:
  - `DatabaseConfig`: MySQL connection management with HikariCP

## Components and Interfaces

### 1. Authentication & Session Management

#### Session Class
```java
public class Session {
    private static Session instance;
    private String username;
    private String fullName;
    private UserRole role;
    private LocalDateTime loginTime;
    
    // Singleton pattern for global session access
    public static Session getInstance() { ... }
    public static void clear() { ... }
}
```

#### AuthenticationService Interface
```java
public interface IAuthenticationService {
    User authenticate(String username, String password) throws AuthenticationException;
    void logout();
    boolean hasRole(UserRole role);
}
```

**Key Responsibilities**:
- Verify credentials against database
- Hash passwords using BCrypt/PBKDF2
- Create session on successful login
- Update last_login timestamp
- Clear session on logout

### 2. Medicine Management

#### Medicine Model
```java
public class Medicine {
    private int medicineId;
    private String medicineName;
    private String genericName;
    private String activeIngredient;
    private String brandName;
    private String manufacturer;
    private String dosageForm;        // e.g., Tablet, Capsule, Syrup
    private String strength;          // e.g., 500mg, 10ml
    private String category;
    private String barcode;
    private BigDecimal sellingPrice;
    private boolean prescriptionRequired;
    private String description;
    private String status;            // ACTIVE, INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

#### MedicineService Interface
```java
public interface IMedicineService {
    void addMedicine(Medicine medicine) throws ValidationException;
    Medicine getMedicineById(int id) throws NotFoundException;
    List<Medicine> getAllMedicines();
    List<Medicine> searchMedicines(String searchTerm);
    void updateMedicine(Medicine medicine) throws ValidationException;
    void deleteMedicine(int id, UserRole role) throws AuthorizationException;
    List<Medicine> findSubstitutes(Medicine medicine);
}
```

**Key Responsibilities**:
- Validate medicine data before persistence
- Enforce business rules (e.g., positive prices)
- Search across multiple fields with partial matching
- Role-based deletion authorization
- Update timestamp management

### 3. Prescription OCR Processing

#### OCRService Interface
```java
public interface IOCRService {
    OCRResult processImage(File imageFile) throws OCRException;
    List<Medicine> matchMedicines(List<String> extractedNames);
}

public class OCRResult {
    private String rawText;
    private List<String> identifiedMedicines;
    private List<String> unmatchedText;
}
```

**Processing Flow**:
1. **Image Upload**: User selects PNG/JPG/JPEG file
2. **Preprocessing**: Convert to grayscale, enhance contrast, noise reduction
3. **OCR Extraction**: Tesseract extracts text from image
4. **Medicine Identification**: Pattern matching to identify medicine names
5. **Database Matching**: Query medicines table for matches
6. **Result Display**: Show matched medicines and unmatched text

**Key Components**:
- **Tesseract Configuration**: Language data path, PSM mode, OEM engine
- **Pattern Matching**: Regex or keyword-based medicine name extraction
- **Error Handling**: Handle corrupted images, poor quality, no text found

### 4. Substitute Suggestion Algorithm

#### Substitute Matching Logic
```java
public List<Medicine> findSubstitutes(Medicine original) {
    // Query: WHERE active_ingredient = ? 
    //        AND strength = ? 
    //        AND dosage_form = ?
    //        AND medicine_id != ?
    //        AND status = 'ACTIVE'
    // ORDER BY selling_price ASC
}
```

**Matching Criteria**:
- **Identical Active Ingredient**: Must match exactly
- **Same Strength**: e.g., both 500mg
- **Same Dosage Form**: e.g., both tablets
- **Exclude Original**: Don't show the source medicine
- **Active Status**: Only show available medicines
- **Price Sorted**: Cheapest alternatives first

### 5. Barcode Lookup

#### BarcodeService Interface
```java
public interface IBarcodeService {
    String decodeBarcode(File imageFile) throws BarcodeException;
    Medicine findByBarcode(String barcode) throws NotFoundException;
}
```

**Processing Flow**:
1. User scans/uploads barcode image
2. ZXing library decodes barcode value
3. Query medicines table by barcode
4. Display medicine details or "not found" message

**Supported Formats**: CODE_128, CODE_39, EAN_13, QR_CODE

### 6. User Interface Components

#### Main Dashboard Layout
```
┌────────────────────────────────────────────────────────────┐
│  Header: [Smart Pharmacy] [User: Admin] [Profile Menu ▼]  │
├───────────┬────────────────────────────────────────────────┤
│ Sidebar   │  Content Area (Dynamic Panel)                 │
│           │                                                 │
│ Dashboard │  - DashboardPanel shows stats                  │
│ Medicines │  - MedicinePanel shows table + CRUD            │
│ Scanner   │  - PrescriptionPanel shows OCR UI              │
│ Logout    │                                                 │
│           │                                                 │
│           │                                                 │
└───────────┴────────────────────────────────────────────────┘
```

#### Custom UI Components

**RoundedPanel**: Panel with rounded corners and shadow
```java
public class RoundedPanel extends JPanel {
    private int cornerRadius = 15;
    // Custom paintComponent with rounded borders
}
```

**CustomButton**: Modern button with hover effects
```java
public class CustomButton extends JButton {
    // Hover state color changes
    // Rounded borders
    // Icon + text support
}
```

**StatusBadge**: Colored badge for status (Active/Inactive)
```java
public class StatusBadge extends JLabel {
    // Color-coded backgrounds: Green (Active), Red (Inactive)
}
```

#### Medicine Table Display
- **JTable** with custom renderer for status badges
- **Pagination**: 20 records per page
- **Sorting**: Click column headers to sort
- **Search**: Real-time filtering as user types
- **Actions**: Edit and Delete buttons per row

## Data Models

### User Entity

| Field         | Type         | Constraints                    |
|---------------|--------------|--------------------------------|
| id            | INT          | PRIMARY KEY, AUTO_INCREMENT    |
| username      | VARCHAR(50)  | UNIQUE, NOT NULL               |
| password_hash | VARCHAR(255) | NOT NULL                       |
| full_name     | VARCHAR(100) | NOT NULL                       |
| role          | ENUM         | 'ADMIN', 'PHARMACIST'          |
| status        | ENUM         | 'ACTIVE', 'INACTIVE'           |
| created_at    | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP      |
| last_login    | TIMESTAMP    | NULL                           |

**Indexes**:
- PRIMARY KEY on `id`
- UNIQUE INDEX on `username`

### Medicine Entity

| Field                | Type          | Constraints                    |
|----------------------|---------------|--------------------------------|
| medicine_id          | INT           | PRIMARY KEY, AUTO_INCREMENT    |
| medicine_name        | VARCHAR(200)  | NOT NULL                       |
| generic_name         | VARCHAR(200)  | NOT NULL                       |
| active_ingredient    | VARCHAR(200)  | NOT NULL                       |
| brand_name           | VARCHAR(100)  | NULL                           |
| manufacturer         | VARCHAR(100)  | NULL                           |
| dosage_form          | VARCHAR(50)   | NOT NULL                       |
| strength             | VARCHAR(50)   | NOT NULL                       |
| category             | VARCHAR(50)   | NULL                           |
| barcode              | VARCHAR(100)  | UNIQUE, NULL                   |
| selling_price        | DECIMAL(10,2) | NOT NULL                       |
| prescription_required| BOOLEAN       | DEFAULT FALSE                  |
| description          | TEXT          | NULL                           |
| status               | ENUM          | 'ACTIVE', 'INACTIVE'           |
| created_at           | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP      |
| updated_at           | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP ON UPDATE |

**Indexes**:
- PRIMARY KEY on `medicine_id`
- INDEX on `medicine_name`
- INDEX on `generic_name`
- INDEX on `active_ingredient`
- UNIQUE INDEX on `barcode`
- COMPOSITE INDEX on `(active_ingredient, strength, dosage_form)` for substitute queries

### Relationships

Currently, the system has two independent tables:
- **users**: Manages authentication and authorization
- **medicines**: Stores medicine inventory

**Future Extensions** (Phase 2):
- **stock**: Links to medicines for inventory tracking
- **suppliers**: Links to medicines for procurement
- **prescriptions**: Links to users and medicines for prescription records
- **bills**: Links to users, medicines for billing operations

## Database Design

### Complete MySQL Schema

```sql
-- Database creation
CREATE DATABASE IF NOT EXISTS pharmacy_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE pharmacy_db;

-- Users table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'PHARMACIST') NOT NULL DEFAULT 'PHARMACIST',
    status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL,
    
    INDEX idx_username (username),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Medicines table
CREATE TABLE medicines (
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
    INDEX idx_composite_substitute (active_ingredient, strength, dosage_form)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed data for users
INSERT INTO users (username, password_hash, full_name, role, status) VALUES
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye/SJ4y9hN7cU3VpQqPdIjVwLpEaJ1V5G', 'Admin User', 'ADMIN', 'ACTIVE'),
('pharmacist1', '$2a$10$N9qo8uLOickgx2ZMRZoMye/SJ4y9hN7cU3VpQqPdIjVwLpEaJ1V5G', 'John Pharmacist', 'PHARMACIST', 'ACTIVE');
-- Default password for both: "password123" (hashed with BCrypt)

-- Seed data for medicines
INSERT INTO medicines (medicine_name, generic_name, active_ingredient, brand_name, manufacturer, dosage_form, strength, category, barcode, selling_price, prescription_required, description, status) VALUES
('Paracetamol 500mg Tablet', 'Paracetamol', 'Paracetamol', 'Dolo', 'Micro Labs', 'Tablet', '500mg', 'Analgesic', '8901234567890', 5.50, FALSE, 'Pain relief and fever reducer', 'ACTIVE'),
('Crocin Advance', 'Paracetamol', 'Paracetamol', 'Crocin', 'GSK', 'Tablet', '500mg', 'Analgesic', '8901234567891', 6.00, FALSE, 'Fast relief from pain and fever', 'ACTIVE'),
('Ibuprofen 400mg Tablet', 'Ibuprofen', 'Ibuprofen', 'Brufen', 'Abbott', 'Tablet', '400mg', 'NSAID', '8901234567892', 12.50, FALSE, 'Anti-inflammatory pain reliever', 'ACTIVE'),
('Amoxicillin 500mg Capsule', 'Amoxicillin', 'Amoxicillin', 'Mox', 'Ranbaxy', 'Capsule', '500mg', 'Antibiotic', '8901234567893', 45.00, TRUE, 'Bacterial infection treatment', 'ACTIVE'),
('Azithromycin 500mg Tablet', 'Azithromycin', 'Azithromycin', 'Azithral', 'Alembic', 'Tablet', '500mg', 'Antibiotic', '8901234567894', 95.00, TRUE, 'Broad-spectrum antibiotic', 'ACTIVE'),
('Cetirizine 10mg Tablet', 'Cetirizine', 'Cetirizine HCl', 'Zyrtec', 'UCB India', 'Tablet', '10mg', 'Antihistamine', '8901234567895', 8.50, FALSE, 'Allergy relief', 'ACTIVE'),
('Omeprazole 20mg Capsule', 'Omeprazole', 'Omeprazole', 'Omez', 'Dr. Reddy\'s', 'Capsule', '20mg', 'PPI', '8901234567896', 22.00, FALSE, 'Acid reflux treatment', 'ACTIVE'),
('Metformin 500mg Tablet', 'Metformin', 'Metformin HCl', 'Glycomet', 'USV Ltd', 'Tablet', '500mg', 'Antidiabetic', '8901234567897', 18.50, TRUE, 'Blood sugar control', 'ACTIVE'),
('Amlodipine 5mg Tablet', 'Amlodipine', 'Amlodipine Besylate', 'Amlong', 'Micro Labs', 'Tablet', '5mg', 'Antihypertensive', '8901234567898', 15.00, TRUE, 'Blood pressure control', 'ACTIVE'),
('Salbutamol Inhaler 100mcg', 'Salbutamol', 'Salbutamol Sulfate', 'Asthalin', 'Cipla', 'Inhaler', '100mcg', 'Bronchodilator', '8901234567899', 120.00, FALSE, 'Asthma relief', 'ACTIVE');
```

### Database Connection Management

**HikariCP Configuration** (Recommended for production):
```java
public class DatabaseConfig {
    private static HikariDataSource dataSource;
    
    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/pharmacy_db");
        config.setUsername("root");
        config.setPassword("password");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        
        dataSource = new HikariDataSource(config);
    }
    
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
```

### Data Access Patterns

#### DAO Base Pattern
```java
public abstract class BaseDAO<T> {
    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
    
    protected void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        // Safe closure of JDBC resources
    }
}
```

#### Example: MedicineDAO
```java
public class MedicineDAO extends BaseDAO<Medicine> {
    
    public void insert(Medicine medicine) throws SQLException {
        String sql = "INSERT INTO medicines (medicine_name, generic_name, " +
                     "active_ingredient, brand_name, manufacturer, dosage_form, " +
                     "strength, category, barcode, selling_price, " +
                     "prescription_required, description, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, medicine.getMedicineName());
            ps.setString(2, medicine.getGenericName());
            ps.setString(3, medicine.getActiveIngredient());
            // ... set remaining parameters
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    medicine.setMedicineId(keys.getInt(1));
                }
            }
        }
    }
    
    public List<Medicine> search(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE " +
                     "medicine_name LIKE ? OR " +
                     "generic_name LIKE ? OR " +
                     "active_ingredient LIKE ? OR " +
                     "manufacturer LIKE ? AND status = 'ACTIVE' " +
                     "ORDER BY medicine_name";
        
        List<Medicine> results = new ArrayList<>();
        String pattern = "%" + searchTerm + "%";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToMedicine(rs));
                }
            }
        }
        
        return results;
    }
}
```

## Security Design

### 1. Password Security

**Hashing Strategy**: BCrypt (Work Factor: 10-12)
```java
public class PasswordUtil {
    private static final int BCRYPT_ROUNDS = 12;
    
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }
    
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
```

**Key Points**:
- Never store plaintext passwords
- Use BCrypt with appropriate work factor (10-12)
- Passwords hashed on registration/password change
- Verification done via BCrypt.checkpw()

### 2. SQL Injection Prevention

**Always use PreparedStatements**:
```java
// GOOD ✓
String sql = "SELECT * FROM users WHERE username = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, username);

// BAD ✗ - Never do this!
String sql = "SELECT * FROM users WHERE username = '" + username + "'";
Statement stmt = conn.createStatement();
stmt.executeQuery(sql);
```

### 3. Input Validation

```java
public class ValidationUtil {
    
    public static void validateMedicine(Medicine medicine) throws ValidationException {
        if (medicine.getMedicineName() == null || medicine.getMedicineName().trim().isEmpty()) {
            throw new ValidationException("Medicine name is required");
        }
        
        if (medicine.getSellingPrice() == null || medicine.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Selling price must be greater than zero");
        }
        
        // Email/phone validation if needed
        // Length constraints
        // Format validation (barcode, etc.)
    }
    
    public static String sanitizeFilePath(String path) {
        // Prevent directory traversal attacks
        return Paths.get(path).normalize().toString();
    }
}
```

### 4. Authorization Checks

```java
public class MedicineService {
    
    public void deleteMedicine(int medicineId) throws AuthorizationException {
        UserRole currentRole = Session.getInstance().getRole();
        
        if (currentRole != UserRole.ADMIN) {
            throw new AuthorizationException("Only administrators can delete medicines");
        }
        
        medicineDAO.delete(medicineId);
    }
}
```

### 5. Resource Management

**Always use try-with-resources**:
```java
try (Connection conn = DatabaseConfig.getConnection();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    
    // Process results
    
} catch (SQLException e) {
    // Handle exception without exposing sensitive data
    logger.error("Database error occurred", e);
    throw new ServiceException("An error occurred. Please try again.");
}
```

### 6. Error Handling

**Never expose sensitive information**:
```java
// GOOD ✓
catch (SQLException e) {
    logger.error("Database error: " + e.getMessage(), e);
    throw new ServiceException("Unable to process request");
}

// BAD ✗
catch (SQLException e) {
    throw new Exception("SQL Error: " + e.getMessage()); // Exposes DB structure
}
```

## Data Flow

### 1. User Login Flow

```
User Input (LoginFrame)
    │
    ├─> Enter username & password
    │
    ▼
AuthenticationService
    │
    ├─> Validate input (not empty)
    │
    ▼
UserDAO
    │
    ├─> Query: SELECT * FROM users WHERE username = ?
    ├─> Retrieve password_hash from database
    │
    ▼
PasswordUtil
    │
    ├─> Verify password using BCrypt
    │
    ▼
AuthenticationService (continued)
    │
    ├─> If valid: Create Session with user details
    ├─> Update last_login timestamp
    ├─> Return User object
    │
    ▼
LoginFrame (continued)
    │
    ├─> Close login window
    ├─> Open MainFrame with dashboard
    └─> Display success message
```

### 2. Add Medicine Flow

```
User Input (MedicinePanel)
    │
    ├─> Click "Add Medicine" button
    ├─> Fill MedicineFormDialog
    │
    ▼
MedicineService
    │
    ├─> Validate medicine data (required fields, price > 0)
    ├─> Check for duplicate barcode
    │
    ▼
MedicineDAO
    │
    ├─> Prepare SQL INSERT statement
    ├─> Execute with PreparedStatement
    ├─> Retrieve generated medicine_id
    │
    ▼
Database (MySQL)
    │
    ├─> Insert record into medicines table
    ├─> Set created_at timestamp
    ├─> Return auto-generated ID
    │
    ▼
MedicinePanel (continued)
    │
    ├─> Refresh table display
    ├─> Show success notification
    └─> Close dialog
```

### 3. Prescription OCR Flow

```
User Input (PrescriptionPanel)
    │
    ├─> Click "Upload Prescription"
    ├─> Select image file (PNG/JPG)
    │
    ▼
ImageUtil
    │
    ├─> Validate file format
    ├─> Preprocess image (grayscale, contrast)
    │
    ▼
OCRService
    │
    ├─> Initialize Tesseract with English data
    ├─> Extract text from image
    ├─> Parse extracted text for medicine names
    │
    ▼
MedicineDAO
    │
    ├─> For each identified medicine name:
    ├─> Query: SELECT * FROM medicines WHERE medicine_name LIKE ?
    ├─> Collect matched and unmatched results
    │
    ▼
PrescriptionPanel (continued)
    │
    ├─> Display matched medicines in table
    ├─> Display unmatched text in separate area
    └─> Prepare data for future billing module
```

### 4. Find Substitutes Flow

```
User Action (MedicinePanel)
    │
    ├─> Select a medicine from table
    ├─> Click "Find Substitutes" button
    │
    ▼
SubstituteService
    │
    ├─> Extract active_ingredient, strength, dosage_form
    │
    ▼
MedicineDAO
    │
    ├─> Query: SELECT * FROM medicines 
    │         WHERE active_ingredient = ?
    │         AND strength = ?
    │         AND dosage_form = ?
    │         AND medicine_id != ?
    │         AND status = 'ACTIVE'
    │         ORDER BY selling_price ASC
    │
    ▼
Database (MySQL)
    │
    ├─> Use composite index for fast lookup
    ├─> Return matching medicines
    │
    ▼
MedicinePanel (continued)
    │
    ├─> Display substitutes in dialog/panel
    ├─> Show: brand name, manufacturer, price
    └─> Allow selection for further actions
```

### 5. Barcode Lookup Flow

```
User Input (MedicinePanel)
    │
    ├─> Click "Scan Barcode"
    ├─> Upload barcode image or use scanner
    │
    ▼
BarcodeService
    │
    ├─> Use ZXing library to decode image
    ├─> Extract barcode value (string)
    │
    ▼
MedicineDAO
    │
    ├─> Query: SELECT * FROM medicines WHERE barcode = ?
    │
    ▼
Database (MySQL)
    │
    ├─> Use unique index on barcode for fast lookup
    ├─> Return medicine record or NULL
    │
    ▼
MedicinePanel (continued)
    │
    ├─> If found: Display medicine details
    └─> If not found: Show "Medicine not in database" message
```

## External Integrations

### 1. Tesseract OCR Integration

**Library**: Tess4J (Java wrapper for Tesseract)

**Maven Dependency**:
```xml
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>5.7.0</version>
</dependency>
```

**Configuration**:
```java
public class OCRService {
    private Tesseract tesseract;
    
    public OCRService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("C:/Program Files/Tesseract-OCR/tessdata");
        tesseract.setLanguage("eng");
        tesseract.setPageSegMode(1);  // Automatic page segmentation with OSD
        tesseract.setOcrEngineMode(1); // Neural nets LSTM engine
    }
    
    public String extractText(File imageFile) throws TesseractException {
        return tesseract.doOCR(imageFile);
    }
}
```

**Image Preprocessing**:
```java
public class ImageUtil {
    
    public static BufferedImage preprocessForOCR(BufferedImage original) {
        // 1. Convert to grayscale
        BufferedImage grayscale = toGrayscale(original);
        
        // 2. Enhance contrast
        BufferedImage contrasted = enhanceContrast(grayscale);
        
        // 3. Reduce noise (optional median filter)
        BufferedImage denoised = reduceNoise(contrasted);
        
        // 4. Binarization (threshold)
        BufferedImage binary = binarize(denoised);
        
        return binary;
    }
}
```

**Medicine Name Extraction**:
- **Pattern Matching**: Use regex to identify medicine name patterns
- **Keyword Recognition**: Maintain a dictionary of common medicine names
- **Fuzzy Matching**: Use Levenshtein distance for partial matches

### 2. ZXing Barcode Integration

**Library**: ZXing Core

**Maven Dependency**:
```xml
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.1</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.1</version>
</dependency>
```

**Implementation**:
```java
public class BarcodeService {
    
    public String decodeBarcode(File imageFile) throws NotFoundException, IOException {
        BufferedImage bufferedImage = ImageIO.read(imageFile);
        LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        
        Result result = new MultiFormatReader().decode(bitmap);
        return result.getText();
    }
}
```

**Supported Formats**:
- CODE_128: Common in pharmacy/retail
- CODE_39: Alphanumeric barcodes
- EAN_13: International article numbers
- QR_CODE: 2D barcodes with more data

## UI Design Details

### Theme Configuration

**FlatLaf Setup**:
```java
public class Main {
    public static void main(String[] args) {
        // Set FlatLaf Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Custom theme properties
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Component.focusWidth", 1);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
```

### Color Scheme

```java
public class UIConstants {
    // Primary colors
    public static final Color PRIMARY = new Color(37, 99, 235);      // Blue-600
    public static final Color PRIMARY_HOVER = new Color(29, 78, 216); // Blue-700
    
    // Status colors
    public static final Color SUCCESS = new Color(34, 197, 94);      // Green-500
    public static final Color DANGER = new Color(239, 68, 68);       // Red-500
    public static final Color WARNING = new Color(251, 191, 36);     // Amber-400
    
    // Neutral colors
    public static final Color BACKGROUND = new Color(249, 250, 251);  // Gray-50
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);   // Gray-900
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128); // Gray-500
    
    // Sidebar
    public static final Color SIDEBAR_BG = new Color(30, 41, 59);     // Slate-800
    public static final Color SIDEBAR_ACTIVE = new Color(51, 65, 85); // Slate-700
}
```

### Login Screen Design

```java
public class LoginFrame extends JFrame {
    /*
     * Layout:
     * - Centered card with rounded corners
     * - Logo/title at top
     * - Username field
     * - Password field
     * - Login button (full width)
     * - Error message area
     */
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private CustomButton loginButton;
    private JLabel errorLabel;
    
    private void initComponents() {
        // Use GroupLayout or MigLayout for precise positioning
        // Apply custom styles from UIConstants
    }
}
```

### Main Dashboard Layout

```java
public class MainFrame extends JFrame {
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private JLabel headerLabel;
    private JLabel userInfoLabel;
    
    private void createSidebar() {
        // Vertical button list:
        // - Dashboard (home icon)
        // - Medicines (pill icon)
        // - Prescription Scanner (document icon)
        // - Logout (exit icon)
        
        // Active state: Different background color
    }
    
    private void switchPanel(JPanel newPanel) {
        contentPanel.removeAll();
        contentPanel.add(newPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}
```

### Medicine Table UI

**Features**:
- Custom table renderer for status badges
- Row selection highlighting
- Action buttons (Edit, Delete) in last column
- Pagination controls at bottom
- Search bar at top with real-time filtering

```java
public class MedicineTableModel extends AbstractTableModel {
    private List<Medicine> medicines;
    private String[] columnNames = {
        "ID", "Name", "Generic Name", "Active Ingredient",
        "Strength", "Price", "Status", "Actions"
    };
    
    // Override getColumnCount, getRowCount, getValueAt
    // Custom renderer for Status column
    // Custom renderer for Actions column with buttons
}
```

### Form Dialogs

**MedicineFormDialog**:
- Modal dialog
- GridBagLayout for form fields
- Input validation on submit
- Cancel and Save buttons

```java
public class MedicineFormDialog extends JDialog {
    private JTextField medicineNameField;
    private JTextField genericNameField;
    private JTextField activeIngredientField;
    // ... other fields
    private CustomButton saveButton;
    private CustomButton cancelButton;
    
    private boolean validateForm() {
        // Check required fields
        // Validate price format
        // Show error messages
    }
}
```

### Loading Indicators

```java
public class LoadingOverlay extends JPanel {
    private JProgressBar progressBar;
    private JLabel messageLabel;
    
    public void show(String message) {
        messageLabel.setText(message);
        this.setVisible(true);
    }
    
    public void hide() {
        this.setVisible(false);
    }
}
```

## Error Handling Strategy

### Exception Hierarchy

```java
// Base application exception
public class PharmacyException extends Exception {
    public PharmacyException(String message) { super(message); }
    public PharmacyException(String message, Throwable cause) { super(message, cause); }
}

// Specific exceptions
public class AuthenticationException extends PharmacyException { }
public class AuthorizationException extends PharmacyException { }
public class ValidationException extends PharmacyException { }
public class NotFoundException extends PharmacyException { }
public class OCRException extends PharmacyException { }
public class BarcodeException extends PharmacyException { }
```

### Error Handling Patterns

**Service Layer**:
```java
public void addMedicine(Medicine medicine) throws ValidationException {
    try {
        // Validate input
        ValidationUtil.validateMedicine(medicine);
        
        // Save to database
        medicineDAO.insert(medicine);
        
    } catch (SQLException e) {
        logger.error("Database error while adding medicine", e);
        throw new ServiceException("Unable to save medicine. Please try again.");
    }
}
```

**UI Layer**:
```java
private void handleAddMedicine() {
    try {
        Medicine medicine = collectFormData();
        medicineService.addMedicine(medicine);
        
        JOptionPane.showMessageDialog(this, 
            "Medicine added successfully", 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
            
        refreshTable();
        
    } catch (ValidationException e) {
        JOptionPane.showMessageDialog(this, 
            e.getMessage(), 
            "Validation Error", 
            JOptionPane.WARNING_MESSAGE);
            
    } catch (Exception e) {
        logger.error("Unexpected error", e);
        JOptionPane.showMessageDialog(this, 
            "An unexpected error occurred", 
            "Error", 
            JOptionPane.ERROR_MESSAGE);
    }
}
```

## Testing Strategy

### Unit Tests

**Focus Areas**:
- Validation logic in `ValidationUtil`
- Password hashing in `PasswordUtil`
- Business logic in Service classes
- Medicine substitute matching algorithm

**Example Test**:
```java
@Test
public void testPasswordHashing() {
    String plainPassword = "password123";
    String hashed = PasswordUtil.hashPassword(plainPassword);
    
    assertNotNull(hashed);
    assertNotEquals(plainPassword, hashed);
    assertTrue(PasswordUtil.verifyPassword(plainPassword, hashed));
    assertFalse(PasswordUtil.verifyPassword("wrongpassword", hashed));
}

@Test
public void testMedicineValidation() {
    Medicine medicine = new Medicine();
    
    // Test missing required field
    assertThrows(ValidationException.class, () -> {
        ValidationUtil.validateMedicine(medicine);
    });
    
    // Test negative price
    medicine.setMedicineName("Test Med");
    medicine.setSellingPrice(BigDecimal.valueOf(-10));
    assertThrows(ValidationException.class, () -> {
        ValidationUtil.validateMedicine(medicine);
    });
}
```

### Integration Tests

**Focus Areas**:
- Database CRUD operations in DAO classes
- Authentication flow (login/logout)
- OCR processing with sample images
- Barcode decoding with sample barcodes

**Example Test**:
```java
@Test
public void testUserAuthentication() {
    // Given: Valid user credentials
    String username = "testuser";
    String password = "password123";
    
    // When: Authenticate
    User user = authService.authenticate(username, password);
    
    // Then: Session should be created
    assertNotNull(user);
    assertEquals(username, Session.getInstance().getUsername());
}

@Test
public void testMedicineSearch() {
    // Given: Medicines in database
    // When: Search by name
    List<Medicine> results = medicineService.searchMedicines("Paracetamol");
    
    // Then: Should return matching medicines
    assertFalse(results.isEmpty());
    assertTrue(results.stream()
        .allMatch(m -> m.getMedicineName().contains("Paracetamol")));
}
```

### UI Tests (Manual)

**Test Cases**:
1. Login with valid/invalid credentials
2. Navigate between sidebar menu items
3. Add new medicine with all fields
4. Search medicines with various terms
5. Edit existing medicine
6. Delete medicine (Admin only)
7. Find substitutes for a medicine
8. Upload prescription image for OCR
9. Scan barcode and lookup medicine
10. Logout and verify session cleared

## Future Extensibility

### Modular Architecture

The current design supports easy addition of new modules:

```
Phase 1 (Current): Core + Medicine Management
Phase 2: Stock Management Module
Phase 3: Billing Module
Phase 4: Supplier Management Module
Phase 5: Reports & Analytics Module
```

### Adding Stock Management Module

**New Tables**:
```sql
CREATE TABLE stock (
    stock_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,
    batch_number VARCHAR(50),
    manufacturing_date DATE,
    expiry_date DATE,
    purchase_price DECIMAL(10,2),
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
);
```

**New Components**:
- `model/Stock.java`
- `dao/StockDAO.java`
- `service/StockService.java`
- `ui/StockPanel.java`

**Integration Points**:
- Medicine panel shows stock levels
- Low stock alerts on dashboard
- Stock updates on medicine sales

### Adding Billing Module

**New Tables**:
```sql
CREATE TABLE bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    customer_name VARCHAR(100),
    total_amount DECIMAL(10,2),
    discount DECIMAL(5,2),
    final_amount DECIMAL(10,2),
    payment_method ENUM('CASH', 'CARD', 'UPI'),
    bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE bill_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id INT NOT NULL,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2),
    total_price DECIMAL(10,2),
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id),
    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
);
```

**Integration with OCR**:
- Prescription OCR results can populate a new bill
- Identified medicines added to bill items
- Generate printable bill

### Adding Supplier Management Module

**New Tables**:
```sql
CREATE TABLE suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(15),
    email VARCHAR(100),
    address TEXT,
    status ENUM('ACTIVE', 'INACTIVE')
);

CREATE TABLE purchase_orders (
    po_id INT AUTO_INCREMENT PRIMARY KEY,
    supplier_id INT NOT NULL,
    order_date DATE,
    delivery_date DATE,
    total_amount DECIMAL(10,2),
    status ENUM('PENDING', 'DELIVERED', 'CANCELLED'),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);
```

### Plugin Architecture (Future)

**Design for extensibility**:
```java
public interface PharmacyModule {
    String getModuleName();
    JPanel getModulePanel();
    void onActivate();
    void onDeactivate();
}

public class ModuleManager {
    private Map<String, PharmacyModule> modules = new HashMap<>();
    
    public void registerModule(PharmacyModule module) {
        modules.put(module.getModuleName(), module);
    }
    
    public JPanel getModulePanel(String moduleName) {
        PharmacyModule module = modules.get(moduleName);
        return module != null ? module.getModulePanel() : null;
    }
}
```

This allows third-party modules to be added without modifying core code.

## Performance Considerations

### Database Optimization

1. **Indexes**: Already defined on frequently queried columns
2. **Connection Pooling**: HikariCP for efficient connection reuse
3. **Prepared Statement Caching**: Reuse compiled SQL statements
4. **Batch Operations**: For bulk inserts/updates

### UI Responsiveness

1. **Background Tasks**: Use SwingWorker for long-running operations
```java
private void loadMedicines() {
    new SwingWorker<List<Medicine>, Void>() {
        @Override
        protected List<Medicine> doInBackground() throws Exception {
            return medicineService.getAllMedicines();
        }
        
        @Override
        protected void done() {
            try {
                List<Medicine> medicines = get();
                updateTable(medicines);
            } catch (Exception e) {
                handleError(e);
            }
        }
    }.execute();
}
```

2. **Lazy Loading**: Load data on-demand rather than all at once
3. **Pagination**: Display limited records per page
4. **Debouncing**: For search operations, delay query execution

### OCR Optimization

1. **Image Preprocessing**: Enhance image quality before OCR
2. **Region of Interest**: Extract only relevant portions of prescription
3. **Caching**: Cache OCR results to avoid reprocessing

## Deployment

### Build Configuration

**Maven pom.xml** (key dependencies):
```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.pharmacy</groupId>
    <artifactId>pharmacy-management</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>
    
    <dependencies>
        <!-- MySQL Connector -->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>8.0.33</version>
        </dependency>
        
        <!-- HikariCP for connection pooling -->
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.0.1</version>
        </dependency>
        
        <!-- FlatLaf Look and Feel -->
        <dependency>
            <groupId>com.formdev</groupId>
            <artifactId>flatlaf</artifactId>
            <version>3.2.5</version>
        </dependency>
        
        <!-- Tesseract OCR -->
        <dependency>
            <groupId>net.sourceforge.tess4j</groupId>
            <artifactId>tess4j</artifactId>
            <version>5.7.0</version>
        </dependency>
        
        <!-- ZXing Barcode -->
        <dependency>
            <groupId>com.google.zxing</groupId>
            <artifactId>core</artifactId>
            <version>3.5.1</version>
        </dependency>
        <dependency>
            <groupId>com.google.zxing</groupId>
            <artifactId>javase</artifactId>
            <version>3.5.1</version>
        </dependency>
        
        <!-- BCrypt for password hashing -->
        <dependency>
            <groupId>org.mindrot</groupId>
            <artifactId>jbcrypt</artifactId>
            <version>0.4</version>
        </dependency>
        
        <!-- Logging -->
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>2.0.9</version>
        </dependency>
        <dependency>
            <groupId>ch.qos.logback</groupId>
            <artifactId>logback-classic</artifactId>
            <version>1.4.11</version>
        </dependency>
        
        <!-- JUnit for testing -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <!-- Maven Shade Plugin for creating fat JAR -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.0</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.pharmacy.management.Main</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

### Prerequisites

1. **JDK 17+**: Required to run the application
2. **MySQL 8.0+**: Database server
3. **Tesseract OCR**: Install Tesseract and configure path
4. **Maven**: For building the project

### Installation Steps

1. Clone/download project repository
2. Install MySQL and create database using provided schema
3. Update `DatabaseConfig.java` with MySQL credentials
4. Install Tesseract OCR and update data path in `OCRService.java`
5. Build project: `mvn clean package`
6. Run application: `java -jar target/pharmacy-management-1.0.0.jar`

### Configuration Files

**application.properties** (optional for externalized config):
```properties
# Database
db.url=jdbc:mysql://localhost:3306/pharmacy_db
db.username=root
db.password=password
db.pool.size=10

# Tesseract
ocr.datapath=C:/Program Files/Tesseract-OCR/tessdata
ocr.language=eng

# Application
app.name=Smart Pharmacy Management System
app.version=1.0.0
```

---

*This design document provides a comprehensive blueprint for implementing the Smart Pharmacy Management System. It covers all architectural layers, security considerations, data flows, and future extensibility while maintaining clean separation of concerns and industry best practices.*
