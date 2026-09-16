package com.pharmacy.management.model;

import java.time.LocalDateTime;

/**
 * Singleton class for managing user session information.
 * Validates Requirements 3.1, 3.2: Session Management
 * 
 * Requirements:
 * - 3.1: When a User successfully authenticates, the System SHALL create a Session 
 *        containing username, full name, and role
 * - 3.2: While a Session is active, the System SHALL display the current User's 
 *        name and role in the dashboard header
 * 
 * Usage:
 * - Call getInstance() to access the current session
 * - Call clear() to terminate the session on logout
 */
public class Session {
    
    /**
     * Singleton instance
     */
    private static Session instance;
    
    /**
     * Username of the logged-in user
     */
    private String username;
    
    /**
     * Full name of the logged-in user
     */
    private String fullName;
    
    /**
     * Role of the logged-in user (ADMIN or PHARMACIST)
     */
    private UserRole role;
    
    /**
     * Timestamp when the user logged in
     */
    private LocalDateTime loginTime;
    
    /**
     * Private constructor to enforce singleton pattern
     */
    private Session() {
        // Private constructor prevents instantiation from other classes
    }
    
    /**
     * Get the singleton instance of the Session.
     * Creates a new instance if one doesn't exist.
     * 
     * @return the Session instance
     */
    public static synchronized Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }
    
    /**
     * Clear the current session data.
     * This should be called when a user logs out.
     * Sets the instance to null, destroying the session.
     */
    public static synchronized void clear() {
        if (instance != null) {
            instance.username = null;
            instance.fullName = null;
            instance.role = null;
            instance.loginTime = null;
            instance = null;
        }
    }
    
    // Getters and Setters
    
    /**
     * Get the username of the current session
     * 
     * @return username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * Set the username for the current session
     * 
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Get the full name of the current user
     * 
     * @return full name
     */
    public String getFullName() {
        return fullName;
    }
    
    /**
     * Set the full name for the current session
     * 
     * @param fullName the full name to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    /**
     * Get the role of the current user
     * 
     * @return user role (ADMIN or PHARMACIST)
     */
    public UserRole getRole() {
        return role;
    }
    
    /**
     * Set the role for the current session
     * 
     * @param role the user role to set
     */
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    /**
     * Get the login time of the current session
     * 
     * @return login timestamp
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    /**
     * Set the login time for the current session
     * 
     * @param loginTime the login timestamp to set
     */
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    /**
     * Check if a session is currently active
     * 
     * @return true if session exists and has a username, false otherwise
     */
    public boolean isActive() {
        return username != null && !username.trim().isEmpty();
    }
    
    /**
     * Check if the current user has admin role
     * 
     * @return true if user role is ADMIN, false otherwise
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
