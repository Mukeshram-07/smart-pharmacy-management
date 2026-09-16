package com.pharmacy.management.dao;

import com.pharmacy.management.model.User;
import com.pharmacy.management.model.UserRole;

import java.sql.*;
import java.time.LocalDateTime;

/**
 * Data Access Object for User entity.
 * Handles all database operations related to users.
 * 
 * Validates Requirements:
 * - 1.1: User authentication with credential verification
 * - 1.4: Update last_login timestamp on successful authentication
 * - 1.5: Use PreparedStatement for SQL injection prevention
 * - 11.1: Use PreparedStatement for all SQL queries
 * - 11.3: Use try-with-resources for proper resource cleanup
 */
public class UserDAO extends BaseDAO {
    
    /**
     * Find a user by username.
     * Used during authentication to retrieve user credentials.
     * 
     * Requirements:
     * - 1.1: Authenticate the User via credential verification
     * - 11.1: Use PreparedStatement to prevent SQL injection
     * 
     * @param username the username to search for
     * @return User object if found, null otherwise
     * @throws SQLException if database error occurs
     */
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, role, status, " +
                     "created_at, last_login FROM users WHERE username = ?";
        
        User user = null;
        
        // Use try-with-resources for automatic resource management (Requirement 11.3)
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Use PreparedStatement to prevent SQL injection (Requirement 11.1)
            ps.setString(1, username);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                    logger.debug("User found: {}", username);
                } else {
                    logger.debug("User not found: {}", username);
                }
            }
        }
        
        return user;
    }
    
    /**
     * Update the last_login timestamp for a user.
     * Called after successful authentication.
     * 
     * Requirements:
     * - 1.4: Update last_login timestamp on successful authentication
     * - 11.1: Use PreparedStatement to prevent SQL injection
     * 
     * @param username the username whose last_login to update
     * @throws SQLException if database error occurs
     */
    public void updateLastLogin(String username) throws SQLException {
        String sql = "UPDATE users SET last_login = ? WHERE username = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(2, username);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.debug("Updated last_login for user: {}", username);
            } else {
                logger.warn("Failed to update last_login for user: {}", username);
            }
        }
    }
    
    /**
     * Map a ResultSet row to a User object.
     * Helper method to construct User objects from database results.
     * 
     * @param rs ResultSet positioned at a valid row
     * @return User object populated with data from ResultSet
     * @throws SQLException if error occurs reading ResultSet
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setFullName(rs.getString("full_name"));
        
        // Convert string role to UserRole enum
        String roleStr = rs.getString("role");
        user.setRole(roleStr);
        
        user.setStatus(rs.getString("status"));
        
        // Handle timestamps
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        if (createdAtTs != null) {
            user.setCreatedAt(createdAtTs.toLocalDateTime());
        }
        
        Timestamp lastLoginTs = rs.getTimestamp("last_login");
        if (lastLoginTs != null) {
            user.setLastLogin(lastLoginTs.toLocalDateTime());
        }
        
        return user;
    }
    
    /**
     * Find a user by ID.
     * 
     * @param id the user ID to search for
     * @return User object if found, null otherwise
     * @throws SQLException if database error occurs
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, role, status, " +
                     "created_at, last_login FROM users WHERE id = ?";
        
        User user = null;
        
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    user = mapResultSetToUser(rs);
                    logger.debug("User found with ID: {}", id);
                }
            }
        }
        
        return user;
    }
}
