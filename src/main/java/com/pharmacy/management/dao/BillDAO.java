package com.pharmacy.management.dao;

import com.pharmacy.management.model.Bill;
import com.pharmacy.management.model.BillItem;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for billing operations.
 * Module 4 - Real-Time Billing
 */
public class BillDAO extends BaseDAO {

    /**
     * Generate a unique bill number in BILL-YYYYMMDD-XXXX format.
     * Uses COUNT of today's bills as sequential suffix.
     */
    public String generateBillNumber() throws SQLException {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String prefix = "BILL-" + dateStr + "-";
        String sql = "SELECT COUNT(*) FROM bills WHERE bill_number LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "%");
            try (ResultSet rs = ps.executeQuery()) {
                int count = rs.next() ? rs.getInt(1) : 0;
                return prefix + String.format("%04d", count + 1);
            }
        }
    }

    /**
     * Insert a new bill record. Returns generated bill_id.
     * Uses the provided connection for transaction support.
     */
    public int insertBill(Connection conn, Bill bill) throws SQLException {
        String sql = "INSERT INTO bills (bill_number, user_id, bill_date, subtotal, discount_percent, " +
                     "discount_amount, tax_percent, tax_amount, grand_total, payment_method, " +
                     "amount_paid, balance, performed_by) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, bill.getBillNumber());
            ps.setInt(2, bill.getUserId());
            ps.setTimestamp(3, Timestamp.valueOf(
                    bill.getBillDate() != null ? bill.getBillDate() : LocalDateTime.now()));
            ps.setBigDecimal(4, bill.getSubtotal());
            ps.setBigDecimal(5, bill.getDiscountPercent());
            ps.setBigDecimal(6, bill.getDiscountAmount());
            ps.setBigDecimal(7, bill.getTaxPercent());
            ps.setBigDecimal(8, bill.getTaxAmount());
            ps.setBigDecimal(9, bill.getGrandTotal());
            ps.setString(10, bill.getPaymentMethod());
            ps.setBigDecimal(11, bill.getAmountPaid());
            ps.setBigDecimal(12, bill.getBalance());
            ps.setString(13, bill.getPerformedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Insert a bill item record.
     * Uses the provided connection for transaction support.
     */
    public void insertBillItem(Connection conn, BillItem item) throws SQLException {
        String sql = "INSERT INTO bill_items (bill_id, medicine_id, medicine_name, batch_number, " +
                     "quantity, unit_price, discount_percent, tax_percent, total) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, item.getBillId());
            ps.setInt(2, item.getMedicineId());
            ps.setString(3, item.getMedicineName());
            ps.setString(4, item.getBatchNumber());
            ps.setInt(5, item.getQuantity());
            ps.setBigDecimal(6, item.getUnitPrice());
            ps.setBigDecimal(7, item.getDiscountPercent());
            ps.setBigDecimal(8, item.getTaxPercent());
            ps.setBigDecimal(9, item.getTotal());
            ps.executeUpdate();
        }
    }

    /**
     * Retrieve all bills ordered by date descending, capped at 500.
     */
    public List<Bill> findAll() throws SQLException {
        String sql = "SELECT * FROM bills ORDER BY bill_date DESC LIMIT 500";
        List<Bill> bills = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                bills.add(mapBill(rs));
            }
        }
        return bills;
    }

    /**
     * Find a bill by ID and eagerly load its items.
     */
    public Bill findById(int billId) throws SQLException {
        String sql = "SELECT * FROM bills WHERE bill_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Bill bill = mapBill(rs);
                    bill.setItems(findItemsByBillId(billId));
                    return bill;
                }
            }
        }
        return null;
    }

    /**
     * Search bills by bill_number or performed_by.
     */
    public List<Bill> search(String term) throws SQLException {
        String sql = "SELECT * FROM bills WHERE bill_number LIKE ? OR performed_by LIKE ? " +
                     "ORDER BY bill_date DESC";
        String pattern = "%" + term + "%";
        List<Bill> bills = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) bills.add(mapBill(rs));
            }
        }
        return bills;
    }

    /**
     * Get all items for a specific bill.
     */
    public List<BillItem> findItemsByBillId(int billId) throws SQLException {
        String sql = "SELECT * FROM bill_items WHERE bill_id = ?";
        List<BillItem> items = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, billId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BillItem item = new BillItem();
                    item.setBillItemId(rs.getInt("bill_item_id"));
                    item.setBillId(rs.getInt("bill_id"));
                    item.setMedicineId(rs.getInt("medicine_id"));
                    item.setMedicineName(rs.getString("medicine_name"));
                    item.setBatchNumber(rs.getString("batch_number"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setDiscountPercent(rs.getBigDecimal("discount_percent"));
                    item.setTaxPercent(rs.getBigDecimal("tax_percent"));
                    item.setTotal(rs.getBigDecimal("total"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private Bill mapBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id"));
        b.setBillNumber(rs.getString("bill_number"));
        b.setUserId(rs.getInt("user_id"));
        Timestamp ts = rs.getTimestamp("bill_date");
        if (ts != null) b.setBillDate(ts.toLocalDateTime());
        b.setSubtotal(rs.getBigDecimal("subtotal"));
        b.setDiscountPercent(rs.getBigDecimal("discount_percent"));
        b.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        b.setTaxPercent(rs.getBigDecimal("tax_percent"));
        b.setTaxAmount(rs.getBigDecimal("tax_amount"));
        b.setGrandTotal(rs.getBigDecimal("grand_total"));
        b.setPaymentMethod(rs.getString("payment_method"));
        b.setAmountPaid(rs.getBigDecimal("amount_paid"));
        b.setBalance(rs.getBigDecimal("balance"));
        b.setPerformedBy(rs.getString("performed_by"));
        return b;
    }
}
