package com.pharmacy.management.model;

import java.time.LocalDateTime;

/**
 * Supplier entity model.
 * Module 5 - Supplier Management
 */
public class Supplier {

    private int supplierId;
    private String supplierName;
    private String companyName;
    private String phone;
    private String email;
    private String address;
    private String gstNumber;
    private String status;           // ACTIVE / INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Supplier() {}

    public Supplier(String supplierName, String companyName, String phone,
                    String email, String address, String gstNumber) {
        this.supplierName = supplierName;
        this.companyName  = companyName;
        this.phone        = phone;
        this.email        = email;
        this.address      = address;
        this.gstNumber    = gstNumber;
        this.status       = "ACTIVE";
    }

    // -----------------------------------------------------------------------
    // Getters & Setters
    // -----------------------------------------------------------------------

    public int getSupplierId()                          { return supplierId; }
    public void setSupplierId(int supplierId)           { this.supplierId = supplierId; }

    public String getSupplierName()                     { return supplierName; }
    public void setSupplierName(String supplierName)    { this.supplierName = supplierName; }

    public String getCompanyName()                      { return companyName; }
    public void setCompanyName(String companyName)      { this.companyName = companyName; }

    public String getPhone()                            { return phone; }
    public void setPhone(String phone)                  { this.phone = phone; }

    public String getEmail()                            { return email; }
    public void setEmail(String email)                  { this.email = email; }

    public String getAddress()                          { return address; }
    public void setAddress(String address)              { this.address = address; }

    public String getGstNumber()                        { return gstNumber; }
    public void setGstNumber(String gstNumber)          { this.gstNumber = gstNumber; }

    public String getStatus()                           { return status; }
    public void setStatus(String status)                { this.status = status; }

    public LocalDateTime getCreatedAt()                 { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)   { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt()                 { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt)   { this.updatedAt = updatedAt; }

    /** Display string used in JComboBox. */
    @Override
    public String toString() {
        return supplierName + " (" + companyName + ")";
    }
}
