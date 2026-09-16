package com.pharmacy.management.model;

import java.math.BigDecimal;

/**
 * BillItem entity model representing a single line item in a bill.
 * Module 4 - Real-Time Billing
 */
public class BillItem {

    private int billItemId;
    private int billId;
    private int medicineId;
    private String medicineName;
    private String batchNumber;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal discountPercent;
    private BigDecimal taxPercent;
    private BigDecimal total;

    public BillItem() {}

    /**
     * Helper method: total = quantity * unitPrice (item-level discount/tax applied in service).
     */
    public BigDecimal calculateTotal() {
        if (unitPrice == null) return BigDecimal.ZERO;
        return unitPrice.multiply(new BigDecimal(quantity));
    }

    // Getters and Setters
    public int getBillItemId() { return billItemId; }
    public void setBillItemId(int billItemId) { this.billItemId = billItemId; }

    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(BigDecimal discountPercent) { this.discountPercent = discountPercent; }

    public BigDecimal getTaxPercent() { return taxPercent; }
    public void setTaxPercent(BigDecimal taxPercent) { this.taxPercent = taxPercent; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    @Override
    public String toString() {
        return String.format("BillItem{id=%d, medicine='%s', qty=%d, unitPrice=%s, total=%s}",
                billItemId, medicineName, quantity, unitPrice, total);
    }
}
