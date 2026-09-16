package com.pharmacy.management.service;

import com.pharmacy.management.dao.UserDAO;
import com.pharmacy.management.model.Session;
import com.pharmacy.management.model.User;
import com.pharmacy.management.model.UserRole;
import com.pharmacy.management.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Service class for handling user authentication and authorization.
 * 
 * Validates Requirements:
 * - 1.1: User Authentication with credential verification
 * - 1.2: Reject invalid credentials with error message
 * - 2.1: Role-based access control (ADMIN/PHARMACIST)
 * - 3.1: Create Session on successful authentication
 * - 3.3: Terminate Session on logout
 */
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserDAO userDAO;
    
    /**
     * Constructor with dependency injection.
     * 
     * @param userDAO UserDAO instance for database operations
     */
    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }
    
    /**
     * Default constructor that creates its own UserDAO instance.
     */
    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }
    
    /**
     * Authenticate a user with username and password.
     * 
     * Requirements:
     * - 1.1: Authenticate the User and create a Session
     * - 1.2: Reject invalid credentials with error message
     * - 3.1: Create Session containing username, full name, and role
     * 
     * @param username the username to authenticate
     * @param password the plaintext password to verify
     * @return User object if authentication successful
     * @throws AuthenticationException if authentication fails
     */
    public User authenticate(String username, String password) throws AuthenticationException {
        logger.info("Authentication attempt for user: {}", username);
        
        // Validate input
        if (username == null || username.trim().isEmpty()) {
            logger.warn("Authentication failed: Username is empty");
            throw new AuthenticationException("Username cannot be empty");
        }
        
        if (password == null || password.trim().isEmpty()) {
            logger.warn("Authentication failed: Password is empty");
            throw new AuthenticationException("Password cannot be empty");
        }
        
        try {
            // Find user in database
            User user = userDAO.findByUsername(username.trim());
            
            if (user == null) {
                logger.warn("Authentication failed: User not found: {}", username);
                throw new AuthenticationException("Invalid username or password");
            }
            
            // Check if user account is active
            if (!"ACTIVE".equals(user.getStatus())) {
                logger.warn("Authentication failed: User account is inactive: {}", username);
                throw new AuthenticationException("Account is inactive. Please contact administrator.");
            }
            
            // Verify password using BCrypt
            boolean passwordValid = PasswordUtil.verifyPassword(password, user.getPasswordHash());
            
            if (!passwordValid) {
                logger.warn("Authentication failed: Invalid password for user: {}", username);
                throw new AuthenticationException("Invalid username or password");
            }
            
            // Authentication successful - create session (Requirement 3.1)
            Session session = Session.getInstance();
            session.setUsername(user.getUsername());
            session.setFullName(user.getFullName());
            session.setRole(UserRole.valueOf(user.getRole()));
            session.setLoginTime(LocalDateTime.now());
            
            // Update last_login in database
            userDAO.updateLastLogin(username);
            
            logger.info("Authentication successful for user: {} ({})", username, user.getRole());
            
            return user;
            
        } catch (SQLException e) {
            logger.error("Database error during authentication for user: {}", username, e);
            throw new AuthenticationException("System error occurred. Please try again later.");
        }
    }
    
    /**
     * Log out the current user.
     * Clears the session and terminates user access.
     * 
     * Requirements:
     * - 3.3: Terminate the Session on logout
     */
    public void logout() {
        Session session = Session.getInstance();
        String username = session.getUsername();
        
        // Clear the session
        Session.clear();
        
        logger.info("User logged out: {}", username);
    }
    
    /**
     * Check if the current user has a specific role.
     * Used for authorization checks throughout the application.
     * 
     * Requirements:
     * - 2.1: Role-based access control
     * 
     * @param role the role to check for
     * @return true if user has the specified role, false otherwise
     */
    public boolean hasRole(UserRole role) {
        Session session = Session.getInstance();
        
        if (!session.isActive()) {
            logger.debug("hasRole check failed: No active session");
            return false;
        }
        
        boolean hasRole = session.getRole() == role;
        logger.debug("Role check for {}: {} (actual role: {})", 
                     role, hasRole, session.getRole());
        
        return hasRole;
    }
    
    /**
     * Check if the current user is an admin.
     * Convenience method for admin checks.
     * 
     * @return true if current user is ADMIN, false otherwise
     */
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }
    
    /**
     * Check if there is an active user session.
     * 
     * @return true if session is active, false otherwise
     */
    public boolean isAuthenticated() {
        return Session.getInstance().isActive();
    }
    
    /**
     * Get the current logged-in username.
     * 
     * @return username if session is active, null otherwise
     */
    public String getCurrentUsername() {
        Session session = Session.getInstance();
        return session.isActive() ? session.getUsername() : null;
    }
}
