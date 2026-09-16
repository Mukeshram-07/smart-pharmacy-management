package com.pharmacy.management.model;

import java.time.LocalDateTime;

/**
 * Stock transaction history record.
 * Module 3 - Stock Management
 */
public class StockTransaction {

    private int transactionId;
    private int medicineId;
    private String medicineName;
    private String transactionType; // ADD, REMOVE, CORRECTION
    private int quantityChanged;
    private int previousQuantity;
    private int newQuantity;
    private LocalDateTime transactionDate;
    private String performedBy;
    private String notes;

    public StockTransaction() {}

    public StockTransaction(int medicineId, String transactionType, int quantityChanged,
                            int previousQuantity, int newQuantity, String performedBy, String notes) {
        this.medicineId = medicineId;
        this.transactionType = transactionType;
        this.quantityChanged = quantityChanged;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.performedBy = performedBy;
        this.notes = notes;
        this.transactionDate = LocalDateTime.now();
    }

    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }

    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }

    public int getQuantityChanged() { return quantityChanged; }
    public void setQuantityChanged(int quantityChanged) { this.quantityChanged = quantityChanged; }

    public int getPreviousQuantity() { return previousQuantity; }
    public void setPreviousQuantity(int previousQuantity) { this.previousQuantity = previousQuantity; }

    public int getNewQuantity() { return newQuantity; }
    public void setNewQuantity(int newQuantity) { this.newQuantity = newQuantity; }

    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }

    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
