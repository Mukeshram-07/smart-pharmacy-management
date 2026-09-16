package com.pharmacy.management.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Medicine entity model.
 * Validates Requirements 5.1 and 10.2
 */
public class Medicine {
    private int medicineId;
    private String medicineName;
    private String genericName;
    private String activeIngredient;
    private String brandName;
    private String manufacturer;
    private String dosageForm;
    private String strength;
    private String category;
    private String barcode;
    private BigDecimal sellingPrice;
    private boolean prescriptionRequired;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public Medicine() {}
    
    public Medicine(String medicineName, String genericName, String activeIngredient,
                   String dosageForm, String strength, BigDecimal sellingPrice) {
        this.medicineName = medicineName;
        this.genericName = genericName;
        this.activeIngredient = activeIngredient;
        this.dosageForm = dosageForm;
        this.strength = strength;
        this.sellingPrice = sellingPrice;
        this.status = "ACTIVE";
    }
    
    // Getters and Setters
    public int getMedicineId() { return medicineId; }
    public void setMedicineId(int medicineId) { this.medicineId = medicineId; }
    
    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    
    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }
    
    public String getActiveIngredient() { return activeIngredient; }
    public void setActiveIngredient(String activeIngredient) { this.activeIngredient = activeIngredient; }
    
    public String getBrandName() { return brandName; }
    public void setBrandName(String brandName) { this.brandName = brandName; }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getDosageForm() { return dosageForm; }
    public void setDosageForm(String dosageForm) { this.dosageForm = dosageForm; }
    
    public String getStrength() { return strength; }
    public void setStrength(String strength) { this.strength = strength; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }
    
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    
    public boolean isPrescriptionRequired() { return prescriptionRequired; }
    public void setPrescriptionRequired(boolean prescriptionRequired) { this.prescriptionRequired = prescriptionRequired; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public String toString() {
        return String.format("Medicine{id=%d, name='%s', price=%s}", 
                           medicineId, medicineName, sellingPrice);
    }
}
