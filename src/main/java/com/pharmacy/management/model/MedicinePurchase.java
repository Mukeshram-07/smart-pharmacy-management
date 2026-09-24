package com.pharmacy.management.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * MedicinePurchase entity — records every stock-receiving event.
 * Module 5 - Supplier Management / Purchase History
 */
public class MedicinePurchase {

    private int purchaseId;
    private int supplierId;
    private String supplierName;    // joined
    private String companyName;     // joined
    private int medicineId;
    private String medicineName;    // joined
    private String batchNumber;
    private int quantity;
    private BigDecimal purchasePrice;
    private BigDecimal sellingPrice;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private int minimumStockLevel;
    private LocalDateTime purchaseDate;
    private String performedBy;
    private String notes;

    public MedicinePurchase() {}

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public int getPurchaseId()                              { return purchaseId; }
    public void setPurchaseId(int purchaseId)               { this.purchaseId = purchaseId; }

    public int getSupplierId()                              { return supplierId; }
    public void setSupplierId(int supplierId)               { this.supplierId = supplierId; }

    public String getSupplierName()                         { return supplierName; }
    public void setSupplierName(String supplierName)        { this.supplierName = supplierName; }

    public String getCompanyName()                          { return companyName; }
    public void setCompanyName(String companyName)          { this.companyName = companyName; }

    public int getMedicineId()                              { return medicineId; }
    public void setMedicineId(int medicineId)               { this.medicineId = medicineId; }

    public String getMedicineName()                         { return medicineName; }
    public void setMedicineName(String medicineName)        { this.medicineName = medicineName; }

    public String getBatchNumber()                          { return batchNumber; }
    public void setBatchNumber(String batchNumber)          { this.batchNumber = batchNumber; }

    public int getQuantity()                                { return quantity; }
    public void setQuantity(int quantity)                   { this.quantity = quantity; }

    public BigDecimal getPurchasePrice()                    { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice)  { this.purchasePrice = purchasePrice; }

    public BigDecimal getSellingPrice()                     { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice)    { this.sellingPrice = sellingPrice; }

    public LocalDate getManufacturingDate()                 { return manufacturingDate; }
    public void setManufacturingDate(LocalDate d)           { this.manufacturingDate = d; }

    public LocalDate getExpiryDate()                        { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate)         { this.expiryDate = expiryDate; }

    public int getMinimumStockLevel()                       { return minimumStockLevel; }
    public void setMinimumStockLevel(int minimumStockLevel) { this.minimumStockLevel = minimumStockLevel; }

    public LocalDateTime getPurchaseDate()                  { return purchaseDate; }
    public void setPurchaseDate(LocalDateTime purchaseDate) { this.purchaseDate = purchaseDate; }

    public String getPerformedBy()                          { return performedBy; }
    public void setPerformedBy(String performedBy)          { this.performedBy = performedBy; }

    public String getNotes()                                { return notes; }
    public void setNotes(String notes)                      { this.notes = notes; }

    /** Total purchase amount for this line. */
    public BigDecimal getTotalAmount() {
        if (purchasePrice == null) return BigDecimal.ZERO;
        return purchasePrice.multiply(new BigDecimal(quantity));
    }
}
