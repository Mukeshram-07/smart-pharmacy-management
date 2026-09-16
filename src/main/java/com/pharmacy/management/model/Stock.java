package com.pharmacy.management.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Stock entity model representing medicine inventory.
 * Module 3 - Stock Management
 */
public class Stock {

    private int stockId;
    private int medicineId;
    private String medicineName;   // joined from medicines table
    private String category;       // joined from medicines table
    private String batchNumber;
    private int quantity;
    private int minimumStockLevel;
    private LocalDate expiryDate;
    private String supplier;
    private BigDecimal purchasePrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Stock() {}

    public Stock(int medicineId, String batchNumber, int quantity, int minimumStockLevel,
                 LocalDate expiryDate, String supplier, BigDecimal purchasePrice) {
        this.medicineId = medicineId;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.minimumStockLevel = minimumStockLevel;
        this.expiryDate = expiryDate;
        this.supplier = supplier;
        this.purchasePrice = purchasePrice;
    }

    /**
     * Calculate stock status based on quantity and minimum stock level.
     */
    public StockStatus getStockStatus() {
        if (quantity == 0) return StockStatus.OUT_OF_STOCK;
        if (quantity <= minimumStockLevel) return StockStatus.LOW_STOCK;
        return StockStatus.AVAILABLE;
    }

    /**
     * Check if this stock is expired.
     */
    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Check if this stock expires within given days.
     */
    public boolean isExpiringWithin(int days) {
        if (expiryDate == null) return false;
        long daysUntilExpiry = ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
        return daysUntilExpiry >= 0 && daysUntilExpiry <= days;
    }

    /**
     * Get days until expiry (negative if already expired).
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    // Getters and Setters
    public int getStockId() { return stockId; }
    public void setStockId(int stockId) { this.stockId = stockId; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public int getMinimumStockLevel() { return minimumStockLevel; }
    public void setMinimumStockLevel(int minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return String.format("Stock{id=%d, medicine='%s', batch='%s', qty=%d, status=%s}",
                stockId, medicineName, batchNumber, quantity, getStockStatus());
    }
}
