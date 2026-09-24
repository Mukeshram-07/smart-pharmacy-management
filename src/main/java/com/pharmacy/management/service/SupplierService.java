package com.pharmacy.management.service;

import com.pharmacy.management.config.DatabaseConfig;
import com.pharmacy.management.dao.StockDAO;
import com.pharmacy.management.dao.SupplierDAO;
import com.pharmacy.management.model.MedicinePurchase;
import com.pharmacy.management.model.Supplier;
import com.pharmacy.management.model.StockTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Service layer for Supplier Management and Medicine Purchase operations.
 * Module 5 - Supplier Management
 */
public class SupplierService {

    private static final Logger logger = LoggerFactory.getLogger(SupplierService.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$|^\\d{7,15}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final SupplierDAO supplierDAO;
    private final StockDAO    stockDAO;

    public SupplierService() {
        this.supplierDAO = new SupplierDAO();
        this.stockDAO    = new StockDAO();
    }

    // =========================================================================
    // SUPPLIER CRUD
    // =========================================================================

    public void addSupplier(Supplier supplier) throws ValidationException, ServiceException {
        validateSupplier(supplier);
        try {
            if (supplierDAO.supplierExists(supplier.getSupplierName(), supplier.getCompanyName(), 0))
                throw new ValidationException(
                        "A supplier with the same name and company already exists.");
            supplierDAO.insertSupplier(supplier);
            logger.info("Supplier added: {}", supplier.getSupplierName());
        } catch (ValidationException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Failed to add supplier", e);
            throw new ServiceException("Failed to add supplier: " + e.getMessage());
        }
    }

    public void updateSupplier(Supplier supplier) throws ValidationException, ServiceException {
        validateSupplier(supplier);
        try {
            if (supplierDAO.supplierExists(
                    supplier.getSupplierName(), supplier.getCompanyName(), supplier.getSupplierId()))
                throw new ValidationException(
                        "Another supplier with the same name and company already exists.");
            supplierDAO.updateSupplier(supplier);
            logger.info("Supplier updated: id={}", supplier.getSupplierId());
        } catch (ValidationException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Failed to update supplier", e);
            throw new ServiceException("Failed to update supplier: " + e.getMessage());
        }
    }

    public void deactivateSupplier(int supplierId) throws ServiceException {
        try {
            supplierDAO.deactivateSupplier(supplierId);
        } catch (SQLException e) {
            logger.error("Failed to deactivate supplier id={}", supplierId, e);
            throw new ServiceException("Failed to deactivate supplier: " + e.getMessage());
        }
    }

    public List<Supplier> getAllSuppliers() throws ServiceException {
        try { return supplierDAO.findAllSuppliers(); }
        catch (SQLException e) { throw new ServiceException("Failed to retrieve suppliers"); }
    }

    public List<Supplier> getActiveSuppliers() throws ServiceException {
        try { return supplierDAO.findActiveSuppliers(); }
        catch (SQLException e) { throw new ServiceException("Failed to retrieve active suppliers"); }
    }

    public Supplier getSupplierById(int id) throws ServiceException {
        try { return supplierDAO.findSupplierById(id); }
        catch (SQLException e) { throw new ServiceException("Failed to retrieve supplier"); }
    }

    public List<Supplier> searchSuppliers(String term) throws ServiceException {
        try { return supplierDAO.searchSuppliers(term); }
        catch (SQLException e) { throw new ServiceException("Failed to search suppliers"); }
    }

    public int getSupplierCount() throws ServiceException {
        try { return supplierDAO.countSuppliers(); }
        catch (SQLException e) { throw new ServiceException("Failed to count suppliers"); }
    }

    // =========================================================================
    // MEDICINE PURCHASE — atomic transaction
    // =========================================================================

    /**
     * Record a medicine purchase.
     * In a single DB transaction:
     * 1. Insert purchase record
     * 2. Upsert stock (new batch or update existing)
     * 3. Insert stock transaction entry
     * If any step fails, the whole thing rolls back.
     */
    public void recordPurchase(MedicinePurchase purchase) throws ValidationException, ServiceException {
        validatePurchase(purchase);

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Resolve supplier name for stock record
                Supplier supplier = supplierDAO.findSupplierById(purchase.getSupplierId());
                if (supplier == null) throw new ServiceException("Supplier not found");
                if (!"ACTIVE".equals(supplier.getStatus()))
                    throw new ServiceException("Cannot record purchase for an inactive supplier");

                purchase.setSupplierName(supplier.getSupplierName());

                // 1. Upsert stock
                int stockId = supplierDAO.upsertStock(conn, purchase, supplier.getSupplierId());

                // 2. Insert purchase history
                supplierDAO.insertPurchase(conn, purchase, stockId);

                // 3. Record stock transaction (ADD)
                StockTransaction txn = new StockTransaction(
                        purchase.getMedicineId(), "ADD", purchase.getQuantity(),
                        0, purchase.getQuantity(),
                        purchase.getPerformedBy(),
                        "Purchase from " + supplier.getSupplierName() +
                        " | Batch: " + purchase.getBatchNumber());
                stockDAO.insertTransaction(conn, txn);

                // 4. Update selling price in medicine master if provided
                if (purchase.getSellingPrice() != null
                        && purchase.getSellingPrice().compareTo(BigDecimal.ZERO) > 0) {
                    updateMedicineSellingPrice(conn, purchase.getMedicineId(), purchase.getSellingPrice());
                }

                conn.commit();
                logger.info("Purchase recorded: medicine_id={}, batch={}, qty={}",
                        purchase.getMedicineId(), purchase.getBatchNumber(), purchase.getQuantity());

            } catch (ServiceException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new ServiceException("Purchase failed: " + e.getMessage());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (SQLException e) {
            throw new ServiceException("Database error during purchase: " + e.getMessage());
        }
    }

    public List<MedicinePurchase> getAllPurchases() throws ServiceException {
        try { return supplierDAO.findAllPurchases(); }
        catch (SQLException e) { throw new ServiceException("Failed to retrieve purchases"); }
    }

    public List<MedicinePurchase> searchPurchases(String term) throws ServiceException {
        try { return supplierDAO.searchPurchases(term); }
        catch (SQLException e) { throw new ServiceException("Failed to search purchases"); }
    }

    public List<MedicinePurchase> getPurchasesBySupplier(int supplierId) throws ServiceException {
        try { return supplierDAO.findPurchasesBySupplier(supplierId); }
        catch (SQLException e) { throw new ServiceException("Failed to retrieve purchases for supplier"); }
    }

    // =========================================================================
    // DASHBOARD STATS
    // =========================================================================

    public double getTodayPurchaseAmount() throws ServiceException {
        try { return supplierDAO.getTodayPurchaseAmount(); }
        catch (SQLException e) { throw new ServiceException("Failed to get today's purchase amount"); }
    }

    public int getTodayBillCount() throws ServiceException {
        try { return supplierDAO.getTodayBillCount(); }
        catch (SQLException e) { throw new ServiceException("Failed to get today's bill count"); }
    }

    public double getTodaySalesAmount() throws ServiceException {
        try { return supplierDAO.getTodaySalesAmount(); }
        catch (SQLException e) { throw new ServiceException("Failed to get today's sales amount"); }
    }

    public double getWeekSalesAmount() throws ServiceException {
        try { return supplierDAO.getWeekSalesAmount(); }
        catch (SQLException e) { throw new ServiceException("Failed to get week sales amount"); }
    }

    public double getMonthSalesAmount() throws ServiceException {
        try { return supplierDAO.getMonthSalesAmount(); }
        catch (SQLException e) { throw new ServiceException("Failed to get month sales amount"); }
    }

    public int getExpiringSoonCount(int days) throws ServiceException {
        try { return supplierDAO.getExpiringSoonCount(days); }
        catch (SQLException e) { throw new ServiceException("Failed to get expiring-soon count"); }
    }

    public int getExpiredCount() throws ServiceException {
        try { return supplierDAO.getExpiredCount(); }
        catch (SQLException e) { throw new ServiceException("Failed to get expired count"); }
    }

    public int getTotalMedicinesCount() throws ServiceException {
        try { return supplierDAO.getTotalMedicinesCount(); }
        catch (SQLException e) { throw new ServiceException("Failed to get medicines count"); }
    }

    // =========================================================================
    // Private helpers
    // =========================================================================

    private void validateSupplier(Supplier s) throws ValidationException {
        if (s.getSupplierName() == null || s.getSupplierName().trim().isEmpty())
            throw new ValidationException("Supplier name is required.");
        if (s.getCompanyName() == null || s.getCompanyName().trim().isEmpty())
            throw new ValidationException("Company name is required.");
        if (s.getPhone() == null || s.getPhone().trim().isEmpty())
            throw new ValidationException("Phone number is required.");
        if (!PHONE_PATTERN.matcher(s.getPhone().trim()).matches())
            throw new ValidationException("Invalid phone number. Please enter 7–15 digits.");
        if (s.getEmail() != null && !s.getEmail().trim().isEmpty()
                && !EMAIL_PATTERN.matcher(s.getEmail().trim()).matches())
            throw new ValidationException("Invalid email format.");
    }

    private void validatePurchase(MedicinePurchase p) throws ValidationException {
        if (p.getSupplierId() <= 0)
            throw new ValidationException("Please select a supplier.");
        if (p.getMedicineId() <= 0)
            throw new ValidationException("Please select a medicine.");
        if (p.getBatchNumber() == null || p.getBatchNumber().trim().isEmpty())
            throw new ValidationException("Batch number is required.");
        if (p.getQuantity() <= 0)
            throw new ValidationException("Quantity must be greater than zero.");
        if (p.getPurchasePrice() == null || p.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0)
            throw new ValidationException("Purchase price cannot be negative.");
        if (p.getExpiryDate() == null)
            throw new ValidationException("Expiry date is required.");
        if (p.getManufacturingDate() != null && p.getExpiryDate().isBefore(p.getManufacturingDate()))
            throw new ValidationException("Expiry date must be after manufacturing date.");
        if (p.getExpiryDate().isBefore(java.time.LocalDate.now()))
            throw new ValidationException("Cannot receive already-expired stock.");
        if (p.getMinimumStockLevel() < 0)
            throw new ValidationException("Minimum stock level cannot be negative.");
    }

    private void updateMedicineSellingPrice(Connection conn, int medicineId, BigDecimal price)
            throws SQLException {
        String sql = "UPDATE medicines SET selling_price=?, updated_at=NOW() WHERE medicine_id=?";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, price);
            ps.setInt(2, medicineId);
            ps.executeUpdate();
        }
    }
}
