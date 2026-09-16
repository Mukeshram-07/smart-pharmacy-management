package com.pharmacy.management.dao;

import com.pharmacy.management.model.Stock;
import com.pharmacy.management.model.StockTransaction;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for stock operations.
 * Module 3 - Stock Management
 */
public class StockDAO extends BaseDAO {

    /**
     * Insert new stock record.
     */
    public void insert(Stock stock) throws SQLException {
        String sql = "INSERT INTO stock (medicine_id, batch_number, quantity, minimum_stock_level, " +
                     "expiry_date, supplier, purchase_price) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, stock.getMedicineId());
            ps.setString(2, stock.getBatchNumber());
            ps.setInt(3, stock.getQuantity());
            ps.setInt(4, stock.getMinimumStockLevel());
            ps.setDate(5, Date.valueOf(stock.getExpiryDate()));
            ps.setString(6, stock.getSupplier());
            if (stock.getPurchasePrice() != null) {
                ps.setBigDecimal(7, stock.getPurchasePrice());
            } else {
                ps.setNull(7, Types.DECIMAL);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    stock.setStockId(keys.getInt(1));
                }
            }

            logger.info("Stock inserted: medicineId={}, batch={}, qty={}",
                    stock.getMedicineId(), stock.getBatchNumber(), stock.getQuantity());
        }
    }

    /**
     * Get all stock records joined with medicine names.
     */
    public List<Stock> findAll() throws SQLException {
        String sql = "SELECT s.*, m.medicine_name, m.category FROM stock s " +
                     "JOIN medicines m ON s.medicine_id = m.medicine_id " +
                     "ORDER BY m.medicine_name, s.expiry_date";
        List<Stock> stockList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stockList.add(mapResultSetToStock(rs));
            }
        }
        return stockList;
    }

    /**
     * Find stock by ID.
     */
    public Stock findById(int stockId) throws SQLException {
        String sql = "SELECT s.*, m.medicine_name, m.category FROM stock s " +
                     "JOIN medicines m ON s.medicine_id = m.medicine_id " +
                     "WHERE s.stock_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stockId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStock(rs);
                }
            }
        }
        return null;
    }

    /**
     * Find stock by medicine ID.
     */
    public List<Stock> findByMedicineId(int medicineId) throws SQLException {
        String sql = "SELECT s.*, m.medicine_name, m.category FROM stock s " +
                     "JOIN medicines m ON s.medicine_id = m.medicine_id " +
                     "WHERE s.medicine_id = ? ORDER BY s.expiry_date";
        List<Stock> stockList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medicineId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stockList.add(mapResultSetToStock(rs));
                }
            }
        }
        return stockList;
    }

    /**
     * Search stock by medicine name, batch number, or supplier.
     */
    public List<Stock> search(String searchTerm) throws SQLException {
        String sql = "SELECT s.*, m.medicine_name, m.category FROM stock s " +
                     "JOIN medicines m ON s.medicine_id = m.medicine_id " +
                     "WHERE m.medicine_name LIKE ? OR s.batch_number LIKE ? OR s.supplier LIKE ? " +
                     "ORDER BY m.medicine_name";
        List<Stock> stockList = new ArrayList<>();
        String pattern = "%" + searchTerm + "%";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stockList.add(mapResultSetToStock(rs));
                }
            }
        }
        return stockList;
    }

    /**
     * Find low-stock medicines (quantity <= minimum_stock_level and quantity > 0).
     */
    public List<Stock> findLowStock() throws SQLException {
        String sql = "SELECT s.*, m.medicine_name, m.category FROM stock s " +
                     "JOIN medicines m ON s.medicine_id = m.medicine_id " +
                     "WHERE s.quantity > 0 AND s.quantity <= s.minimum_stock_level " +
                     "ORDER BY s.quantity ASC";
        List<Stock> stockList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stockList.add(mapResultSetToStock(rs));
            }
        }
        return stockList;
    }

    /**
     * Find out-of-stock medicines (quantity = 0).
     */
    public List<Stock> findOutOfStock() throws SQLException {
        String sql = "SELECT s.*, m.medicine_name, m.category FROM stock s " +
                     "JOIN medicines m ON s.medicine_id = m.medicine_id " +
                     "WHERE s.quantity = 0 ORDER BY m.medicine_name";
        List<Stock> stockList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stockList.add(mapResultSetToStock(rs));
            }
        }
        return stockList;
    }

    /**
     * Find expired stock.
     */
    public List<Stock> findExpired() throws SQLException {
        String sql = "SELECT s.*, m.medicine_name, m.category FROM stock s " +
                     "JOIN medicines m ON s.medicine_id = m.medicine_id " +
                     "WHERE s.expiry_date < CURDATE() ORDER BY s.expiry_date";
        List<Stock> stockList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stockList.add(mapResultSetToStock(rs));
            }
        }
        return stockList;
    }

    /**
     * Find stock expiring within given days.
     */
    public List<Stock> findExpiringWithin(int days) throws SQLException {
        String sql = "SELECT s.*, m.medicine_name, m.category FROM stock s " +
                     "JOIN medicines m ON s.medicine_id = m.medicine_id " +
                     "WHERE s.expiry_date >= CURDATE() AND s.expiry_date <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                     "ORDER BY s.expiry_date";
        List<Stock> stockList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, days);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stockList.add(mapResultSetToStock(rs));
                }
            }
        }
        return stockList;
    }

    /**
     * Update stock quantity (atomic - used in transactions).
     */
    public void updateQuantity(Connection conn, int stockId, int newQuantity) throws SQLException {
        String sql = "UPDATE stock SET quantity = ?, updated_at = ? WHERE stock_id = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, newQuantity);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, stockId);
            ps.executeUpdate();
        }
    }

    /**
     * Update full stock record.
     */
    public void update(Stock stock) throws SQLException {
        String sql = "UPDATE stock SET batch_number=?, quantity=?, minimum_stock_level=?, " +
                     "expiry_date=?, supplier=?, purchase_price=?, updated_at=? WHERE stock_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, stock.getBatchNumber());
            ps.setInt(2, stock.getQuantity());
            ps.setInt(3, stock.getMinimumStockLevel());
            ps.setDate(4, Date.valueOf(stock.getExpiryDate()));
            ps.setString(5, stock.getSupplier());
            if (stock.getPurchasePrice() != null) {
                ps.setBigDecimal(6, stock.getPurchasePrice());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(8, stock.getStockId());

            ps.executeUpdate();
            logger.info("Stock updated: stockId={}", stock.getStockId());
        }
    }

    /**
     * Get total stock quantity across all records.
     */
    public int getTotalStockQuantity() throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM stock";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Count low-stock records.
     */
    public int getLowStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM stock WHERE quantity > 0 AND quantity <= minimum_stock_level";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Count out-of-stock records.
     */
    public int getOutOfStockCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM stock WHERE quantity = 0";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Get total quantity for a specific medicine (across all batches).
     */
    public int getTotalQuantityForMedicine(int medicineId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(quantity), 0) FROM stock WHERE medicine_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get available (non-expired) stock for a medicine — FEFO order for billing.
     */
    public List<Stock> findAvailableStockForMedicine(int medicineId) throws SQLException {
        String sql = "SELECT s.*, m.medicine_name, m.category FROM stock s " +
                     "JOIN medicines m ON s.medicine_id = m.medicine_id " +
                     "WHERE s.medicine_id = ? AND s.quantity > 0 AND s.expiry_date >= CURDATE() " +
                     "ORDER BY s.expiry_date ASC";
        List<Stock> stockList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    stockList.add(mapResultSetToStock(rs));
                }
            }
        }
        return stockList;
    }

    /**
     * Insert transaction record (uses a provided connection for atomicity).
     */
    public void insertTransaction(Connection conn, StockTransaction transaction) throws SQLException {
        String sql = "INSERT INTO stock_transactions (medicine_id, transaction_type, quantity_changed, " +
                     "previous_quantity, new_quantity, transaction_date, performed_by, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, transaction.getMedicineId());
            ps.setString(2, transaction.getTransactionType());
            ps.setInt(3, transaction.getQuantityChanged());
            ps.setInt(4, transaction.getPreviousQuantity());
            ps.setInt(5, transaction.getNewQuantity());
            ps.setTimestamp(6, Timestamp.valueOf(
                    transaction.getTransactionDate() != null
                            ? transaction.getTransactionDate()
                            : LocalDateTime.now()));
            ps.setString(7, transaction.getPerformedBy());
            ps.setString(8, transaction.getNotes());

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) transaction.setTransactionId(keys.getInt(1));
            }
        }
    }

    /**
     * Get all transactions (most recent first, capped at 500).
     */
    public List<StockTransaction> findAllTransactions() throws SQLException {
        String sql = "SELECT st.*, m.medicine_name FROM stock_transactions st " +
                     "JOIN medicines m ON st.medicine_id = m.medicine_id " +
                     "ORDER BY st.transaction_date DESC LIMIT 500";
        List<StockTransaction> transactions = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                StockTransaction t = new StockTransaction();
                t.setTransactionId(rs.getInt("transaction_id"));
                t.setMedicineId(rs.getInt("medicine_id"));
                t.setMedicineName(rs.getString("medicine_name"));
                t.setTransactionType(rs.getString("transaction_type"));
                t.setQuantityChanged(rs.getInt("quantity_changed"));
                t.setPreviousQuantity(rs.getInt("previous_quantity"));
                t.setNewQuantity(rs.getInt("new_quantity"));
                Timestamp ts = rs.getTimestamp("transaction_date");
                if (ts != null) t.setTransactionDate(ts.toLocalDateTime());
                t.setPerformedBy(rs.getString("performed_by"));
                t.setNotes(rs.getString("notes"));
                transactions.add(t);
            }
        }
        return transactions;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Stock mapResultSetToStock(ResultSet rs) throws SQLException {
        Stock stock = new Stock();
        stock.setStockId(rs.getInt("stock_id"));
        stock.setMedicineId(rs.getInt("medicine_id"));
        stock.setBatchNumber(rs.getString("batch_number"));
        stock.setQuantity(rs.getInt("quantity"));
        stock.setMinimumStockLevel(rs.getInt("minimum_stock_level"));
        Date expiryDate = rs.getDate("expiry_date");
        if (expiryDate != null) stock.setExpiryDate(expiryDate.toLocalDate());
        stock.setSupplier(rs.getString("supplier"));
        stock.setPurchasePrice(rs.getBigDecimal("purchase_price"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) stock.setCreatedAt(createdAt.toLocalDateTime());
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) stock.setUpdatedAt(updatedAt.toLocalDateTime());
        // Joined columns — silently skip if absent
        try { stock.setMedicineName(rs.getString("medicine_name")); } catch (SQLException ignored) {}
        try { stock.setCategory(rs.getString("category")); } catch (SQLException ignored) {}
        return stock;
    }
}
