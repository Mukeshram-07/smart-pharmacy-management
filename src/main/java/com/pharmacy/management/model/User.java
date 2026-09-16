package com.pharmacy.management.model;

import java.time.LocalDateTime;

/**
 * User entity representing pharmacy system users.
 * Validates Requirements 1.1 (User Authentication) and 10.1 (Database Schema).
 */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String fullName;
    private String role;  // ADMIN or PHARMACIST
    private String status;  // ACTIVE or INACTIVE
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Default constructor
    public User() {
    }

    // Constructor with all fields
    public User(int id, String username, String passwordHash, String fullName, 
                String role, String status, LocalDateTime createdAt, LocalDateTime lastLogin) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }

    // Constructor without id (for creating new users)
    public User(String username, String passwordHash, String fullName, 
                String role, String status) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.status = status;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                ", lastLogin=" + lastLogin +
                '}';
    }
}
