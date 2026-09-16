package com.pharmacy.management.model;

/**
 * Enum representing user roles in the Pharmacy Management System.
 * Validates Requirement 2.1: Role-Based Access Control
 * 
 * Roles:
 * - ADMIN: Full access including delete operations
 * - PHARMACIST: Standard access without delete permissions
 */
public enum UserRole {
    /**
     * Administrator role with full system access and delete permissions
     */
    ADMIN,
    
    /**
     * Pharmacist role with standard access (no delete permissions)
     */
    PHARMACIST
}
