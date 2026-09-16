# Smart Pharmacy Management System

A comprehensive Java desktop application for pharmacy operations management with OCR prescription processing and barcode scanning capabilities.

## 🚀 Features

- **Secure Authentication**: Role-based access control (Admin/Pharmacist)
- **Medicine Management**: Full CRUD operations with search and filtering
- **Prescription OCR**: Extract medicine names from prescription images
- **Barcode Scanning**: Quick medicine lookup via barcode/QR codes
- **Substitute Suggestions**: Find alternative medicines with same active ingredients
- **Modern UI**: Professional interface using FlatLaf Look and Feel

## 🛠️ Technology Stack

- **Java**: JDK 17+
- **Database**: MySQL 8.0+
- **UI Framework**: Java Swing with FlatLaf
- **OCR**: Tesseract via Tess4J
- **Barcode**: ZXing (Zebra Crossing)
- **Build Tool**: Maven
- **Connection Pool**: HikariCP
- **Security**: BCrypt password hashing
- **Logging**: SLF4J + Logback

## 📁 Project Structure

```
pharmacy-management/
├── src/
│   ├── main/
│   │   ├── java/com/pharmacy/management/
│   │   │   ├── config/          # Database and app configuration
│   │   │   ├── model/           # Entity classes (User, Medicine, Session)
│   │   │   ├── dao/             # Data Access Objects
│   │   │   ├── service/         # Business logic layer
│   │   │   ├── ui/              # Swing UI components
│   │   │   │   ├── components/  # Reusable UI components
│   │   │   │   └── dialogs/     # Dialog windows
│   │   │   ├── util/            # Utility classes
│   │   │   └── Main.java        # Application entry point
│   │   └── resources/
│   │       └── logback.xml      # Logging configuration
│   └── test/
│       └── java/                # Unit tests
├── pom.xml                      # Maven configuration
└── README.md
```

## 📦 Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| MySQL Connector | 8.0.33 | Database connectivity |
| HikariCP | 5.0.1 | Connection pooling |
| FlatLaf | 3.2.5 | Modern Look and Feel |
| Tess4J | 5.7.0 | OCR processing |
| ZXing | 3.5.2 | Barcode scanning |
| BCrypt | 0.10.2 | Password hashing |
| SLF4J | 2.0.9 | Logging API |
| Logback | 1.4.11 | Logging implementation |

## 🔧 Prerequisites

1. **Java Development Kit (JDK) 17 or higher**
   ```bash
   java -version
   ```

2. **Maven 3.6+**
   ```bash
   mvn -version
   ```

3. **MySQL 8.0+**
   - Ensure MySQL server is running
   - Create database: `pharmacy_db`

4. **Tesseract OCR** (for prescription scanning)
   - Download from: https://github.com/tesseract-ocr/tesseract
   - Install and add to system PATH

## 🚀 Getting Started

### 1. Clone or Download the Project

```bash
cd pharmacy-management
```

### 2. Configure Database

Update database credentials in `src/main/java/com/pharmacy/management/config/DatabaseConfig.java`:

```java
config.setJdbcUrl("jdbc:mysql://localhost:3306/pharmacy_db");
config.setUsername("root");
config.setPassword("your_password");
```

### 3. Build the Project

```bash
mvn clean install
```

### 4. Run the Application

```bash
mvn exec:java -Dexec.mainClass="com.pharmacy.management.Main"
```

Or run the compiled JAR:

```bash
java -jar target/pharmacy-management-1.0-SNAPSHOT.jar
```

## 📝 Default Credentials

| Username | Password | Role |
|----------|----------|------|
| admin | password123 | ADMIN |
| pharmacist1 | password123 | PHARMACIST |

**⚠️ Change default passwords after first login!**

## 🏗️ Build Commands

### Compile
```bash
mvn compile
```

### Run Tests
```bash
mvn test
```

### Package (Create Fat JAR)
```bash
mvn package
```

The executable JAR will be created in `target/pharmacy-management-1.0-SNAPSHOT.jar`

### Clean Build
```bash
mvn clean package
```

## 📊 Database Setup

### Quick Setup

1. **Install MySQL** (if not already installed)
   - Download from: https://dev.mysql.com/downloads/
   - Or use XAMPP: https://www.apachefriends.org/

2. **Execute the schema file**
   ```bash
   mysql -u root -p < src/main/resources/schema.sql
   ```

3. **Verify installation**
   ```bash
   mysql -u root -p < verify-database.sql
   ```

The schema automatically creates:
- ✅ `pharmacy_db` database
- ✅ `users` table with 2 seed users (admin & pharmacist1)
- ✅ `medicines` table with 10 sample medicines
- ✅ All required indexes for optimal performance
- ✅ BCrypt hashed passwords for security

**For detailed setup instructions, see [DATABASE_SETUP.md](DATABASE_SETUP.md)**

## 🎨 UI Components

- **Login Screen**: Secure authentication
- **Dashboard**: Summary statistics and quick access
- **Medicine Management**: Add, edit, search, delete medicines
- **Prescription Scanner**: Upload and process prescription images
- **Barcode Lookup**: Scan barcodes for quick medicine info

## 🔐 Security Features

- BCrypt password hashing (work factor: 12)
- SQL injection prevention via PreparedStatements
- Role-based access control
- Secure session management
- Input validation and sanitization

## 📄 License

This project is developed for educational purposes.

## 👥 Authors

Pharmacy Management System Development Team

## 🤝 Contributing

1. Follow the existing code structure
2. Write unit tests for new features
3. Maintain clean code practices
4. Document your changes

---

**Note**: This is Task 1 of the pharmacy management system implementation. Future tasks will add the actual Java classes and business logic.
