# 🗄️ Database Setup Guide - MySQL Installation & Schema Execution

## 📋 Task 2 Status: Schema Created ✅

The complete MySQL schema file has been created at:
```
src/main/resources/schema.sql
```

This file includes:
- ✅ Database creation (`pharmacy_db`)
- ✅ `users` table with all required columns and indexes
- ✅ `medicines` table with all required columns and indexes
- ✅ 2 seed users (admin & pharmacist1) with BCrypt hashed passwords
- ✅ 10 sample medicines across various categories
- ✅ Proper indexes for performance optimization

---

## 🚀 MySQL Installation Steps

### Option 1: MySQL Community Server (Recommended)

1. **Download MySQL Community Server**
   - Visit: https://dev.mysql.com/downloads/mysql/
   - Select Windows version
   - Download the installer (mysql-installer-community-*.msi)

2. **Run the Installer**
   - Choose "Custom" installation type
   - Select components:
     - ✅ MySQL Server 8.0+
     - ✅ MySQL Workbench (GUI tool - optional but helpful)
     - ✅ MySQL Shell (optional)

3. **Configure MySQL Server**
   - **Port**: 3306 (default)
   - **Root Password**: Set a strong password (remember this!)
   - **Windows Service**: Enable "Start MySQL Server at System Startup"
   - **Authentication Method**: Use Strong Password Encryption

4. **Add MySQL to PATH** (Important!)
   - Go to System Environment Variables
   - Add to PATH: `C:\Program Files\MySQL\MySQL Server 8.0\bin`
   - Restart terminal/PowerShell

### Option 2: XAMPP (Easier for Development)

1. **Download XAMPP**
   - Visit: https://www.apachefriends.org/
   - Download Windows installer

2. **Install XAMPP**
   - Run installer (default location: `C:\xampp`)
   - Select Apache and MySQL components

3. **Start MySQL**
   - Open XAMPP Control Panel
   - Click "Start" next to MySQL
   - MySQL runs on port 3306

4. **Add to PATH** (Optional)
   - Add to PATH: `C:\xampp\mysql\bin`

---

## 🔧 Execute the Schema

### Method 1: Using MySQL Command Line

```bash
# Navigate to project directory
cd C:\Users\Muthulakshmi\thotho

# Execute the schema file
mysql -u root -p < src\main\resources\schema.sql

# Enter your MySQL root password when prompted
```

### Method 2: Using MySQL Workbench (GUI)

1. Open MySQL Workbench
2. Connect to your local MySQL instance (localhost:3306)
3. Click "File" → "Open SQL Script"
4. Navigate to `src\main\resources\schema.sql`
5. Click the lightning bolt icon (Execute) or press Ctrl+Shift+Enter
6. Verify in the output that all commands executed successfully

### Method 3: Using XAMPP phpMyAdmin

1. Open XAMPP Control Panel
2. Ensure MySQL is running
3. Click "Admin" next to MySQL (opens phpMyAdmin in browser)
4. Click "SQL" tab at the top
5. Copy the contents of `schema.sql` and paste into the SQL editor
6. Click "Go" to execute

---

## ✅ Verify Installation

After executing the schema, run these commands to verify:

```sql
-- Check database exists
SHOW DATABASES;

-- Use the database
USE pharmacy_db;

-- Check tables
SHOW TABLES;

-- Verify users (should show 2 users)
SELECT username, role, status FROM users;

-- Verify medicines (should show 10 medicines)
SELECT COUNT(*) AS total_medicines FROM medicines;

-- View sample medicines
SELECT medicine_name, category, selling_price, prescription_required 
FROM medicines 
ORDER BY category, medicine_name;
```

### Expected Results:

**Users Table:**
| username     | role        | status  |
|--------------|-------------|---------|
| admin        | ADMIN       | ACTIVE  |
| pharmacist1  | PHARMACIST  | ACTIVE  |

**Medicines Count:** 10 medicines

**Sample Medicines by Category:**
- Analgesic: Paracetamol variants (Dolo, Crocin)
- NSAID: Ibuprofen
- Antibiotic: Amoxicillin, Azithromycin
- Antihistamine: Cetirizine
- PPI: Omeprazole
- Antidiabetic: Metformin
- Antihypertensive: Amlodipine
- Bronchodilator: Salbutamol Inhaler

---

## 🔐 Default Credentials

**Admin User:**
- Username: `admin`
- Password: `password123`
- Role: ADMIN (full access including delete)

**Pharmacist User:**
- Username: `pharmacist1`
- Password: `password123`
- Role: PHARMACIST (cannot delete medicines)

> **Security Note:** The password "password123" is hashed using BCrypt with work factor 10. 
> The hash stored in the database is: `$2a$10$N9qo8uLOickgx2ZMRZoMye/SJ4y9hN7cU3VpQqPdIjVwLpEaJ1V5G`

---

## 🗂️ Database Schema Details

### `users` Table Structure

