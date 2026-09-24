package com.pharmacy.management.dao;

import com.pharmacy.management.model.MedicinePurchase;
import com.pharmacy.management.model.Supplier;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Supplier and MedicinePurchase operations.
 * Module 5 - Supplier Management
 */
public class SupplierDAO extends BaseDAO {

    // =========================================================================
    // SUPPLIER CRUD
    // =========================================================================

    /** Insert a new supplier. Populates supplierId on success. */
    public void insertSupplier(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO suppliers (supplier_name, company_name, phone, email, " +
                     "address, gst_number, status) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getCompanyName());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setString(6, supplier.getGstNumber());
            ps.setString(7, supplier.getStatus() != null ? supplier.getStatus() : "ACTIVE");
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) supplier.setSupplierId(keys.getInt(1));
            }
            logger.info("Supplier inserted: {}", supplier.getSupplierName());
        }
    }

    /** Get all suppliers ordered by name. */
    public List<Supplier> findAllSuppliers() throws SQLException {
        String sql = "SELECT * FROM suppliers ORDER BY supplier_name";
        List<Supplier> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapSupplier(rs));
        }
        return list;
    }

    /** Get active suppliers only (for combo boxes). */
    public List<Supplier> findActiveSuppliers() throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE status = 'ACTIVE' ORDER BY supplier_name";
        List<Supplier> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapSupplier(rs));
        }
        return list;
    }

    /** Find supplier by ID. */
    public Supplier findSupplierById(int supplierId) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE supplier_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapSupplier(rs);
            }
        }
        return null;
    }

    /** Search suppliers by name, company, phone, or email. */
    public List<Supplier> searchSuppliers(String term) throws SQLException {
        String sql = "SELECT * FROM suppliers WHERE " +
                     "supplier_name LIKE ? OR company_name LIKE ? OR phone LIKE ? OR email LIKE ? " +
                     "ORDER BY supplier_name";
        String p = "%" + term + "%";
        List<Supplier> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p); ps.setString(2, p);
            ps.setString(3, p); ps.setString(4, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapSupplier(rs));
            }
        }
        return list;
    }

    /** Update existing supplier. */
    public void updateSupplier(Supplier supplier) throws SQLException {
        String sql = "UPDATE suppliers SET supplier_name=?, company_name=?, phone=?, email=?, " +
                     "address=?, gst_number=?, status=?, updated_at=? WHERE supplier_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getCompanyName());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.setString(6, supplier.getGstNumber());
            ps.setString(7, supplier.getStatus());
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(9, supplier.getSupplierId());
            ps.executeUpdate();
            logger.info("Supplier updated: id={}", supplier.getSupplierId());
        }
    }

    /** Soft-delete by setting status INACTIVE. */
    public void deactivateSupplier(int supplierId) throws SQLException {
        String sql = "UPDATE suppliers SET status='INACTIVE', updated_at=? WHERE supplier_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, supplierId);
            ps.executeUpdate();
            logger.info("Supplier deactivated: id={}", supplierId);
        }
    }

    /** Check whether a supplier name + company combo already exists. */
    public boolean supplierExists(String supplierName, String companyName, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM suppliers WHERE supplier_name=? AND company_name=? AND supplier_id<>?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplierName);
            ps.setString(2, companyName);
            ps.setInt(3, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    /** Count total suppliers. */
    public int countSuppliers() throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM suppliers WHERE status='ACTIVE'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // =========================================================================
    // MEDICINE PURCHASE CRUD
    // =========================================================================

    /**
     * Insert a purchase record AND create/update the stock record in a single transaction.
     * The medicine master record is NOT duplicated.
     */
    public void insertPurchase(Connection conn, MedicinePurchase purchase, int stockId) throws SQLException {
        String sql = "INSERT INTO medicine_purchases (supplier_id, medicine_id, batch_number, quantity, " +
                     "purchase_price, selling_price, manufacturing_date, expiry_date, minimum_stock_level, " +
                     "purchase_date, performed_by, notes) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, purchase.getSupplierId());
            ps.setInt(2, purchase.getMedicineId());
            ps.setString(3, purchase.getBatchNumber());
            ps.setInt(4, purchase.getQuantity());
            ps.setBigDecimal(5, purchase.getPurchasePrice());
            if (purchase.getSellingPrice() != null) ps.setBigDecimal(6, purchase.getSellingPrice());
            else ps.setNull(6, Types.DECIMAL);
            if (purchase.getManufacturingDate() != null) ps.setDate(7, Date.valueOf(purchase.getManufacturingDate()));
            else ps.setNull(7, Types.DATE);
            ps.setDate(8, Date.valueOf(purchase.getExpiryDate()));
            ps.setInt(9, purchase.getMinimumStockLevel());
            ps.setTimestamp(10, Timestamp.valueOf(
                    purchase.getPurchaseDate() != null ? purchase.getPurchaseDate() : LocalDateTime.now()));
            ps.setString(11, purchase.getPerformedBy());
            ps.setString(12, purchase.getNotes());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) purchase.setPurchaseId(keys.getInt(1));
            }
        }
    }

    /** Insert a new stock row for this purchase batch (or update quantity if batch already exists). */
    public int upsertStock(Connection conn, MedicinePurchase purchase, int supplierId) throws SQLException {
        // Check if this exact batch already exists
        String checkSql = "SELECT stock_id, quantity FROM stock WHERE medicine_id=? AND batch_number=?";
        try (PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setInt(1, purchase.getMedicineId());
            check.setString(2, purchase.getBatchNumber());
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    // Batch exists → increase quantity
                    int existingId  = rs.getInt("stock_id");
                    int existingQty = rs.getInt("quantity");
                    int newQty      = existingQty + purchase.getQuantity();
                    String updateSql = "UPDATE stock SET quantity=?, purchase_price=?, expiry_date=?, " +
                                       "minimum_stock_level=?, supplier=?, supplier_id=?, updated_at=? " +
                                       "WHERE stock_id=?";
                    try (PreparedStatement upd = conn.prepareStatement(updateSql)) {
                        upd.setInt(1, newQty);
                        if (purchase.getPurchasePrice() != null) upd.setBigDecimal(2, purchase.getPurchasePrice());
                        else upd.setNull(2, Types.DECIMAL);
                        upd.setDate(3, Date.valueOf(purchase.getExpiryDate()));
                        upd.setInt(4, purchase.getMinimumStockLevel());
                        upd.setString(5, purchase.getSupplierName());
                        upd.setInt(6, supplierId);
                        upd.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
                        upd.setInt(8, existingId);
                        upd.executeUpdate();
                    }
                    return existingId;
                } else {
                    // New batch → insert
                    String insertSql = "INSERT INTO stock (medicine_id, batch_number, quantity, minimum_stock_level, " +
                                       "expiry_date, supplier, supplier_id, purchase_price) VALUES (?,?,?,?,?,?,?,?)";
                    try (PreparedStatement ins = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                        ins.setInt(1, purchase.getMedicineId());
                        ins.setString(2, purchase.getBatchNumber());
                        ins.setInt(3, purchase.getQuantity());
                        ins.setInt(4, purchase.getMinimumStockLevel());
                        ins.setDate(5, Date.valueOf(purchase.getExpiryDate()));
                        ins.setString(6, purchase.getSupplierName());
                        ins.setInt(7, supplierId);
                        if (purchase.getPurchasePrice() != null) ins.setBigDecimal(8, purchase.getPurchasePrice());
                        else ins.setNull(8, Types.DECIMAL);
                        ins.executeUpdate();
                        try (ResultSet keys = ins.getGeneratedKeys()) {
                            if (keys.next()) return keys.getInt(1);
                        }
                    }
                }
            }
        }
        return -1;
    }

    /** Retrieve all purchases (most recent first, max 500). */
    public List<MedicinePurchase> findAllPurchases() throws SQLException {
        String sql = "SELECT mp.*, s.supplier_name, s.company_name, m.medicine_name " +
                     "FROM medicine_purchases mp " +
                     "JOIN suppliers s ON mp.supplier_id = s.supplier_id " +
                     "JOIN medicines  m ON mp.medicine_id  = m.medicine_id " +
                     "ORDER BY mp.purchase_date DESC LIMIT 500";
        return queryPurchases(sql);
    }

    /** Search purchases by supplier name, medicine name, or batch number. */
    public List<MedicinePurchase> searchPurchases(String term) throws SQLException {
        String sql = "SELECT mp.*, s.supplier_name, s.company_name, m.medicine_name " +
                     "FROM medicine_purchases mp " +
                     "JOIN suppliers s ON mp.supplier_id = s.supplier_id " +
                     "JOIN medicines  m ON mp.medicine_id  = m.medicine_id " +
                     "WHERE s.supplier_name LIKE ? OR m.medicine_name LIKE ? OR mp.batch_number LIKE ? " +
                     "ORDER BY mp.purchase_date DESC";
        String p = "%" + term + "%";
        List<MedicinePurchase> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p); ps.setString(2, p); ps.setString(3, p);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPurchase(rs));
            }
        }
        return list;
    }

    /** Get all purchases for a specific supplier. */
    public List<MedicinePurchase> findPurchasesBySupplier(int supplierId) throws SQLException {
        String sql = "SELECT mp.*, s.supplier_name, s.company_name, m.medicine_name " +
                     "FROM medicine_purchases mp " +
                     "JOIN suppliers s ON mp.supplier_id = s.supplier_id " +
                     "JOIN medicines  m ON mp.medicine_id  = m.medicine_id " +
                     "WHERE mp.supplier_id = ? ORDER BY mp.purchase_date DESC";
        List<MedicinePurchase> list = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapPurchase(rs));
            }
        }
        return list;
    }

    /** Today's total purchase amount (for dashboard). */
    public double getTodayPurchaseAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity * purchase_price), 0) FROM medicine_purchases " +
                     "WHERE DATE(purchase_date) = CURDATE()";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    /** Today's bill count from bills table (for dashboard). */
    public int getTodayBillCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bills WHERE DATE(bill_date) = CURDATE()";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Today's sales total from bills table. */
    public double getTodaySalesAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(grand_total), 0) FROM bills WHERE DATE(bill_date) = CURDATE()";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    /** This week's sales total. */
    public double getWeekSalesAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(grand_total), 0) FROM bills " +
                     "WHERE bill_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    /** This month's sales total. */
    public double getMonthSalesAmount() throws SQLException {
        String sql = "SELECT COALESCE(SUM(grand_total), 0) FROM bills " +
                     "WHERE MONTH(bill_date) = MONTH(CURDATE()) AND YEAR(bill_date) = YEAR(CURDATE())";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    /** Expiring-soon count (within given days). */
    public int getExpiringSoonCount(int days) throws SQLException {
        String sql = "SELECT COUNT(*) FROM stock WHERE expiry_date >= CURDATE() " +
                     "AND expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) AND quantity > 0";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Expired stock count. */
    public int getExpiredCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM stock WHERE expiry_date < CURDATE() AND quantity > 0";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /** Total medicines count. */
    public int getTotalMedicinesCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM medicines WHERE status = 'ACTIVE'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private List<MedicinePurchase> queryPurchases(String sql) throws SQLException {
        List<MedicinePurchase> list = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapPurchase(rs));
        }
        return list;
    }

    private Supplier mapSupplier(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setSupplierId(rs.getInt("supplier_id"));
        s.setSupplierName(rs.getString("supplier_name"));
        s.setCompanyName(rs.getString("company_name"));
        s.setPhone(rs.getString("phone"));
        s.setEmail(rs.getString("email"));
        s.setAddress(rs.getString("address"));
        s.setGstNumber(rs.getString("gst_number"));
        s.setStatus(rs.getString("status"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) s.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) s.setUpdatedAt(ua.toLocalDateTime());
        return s;
    }

    private MedicinePurchase mapPurchase(ResultSet rs) throws SQLException {
        MedicinePurchase p = new MedicinePurchase();
        p.setPurchaseId(rs.getInt("purchase_id"));
        p.setSupplierId(rs.getInt("supplier_id"));
        p.setMedicineId(rs.getInt("medicine_id"));
        p.setBatchNumber(rs.getString("batch_number"));
        p.setQuantity(rs.getInt("quantity"));
        p.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        p.setSellingPrice(rs.getBigDecimal("selling_price"));
        Date mfg = rs.getDate("manufacturing_date");
        if (mfg != null) p.setManufacturingDate(mfg.toLocalDate());
        Date exp = rs.getDate("expiry_date");
        if (exp != null) p.setExpiryDate(exp.toLocalDate());
        p.setMinimumStockLevel(rs.getInt("minimum_stock_level"));
        Timestamp pd = rs.getTimestamp("purchase_date");
        if (pd != null) p.setPurchaseDate(pd.toLocalDateTime());
        p.setPerformedBy(rs.getString("performed_by"));
        p.setNotes(rs.getString("notes"));
        // Joined columns
        try { p.setSupplierName(rs.getString("supplier_name")); } catch (SQLException ignored) {}
        try { p.setCompanyName(rs.getString("company_name")); }  catch (SQLException ignored) {}
        try { p.setMedicineName(rs.getString("medicine_name")); } catch (SQLException ignored) {}
        return p;
    }
}
