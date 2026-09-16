package com.pharmacy.management.model;

/**
 * Enum representing possible stock status values.
 * Module 3 - Stock Management
 */
public enum StockStatus {
    AVAILABLE("Available"),
    LOW_STOCK("Low Stock"),
    OUT_OF_STOCK("Out of Stock"),
    EXPIRED("Expired"),
    EXPIRING_SOON("Expiring Soon");

    private final String displayName;

    StockStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