| Column        | Type         | Constraints                    |
|---------------|--------------|--------------------------------|
| id            | INT          | PRIMARY KEY, AUTO_INCREMENT    |
| username      | VARCHAR(50)  | UNIQUE, NOT NULL               |
| password_hash | VARCHAR(255) | NOT NULL                       |
| full_name     | VARCHAR(100) | NOT NULL                       |
| role          | ENUM         | 'ADMIN', 'PHARMACIST'          |
| status        | ENUM         | 'ACTIVE', 'INACTIVE'           |
| created_at    | TIMESTAMP    | DEFAULT CURRENT_TIMESTAMP      |
| last_login    | TIMESTAMP    | NULL                           |

**Indexes:**
- PRIMARY KEY on `id`
- UNIQUE INDEX on `username`
- INDEX on `status`
- INDEX on `role`

### `medicines` Table Structure

| Column               | Type          | Constraints                    |
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
| selling_price        | DECIMAL(10,2) | NOT NULL, CHECK (>0)           |
| prescription_required| BOOLEAN       | DEFAULT FALSE                  |
| description          | TEXT          | NULL                           |
| status               | ENUM          | 'ACTIVE', 'INACTIVE'           |
| created_at           | TIMESTAMP     | DEFAULT CURRENT_TIMESTAMP      |
| updated_at           | TIMESTAMP     | AUTO UPDATE                    |

**Indexes:**
- PRIMARY KEY on `medicine_id`
- UNIQUE INDEX on `barcode`
- INDEX on `medicine_name`
- INDEX on `generic_name`
- INDEX on `active_ingredient`
- INDEX on `status`
- INDEX on `category`
- COMPOSITE INDEX on `(active_ingredient, strength, dosage_form)` for substitute queries

---

## 🧪 Test Queries

After schema execution, you can test with these queries:

```sql
-- Test substitute medicine search (should find 2 paracetamol 500mg tablets)
SELECT medicine_name, brand_name, selling_price 
FROM medicines 
WHERE active_ingredient = 'Paracetamol' 
  AND strength = '500mg' 
  AND dosage_form = 'Tablet' 
  AND status = 'ACTIVE';

-- Test barcode lookup
SELECT * FROM medicines WHERE barcode = '8901234567890';

-- Test search functionality (partial matching)
SELECT medicine_name, category, selling_price 
FROM medicines 
WHERE medicine_name LIKE '%para%' 
   OR generic_name LIKE '%para%';

-- Test prescription required filter
SELECT medicine_name, category, prescription_required 
FROM medicines 
WHERE prescription_required = TRUE;
```

---

## 🔄 Connection Configuration

Once MySQL is installed, update your Java application's database connection in:
- File: `src/main/java/com/pharmacy/management/config/DatabaseConfig.java`

```java
// Default connection parameters
JDBC URL: jdbc:mysql://localhost:3306/pharmacy_db
Username: root
Password: [your MySQL root password]
```

For production, use environment variables or a properties file:
```properties
# application.properties
db.url=jdbc:mysql://localhost:3306/pharmacy_db
db.username=root
db.password=your_password_here
```

---

## ⚠️ Troubleshooting

### Issue: "Command 'mysql' not recognized"
**Solution:** Add MySQL bin directory to system PATH and restart terminal

### Issue: "Access denied for user 'root'@'localhost'"
**Solution:** Check your root password or reset it using MySQL installer

### Issue: "Can't connect to MySQL server on 'localhost'"
**Solution:** Ensure MySQL service is running:
```bash
# Check service status
Get-Service | Where-Object { $_.Name -like "*mysql*" }

# Start service if stopped
net start MySQL80  # Adjust service name as needed
```

### Issue: "Table already exists" error
**Solution:** The schema drops and recreates the database. If you want to keep existing data, remove the `DROP DATABASE` line from schema.sql

---

## 📊 Requirements Coverage

This schema file satisfies the following requirements from the spec:

- ✅ **Requirement 10.1**: Users table with all specified columns
- ✅ **Requirement 10.2**: Medicines table with all specified columns
- ✅ **Requirement 10.3**: Unique constraints on username and barcode
- ✅ **Requirement 10.4**: Indexes on frequently queried fields
- ✅ **Requirement 10.5**: Seed data with 1 Admin, 1 Pharmacist, 10 medicines

---

## 🎯 Next Steps

After completing database setup:

1. ✅ MySQL installed and running
2. ✅ Schema executed successfully
3. ✅ Seed data verified
4. ➡️ **Next Task**: Implement DatabaseConfig.java (Task 3)
5. ➡️ Create DAO layer for database operations
6. ➡️ Build authentication service

---

## 📞 Need Help?

If you encounter any issues:
1. Check MySQL error logs (usually in `C:\ProgramData\MySQL\MySQL Server 8.0\Data\`)
2. Verify MySQL service is running
3. Test connection with MySQL Workbench first before connecting from Java
4. Ensure firewall allows MySQL on port 3306

---

**Status:** ✅ Schema file created and ready for execution
**File Location:** `src/main/resources/schema.sql`
**Date Created:** Task 2 completion
