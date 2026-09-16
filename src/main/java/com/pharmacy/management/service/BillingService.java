package com.pharmacy.management.service;

import com.pharmacy.management.config.DatabaseConfig;
import com.pharmacy.management.dao.BillDAO;
import com.pharmacy.management.dao.MedicineDAO;
import com.pharmacy.management.dao.StockDAO;
import com.pharmacy.management.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service layer for billing operations.
 * Module 4 - Real-Time Billing
 *
 * Core operations:
 * - generateBill(): atomic bill + stock-deduction transaction
 * - getAllBills(), getBillById(), searchBills(): read operations
 */
public class BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);

    private final BillDAO billDAO;
    private final StockDAO stockDAO;
    private final MedicineDAO medicineDAO;

    public BillingService() {
        this.billDAO = new BillDAO();
        this.stockDAO = new StockDAO();
        this.medicineDAO = new MedicineDAO();
    }

    // -----------------------------------------------------------------------
    // Bill generation — the core atomic operation
    // -----------------------------------------------------------------------

    /**
     * Generate a complete bill atomically.
     * Validates stock → inserts bill → inserts bill items → reduces stock (FEFO) — all in one DB transaction.
     *
     * @param cart            list of CartItems to bill
     * @param discountPercent bill-level discount (0–100)
     * @param taxPercent      bill-level tax (0–100)
     * @param paymentMethod   CASH, CARD, or UPI
     * @param amountPaid      cash tendered (must be >= grandTotal)
     * @param userId          ID of the logged-in user
     * @param performedBy     username of the pharmacist
     * @return the saved Bill with billId populated
     * @throws ValidationException if inputs fail validation
     * @throws ServiceException    if stock is insufficient or a DB error occurs
     */
    public Bill generateBill(List<CartItem> cart,
                              BigDecimal discountPercent,
                              BigDecimal taxPercent,
                              String paymentMethod,
                              BigDecimal amountPaid,
                              int userId,
                              String performedBy) throws ValidationException, ServiceException {

        // --- Input validation ---
        if (cart == null || cart.isEmpty())
            throw new ValidationException("Cart cannot be empty");
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) < 0)
            throw new ValidationException("Discount cannot be negative");
        if (taxPercent == null || taxPercent.compareTo(BigDecimal.ZERO) < 0)
            throw new ValidationException("Tax cannot be negative");
        if (amountPaid == null || amountPaid.compareTo(BigDecimal.ZERO) < 0)
            throw new ValidationException("Amount paid cannot be negative");
        if (paymentMethod == null || paymentMethod.trim().isEmpty())
            throw new ValidationException("Payment method is required");

        // --- Calculate totals ---
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart) {
            subtotal = subtotal.add(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        BigDecimal discountAmount = subtotal
                .multiply(discountPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal taxableAmount = subtotal.subtract(discountAmount);
        BigDecimal taxAmount = taxableAmount
                .multiply(taxPercent)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = taxableAmount.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        BigDecimal balance = amountPaid.subtract(grandTotal).setScale(2, RoundingMode.HALF_UP);

        if (amountPaid.compareTo(grandTotal) < 0)
            throw new ValidationException(
                    "Amount paid (" + amountPaid + ") is less than grand total (" + grandTotal + ")");

        // --- Atomic DB transaction ---
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Pre-checkout: validate every cart item
                for (CartItem item : cart) {
                    Medicine medicine = medicineDAO.findById(item.getMedicineId());
                    if (medicine == null || !"ACTIVE".equals(medicine.getStatus())) {
                        throw new ServiceException(
                                "Medicine not found or inactive: " + item.getMedicineName());
                    }

                    List<Stock> available = stockDAO.findAvailableStockForMedicine(item.getMedicineId());
                    int totalAvailable = available.stream().mapToInt(Stock::getQuantity).sum();
                    if (totalAvailable < item.getQuantity()) {
                        throw new ServiceException(
                                "Insufficient stock for " + item.getMedicineName() +
                                ". Available: " + totalAvailable +
                                ", Requested: " + item.getQuantity());
                    }
                }

                // Generate bill number
                String billNumber = billDAO.generateBillNumber();

                // Build Bill object
                Bill bill = new Bill();
                bill.setBillNumber(billNumber);
                bill.setUserId(userId);
                bill.setBillDate(LocalDateTime.now());
                bill.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
                bill.setDiscountPercent(discountPercent);
                bill.setDiscountAmount(discountAmount);
                bill.setTaxPercent(taxPercent);
                bill.setTaxAmount(taxAmount);
                bill.setGrandTotal(grandTotal);
                bill.setPaymentMethod(paymentMethod);
                bill.setAmountPaid(amountPaid.setScale(2, RoundingMode.HALF_UP));
                bill.setBalance(balance);
                bill.setPerformedBy(performedBy);

                // Insert bill
                int billId = billDAO.insertBill(conn, bill);
                if (billId < 0) throw new ServiceException("Failed to insert bill record");
                bill.setBillId(billId);

                // Insert bill items + reduce stock (FEFO, conditional update)
                for (CartItem item : cart) {
                    BigDecimal itemTotal = item.getUnitPrice()
                            .multiply(new BigDecimal(item.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP);

                    BillItem billItem = new BillItem();
                    billItem.setBillId(billId);
                    billItem.setMedicineId(item.getMedicineId());
                    billItem.setMedicineName(item.getMedicineName());
                    billItem.setBatchNumber(item.getBatchNumber() != null ? item.getBatchNumber() : "");
                    billItem.setQuantity(item.getQuantity());
                    billItem.setUnitPrice(item.getUnitPrice());
                    billItem.setDiscountPercent(
                            item.getDiscountPercent() != null ? item.getDiscountPercent() : BigDecimal.ZERO);
                    billItem.setTaxPercent(
                            item.getTaxPercent() != null ? item.getTaxPercent() : BigDecimal.ZERO);
                    billItem.setTotal(itemTotal);

                    billDAO.insertBillItem(conn, billItem);

                    // Reduce stock within same transaction
                    reduceStockForBilling(conn, item.getMedicineId(), item.getQuantity(), performedBy, billNumber);
                }

                conn.commit();
                logger.info("Bill generated: {} by {} | Total: {}", billNumber, performedBy, grandTotal);
                return bill;

            } catch (ServiceException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                logger.error("Bill generation failed, transaction rolled back", e);
                throw new ServiceException("Bill generation failed: " + e.getMessage());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Database error during bill generation", e);
            throw new ServiceException("Database error during billing: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Stock reduction (FEFO + conditional update for concurrency safety)
    // -----------------------------------------------------------------------

    /**
     * Reduce stock within an existing connection (FEFO order).
     * Uses conditional UPDATE (AND quantity >= ?) to prevent negative stock under concurrency.
     */
    private void reduceStockForBilling(Connection conn, int medicineId, int quantity,
                                        String performedBy, String billNumber)
            throws SQLException, ServiceException {

        List<Stock> available = stockDAO.findAvailableStockForMedicine(medicineId);
        int remaining = quantity;

        for (Stock stock : available) {
            if (remaining <= 0) break;

            int toTake = Math.min(remaining, stock.getQuantity());
            int newQty = stock.getQuantity() - toTake;

            // Conditional update — prevents negative stock even under concurrent access
            String updateSql = "UPDATE stock SET quantity = ?, updated_at = ? " +
                               "WHERE stock_id = ? AND quantity >= ?";
            try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                ps.setInt(1, newQty);
                ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                ps.setInt(3, stock.getStockId());
                ps.setInt(4, toTake);
                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new ServiceException(
                            "Stock update failed due to concurrent modification for batch: " +
                            stock.getBatchNumber() + ". Please retry.");
                }
            }

            // Record stock transaction
            StockTransaction txn = new StockTransaction(
                    medicineId, "REMOVE", toTake, stock.getQuantity(), newQty,
                    performedBy, "Billing deduction - " + billNumber +
                                 " (batch: " + stock.getBatchNumber() + ")");
            stockDAO.insertTransaction(conn, txn);

            remaining -= toTake;
        }

        if (remaining > 0) {
            throw new ServiceException(
                    "Insufficient stock during checkout. Please refresh and try again.");
        }
    }

    // -----------------------------------------------------------------------
    // Read operations
    // -----------------------------------------------------------------------

    public List<Bill> getAllBills() throws ServiceException {
        try {
            return billDAO.findAll();
        } catch (SQLException e) {
            logger.error("Failed to retrieve bills", e);
            throw new ServiceException("Failed to retrieve bills");
        }
    }

    public Bill getBillById(int billId) throws ServiceException {
        try {
            return billDAO.findById(billId);
        } catch (SQLException e) {
            logger.error("Failed to retrieve bill by ID: {}", billId, e);
            throw new ServiceException("Failed to retrieve bill");
        }
    }

    public List<Bill> searchBills(String term) throws ServiceException {
        try {
            return billDAO.search(term);
        } catch (SQLException e) {
            logger.error("Failed to search bills", e);
            throw new ServiceException("Failed to search bills");
        }
    }
}
