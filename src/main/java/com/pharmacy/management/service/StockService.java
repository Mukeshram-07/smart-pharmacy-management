package com.pharmacy.management.service;

import com.pharmacy.management.config.DatabaseConfig;
import com.pharmacy.management.dao.StockDAO;
import com.pharmacy.management.model.Stock;
import com.pharmacy.management.model.StockTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Service layer for stock management operations.
 * Module 3 - Stock Management
 *
 * Also exposes reduceStock() as the Module 4 (Billing) integration point.
 */
public class StockService {

    private static final Logger logger = LoggerFactory.getLogger(StockService.class);
    private final StockDAO stockDAO;

    public StockService() {
        this.stockDAO = new StockDAO();
    }

    public StockService(StockDAO stockDAO) {
        this.stockDAO = stockDAO;
    }

    // -----------------------------------------------------------------------
    // CRUD
    // -----------------------------------------------------------------------

    /**
     * Add a new stock record.
     */
    public void addStock(Stock stock) throws ValidationException, ServiceException {
        validateStock(stock);
        try {
            stockDAO.insert(stock);
            logger.info("Stock added for medicineId={}, batch={}", stock.getMedicineId(), stock.getBatchNumber());
        } catch (SQLException e) {
            logger.error("Failed to add stock", e);
            throw new ServiceException("Failed to add stock: " + e.getMessage());
        }
    }

    /**
     * Get all stock records.
     */
    public List<Stock> getAllStock() throws ServiceException {
        try {
            return stockDAO.findAll();
        } catch (SQLException e) {
            logger.error("Failed to get all stock", e);
            throw new ServiceException("Failed to retrieve stock");
        }
    }

    /**
     * Get a single stock record by ID.
     */
    public Stock getStockById(int stockId) throws ServiceException {
        try {
            return stockDAO.findById(stockId);
        } catch (SQLException e) {
            logger.error("Failed to get stock by ID: {}", stockId, e);
            throw new ServiceException("Failed to retrieve stock");
        }
    }

    /**
     * Full-text search across medicine name, batch number, and supplier.
     */
    public List<Stock> searchStock(String term) throws ServiceException {
        try {
            return stockDAO.search(term);
        } catch (SQLException e) {
            logger.error("Failed to search stock", e);
            throw new ServiceException("Failed to search stock");
        }
    }

