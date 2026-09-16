package com.pharmacy.management.model;

import java.math.BigDecimal;

/**
 * CartItem — in-memory only (not persisted directly).
 * Represents a medicine line item in the billing cart before bill generation.
 * Module 4 - Real-Time Billing
 */
public class CartItem {

    private int medicineId;
    private String medicineName;
    private String batchNumber;
    private int availableStock;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercent;   // defaults to 0
    private BigDecimal taxPercent;        // defaults to 0

    public CartItem() {
        this.discountPercent = BigDecimal.ZERO;
        this.taxPercent = BigDecimal.ZERO;
    }

    /**
     * Calculate the line item total: quantity * unitPrice.
     */
    public BigDecimal getItemTotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(new BigDecimal(quantity));
    }

    // Getters and Setters
    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public int getAvailableStock() { return availableStock; }
    public void setAvailableStock(int availableStock) { this.availableStock = availableStock; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

    public BigDecimal getTaxPercent() { return taxPercent; }
    public void setTaxPercent(BigDecimal taxPercent) { this.taxPercent = taxPercent; }

    @Override
    public String toString() {
        return String.format("CartItem{medicine='%s', qty=%d, unitPrice=%s, total=%s}",
                medicineName, quantity, unitPrice, getItemTotal());
    }
}
