package com.pharmacy.management.dao;

import com.pharmacy.management.model.Medicine;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Medicine Data Access Object.
 * Requirements: 5.1, 5.3, 5.4, 5.5, 6.2, 7.1, 8.2, 11.1, 11.3
 */
public class MedicineDAO extends BaseDAO {
    
    public void insert(Medicine medicine) throws SQLException {
        String sql = "INSERT INTO medicines (medicine_name, generic_name, active_ingredient, " +
                     "brand_name, manufacturer, dosage_form, strength, category, barcode, " +
                     "selling_price, prescription_required, description, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, medicine.getMedicineName());
            ps.setString(2, medicine.getGenericName());
            ps.setString(3, medicine.getActiveIngredient());
            ps.setString(4, medicine.getBrandName());
            ps.setString(5, medicine.getManufacturer());
            ps.setString(6, medicine.getDosageForm());
            ps.setString(7, medicine.getStrength());
            ps.setString(8, medicine.getCategory());
            ps.setString(9, medicine.getBarcode());
            ps.setBigDecimal(10, medicine.getSellingPrice());
            ps.setBoolean(11, medicine.isPrescriptionRequired());
            ps.setString(12, medicine.getDescription());
            ps.setString(13, medicine.getStatus() != null ? medicine.getStatus() : "ACTIVE");
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    medicine.setMedicineId(keys.getInt(1));
                }
            }
        }
    }
    
    public Medicine findById(int medicineId) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE medicine_id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, medicineId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedicine(rs);
                }
            }
        }
        return null;
    }
    
    public List<Medicine> findAll() throws SQLException {
        String sql = "SELECT * FROM medicines ORDER BY medicine_name";
        List<Medicine> medicines = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                medicines.add(mapResultSetToMedicine(rs));
            }
        }
        return medicines;
    }
    
    public List<Medicine> search(String searchTerm) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE " +
                     "medicine_name LIKE ? OR generic_name LIKE ? OR " +
                     "active_ingredient LIKE ? OR manufacturer LIKE ? " +
                     "ORDER BY medicine_name";
        List<Medicine> medicines = new ArrayList<>();
        String pattern = "%" + searchTerm + "%";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapResultSetToMedicine(rs));
                }
            }
        }
        return medicines;
    }
    
    public void update(Medicine medicine) throws SQLException {
        String sql = "UPDATE medicines SET medicine_name=?, generic_name=?, active_ingredient=?, " +
                     "brand_name=?, manufacturer=?, dosage_form=?, strength=?, category=?, " +
                     "barcode=?, selling_price=?, prescription_required=?, description=?, " +
                     "status=?, updated_at=? WHERE medicine_id=?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, medicine.getMedicineName());
            ps.setString(2, medicine.getGenericName());
            ps.setString(3, medicine.getActiveIngredient());
            ps.setString(4, medicine.getBrandName());
            ps.setString(5, medicine.getManufacturer());
            ps.setString(6, medicine.getDosageForm());
            ps.setString(7, medicine.getStrength());
            ps.setString(8, medicine.getCategory());
            ps.setString(9, medicine.getBarcode());
            ps.setBigDecimal(10, medicine.getSellingPrice());
            ps.setBoolean(11, medicine.isPrescriptionRequired());
            ps.setString(12, medicine.getDescription());
            ps.setString(13, medicine.getStatus());
            ps.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(15, medicine.getMedicineId());
            
            ps.executeUpdate();
        }
    }
    
    public void delete(int medicineId) throws SQLException {
        String sql = "UPDATE medicines SET status='INACTIVE' WHERE medicine_id=?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, medicineId);
            ps.executeUpdate();
        }
    }
    
    public Medicine findByBarcode(String barcode) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE barcode = ? AND status = 'ACTIVE'";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, barcode);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMedicine(rs);
                }
            }
        }
        return null;
    }
    
    public List<Medicine> findSubstitutes(String activeIngredient, String strength, String dosageForm, int excludeId) throws SQLException {
        String sql = "SELECT * FROM medicines WHERE active_ingredient = ? AND strength = ? " +
                     "AND dosage_form = ? AND medicine_id != ? AND status = 'ACTIVE' " +
                     "ORDER BY selling_price ASC";
        List<Medicine> medicines = new ArrayList<>();
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, activeIngredient);
            ps.setString(2, strength);
            ps.setString(3, dosageForm);
            ps.setInt(4, excludeId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    medicines.add(mapResultSetToMedicine(rs));
                }
            }
        }
        return medicines;
    }
    
    private Medicine mapResultSetToMedicine(ResultSet rs) throws SQLException {
        Medicine medicine = new Medicine();
        
        medicine.setMedicineId(rs.getInt("medicine_id"));
        medicine.setMedicineName(rs.getString("medicine_name"));
        medicine.setGenericName(rs.getString("generic_name"));
        medicine.setActiveIngredient(rs.getString("active_ingredient"));
        medicine.setBrandName(rs.getString("brand_name"));
        medicine.setManufacturer(rs.getString("manufacturer"));
        medicine.setDosageForm(rs.getString("dosage_form"));
        medicine.setStrength(rs.getString("strength"));
        medicine.setCategory(rs.getString("category"));
        medicine.setBarcode(rs.getString("barcode"));
        medicine.setSellingPrice(rs.getBigDecimal("selling_price"));
        medicine.setPrescriptionRequired(rs.getBoolean("prescription_required"));
        medicine.setDescription(rs.getString("description"));
        medicine.setStatus(rs.getString("status"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            medicine.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            medicine.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return medicine;
    }
}