    /**
     * Update a stock record (records a transaction if quantity changes).
     */
    public void updateStock(Stock stock, String performedBy, String notes)
            throws ValidationException, ServiceException {
        validateStock(stock);

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Stock existing = stockDAO.findById(stock.getStockId());
                if (existing == null) throw new ServiceException("Stock record not found");

                int previousQty = existing.getQuantity();
                int newQty = stock.getQuantity();

                stockDAO.update(stock);

                // Record a transaction only when quantity actually changed
                if (previousQty != newQty) {
                    String type = newQty > previousQty ? "ADD" : "REMOVE";
                    if (notes != null && notes.toLowerCase().contains("correction")) type = "CORRECTION";

                    StockTransaction transaction = new StockTransaction(
                            stock.getMedicineId(), type, Math.abs(newQty - previousQty),
                            previousQty, newQty, performedBy, notes);
                    stockDAO.insertTransaction(conn, transaction);
                }

                conn.commit();
                logger.info("Stock updated: stockId={}", stock.getStockId());
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (ServiceException e) {
            throw e;
        } catch (SQLException e) {
            logger.error("Failed to update stock", e);
            throw new ServiceException("Failed to update stock: " + e.getMessage());
        } catch (Exception e) {
            throw new ServiceException("Unexpected error updating stock: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Quantity adjustments
    // -----------------------------------------------------------------------

    /**
     * Increase stock quantity by the given amount.
     */
    public void increaseStock(int stockId, int amount, String performedBy, String notes)
            throws ServiceException {
        if (amount <= 0) throw new ServiceException("Increase amount must be positive");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Stock stock = stockDAO.findById(stockId);
                if (stock == null) throw new ServiceException("Stock not found");

                int previousQty = stock.getQuantity();
                int newQty = previousQty + amount;

                stockDAO.updateQuantity(conn, stockId, newQty);

                StockTransaction transaction = new StockTransaction(
                        stock.getMedicineId(), "ADD", amount, previousQty, newQty, performedBy, notes);
                stockDAO.insertTransaction(conn, transaction);

                conn.commit();
                logger.info("Stock increased: stockId={}, amount={}", stockId, amount);
            } catch (ServiceException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new ServiceException("Failed to increase stock: " + e.getMessage());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (SQLException e) {
            throw new ServiceException("Database error: " + e.getMessage());
        }
    }

    /**
     * Decrease stock quantity. Never allows the result to go negative.
     */
    public void decreaseStock(int stockId, int amount, String performedBy, String notes)
            throws ServiceException {
        if (amount <= 0) throw new ServiceException("Decrease amount must be positive");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Stock stock = stockDAO.findById(stockId);
                if (stock == null) throw new ServiceException("Stock not found");

                int previousQty = stock.getQuantity();
                if (amount > previousQty) {
                    throw new ServiceException(
                            "Insufficient stock. Available: " + previousQty + ", Requested: " + amount);
                }

                int newQty = previousQty - amount;

                stockDAO.updateQuantity(conn, stockId, newQty);

                StockTransaction transaction = new StockTransaction(
                        stock.getMedicineId(), "REMOVE", amount, previousQty, newQty, performedBy, notes);
                stockDAO.insertTransaction(conn, transaction);

                conn.commit();
                logger.info("Stock decreased: stockId={}, amount={}", stockId, amount);
            } catch (ServiceException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new ServiceException("Failed to decrease stock: " + e.getMessage());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (SQLException e) {
            throw new ServiceException("Database error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Alert/Filter queries
    // -----------------------------------------------------------------------

    public List<Stock> getLowStock() throws ServiceException {
        try { return stockDAO.findLowStock(); }
        catch (SQLException e) { throw new ServiceException("Failed to retrieve low stock"); }
    }

    public List<Stock> getOutOfStock() throws ServiceException {
        try { return stockDAO.findOutOfStock(); }
        catch (SQLException e) { throw new ServiceException("Failed to retrieve out-of-stock"); }
    }

    public List<Stock> getExpiredStock() throws ServiceException {
        try { return stockDAO.findExpired(); }
        catch (SQLException e) { throw new ServiceException("Failed to retrieve expired stock"); }
    }

    public List<Stock> getExpiringSoon(int days) throws ServiceException {
        try { return stockDAO.findExpiringWithin(days); }
        catch (SQLException e) { throw new ServiceException("Failed to retrieve expiring stock"); }
    }

    // -----------------------------------------------------------------------
    // Transaction history
    // -----------------------------------------------------------------------

    public List<StockTransaction> getTransactionHistory() throws ServiceException {
        try { return stockDAO.findAllTransactions(); }
        catch (SQLException e) { throw new ServiceException("Failed to retrieve transaction history"); }
    }

    // -----------------------------------------------------------------------
    // Dashboard statistics
    // -----------------------------------------------------------------------

    public int getTotalStockQuantity() throws ServiceException {
        try { return stockDAO.getTotalStockQuantity(); }
        catch (SQLException e) { throw new ServiceException("Failed to get total stock quantity"); }
    }

    public int getLowStockCount() throws ServiceException {
        try { return stockDAO.getLowStockCount(); }
        catch (SQLException e) { throw new ServiceException("Failed to get low stock count"); }
    }

    public int getOutOfStockCount() throws ServiceException {
        try { return stockDAO.getOutOfStockCount(); }
        catch (SQLException e) { throw new ServiceException("Failed to get out-of-stock count"); }
    }

    // -----------------------------------------------------------------------
    // MODULE 4 INTEGRATION POINT
    // -----------------------------------------------------------------------

    /**
     * Reduce stock atomically for billing using FEFO (First Expired, First Out).
     *
     * @param medicineId the medicine whose stock should be reduced
     * @param quantity   the total quantity to deduct
     * @param performedBy the username performing the billing action
     * @throws ServiceException if stock is insufficient, all expired, or a DB error occurs
     */
    public void reduceStock(int medicineId, int quantity, String performedBy) throws ServiceException {
        if (quantity <= 0) throw new ServiceException("Quantity must be positive");

        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                List<Stock> availableStock = stockDAO.findAvailableStockForMedicine(medicineId);

                if (availableStock.isEmpty()) {
                    throw new ServiceException("No available (non-expired) stock for this medicine");
                }

                int totalAvailable = availableStock.stream().mapToInt(Stock::getQuantity).sum();
                if (totalAvailable < quantity) {
                    throw new ServiceException(
                            "Insufficient stock. Available: " + totalAvailable + ", Requested: " + quantity);
                }

                int remaining = quantity;
                for (Stock stock : availableStock) {
                    if (remaining <= 0) break;

                    int toTake = Math.min(remaining, stock.getQuantity());
                    int previousQty = stock.getQuantity();
                    int newQty = previousQty - toTake;

                    stockDAO.updateQuantity(conn, stock.getStockId(), newQty);

                    StockTransaction transaction = new StockTransaction(
                            medicineId, "REMOVE", toTake, previousQty, newQty,
                            performedBy, "Billing deduction - batch " + stock.getBatchNumber());
                    stockDAO.insertTransaction(conn, transaction);

                    remaining -= toTake;
                }

                conn.commit();
                logger.info("Stock reduced for billing: medicineId={}, quantity={}", medicineId, quantity);
            } catch (ServiceException e) {
                conn.rollback();
                throw e;
            } catch (Exception e) {
                conn.rollback();
                throw new ServiceException("Failed to reduce stock: " + e.getMessage());
            }
        } catch (ServiceException e) {
            throw e;
        } catch (SQLException e) {
            throw new ServiceException("Database error: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Validation
    // -----------------------------------------------------------------------

    private void validateStock(Stock stock) throws ValidationException {
        if (stock.getMedicineId() <= 0)
            throw new ValidationException("Medicine is required");
        if (stock.getBatchNumber() == null || stock.getBatchNumber().trim().isEmpty())
            throw new ValidationException("Batch number is required");
        if (stock.getQuantity() < 0)
            throw new ValidationException("Quantity cannot be negative");
        if (stock.getMinimumStockLevel() < 0)
            throw new ValidationException("Minimum stock level cannot be negative");
        if (stock.getExpiryDate() == null)
            throw new ValidationException("Expiry date is required");
        if (stock.getPurchasePrice() != null
                && stock.getPurchasePrice().compareTo(BigDecimal.ZERO) < 0)
            throw new ValidationException("Purchase price cannot be negative");
    }
}
