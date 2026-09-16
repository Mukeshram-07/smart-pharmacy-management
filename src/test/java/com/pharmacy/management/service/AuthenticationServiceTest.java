package com.pharmacy.management.service;

import com.pharmacy.management.dao.UserDAO;
import com.pharmacy.management.model.Session;
import com.pharmacy.management.model.User;
import com.pharmacy.management.model.UserRole;
import com.pharmacy.management.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationService
 * Validates Requirements 1.1, 1.2, 2.1, 3.1, 3.3
 */
class AuthenticationServiceTest {
    
    @Mock
    private UserDAO userDAO;
    
    private AuthenticationService authService;
    private User testUser;
    
    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        authService = new AuthenticationService(userDAO);
        Session.clear(); // Clear session before each test
        
        // Create test user
        testUser = new User();
        testUser.setId(1);
        testUser.setUsername("testuser");
        testUser.setPasswordHash(PasswordUtil.hashPassword("password123"));
        testUser.setFullName("Test User");
        testUser.setRole("ADMIN");
        testUser.setStatus("ACTIVE");
        testUser.setCreatedAt(LocalDateTime.now());
    }
    
    @AfterEach
    void tearDown() {
        Session.clear(); // Clean up after each test
    }
    
    @Test
    void testAuthenticate_ValidCredentials_ReturnsUser() throws Exception {
        // Arrange
        when(userDAO.findByUsername("testuser")).thenReturn(testUser);
        doNothing().when(userDAO).updateLastLogin("testuser");
        
        // Act
        User result = authService.authenticate("testuser", "password123");
        
        // Assert
        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("Test User", result.getFullName());
        assertEquals("ADMIN", result.getRole());
        
        // Verify session was created
        Session session = Session.getInstance();
        assertTrue(session.isActive());
        assertEquals("testuser", session.getUsername());
        assertEquals("Test User", session.getFullName());
        assertEquals(UserRole.ADMIN, session.getRole());
        
        verify(userDAO).findByUsername("testuser");
        verify(userDAO).updateLastLogin("testuser");
    }
    
    @Test
    void testAuthenticate_InvalidUsername_ThrowsAuthenticationException() throws Exception {
        // Arrange
        when(userDAO.findByUsername("invaliduser")).thenReturn(null);
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("invaliduser", "password123");
        });
        
        assertEquals("Invalid username or password", exception.getMessage());
        assertFalse(Session.getInstance().isActive());
        
        verify(userDAO).findByUsername("invaliduser");
        verify(userDAO, never()).updateLastLogin(anyString());
    }
    
    @Test
    void testAuthenticate_InvalidPassword_ThrowsAuthenticationException() throws Exception {
        // Arrange
        when(userDAO.findByUsername("testuser")).thenReturn(testUser);
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("testuser", "wrongpassword");
        });
        
        assertEquals("Invalid username or password", exception.getMessage());
        assertFalse(Session.getInstance().isActive());
        
        verify(userDAO).findByUsername("testuser");
        verify(userDAO, never()).updateLastLogin(anyString());
    }
    
    @Test
    void testAuthenticate_EmptyUsername_ThrowsAuthenticationException() {
        // Test empty username
        AuthenticationException exception1 = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("", "password123");
        });
        assertEquals("Username cannot be empty", exception1.getMessage());
        
        // Test null username
        AuthenticationException exception2 = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate(null, "password123");
        });
        assertEquals("Username cannot be empty", exception2.getMessage());
        
        // Test whitespace username
        AuthenticationException exception3 = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("   ", "password123");
        });
        assertEquals("Username cannot be empty", exception3.getMessage());
        
        assertFalse(Session.getInstance().isActive());
    }
    
    @Test
    void testAuthenticate_EmptyPassword_ThrowsAuthenticationException() {
        // Test empty password
        AuthenticationException exception1 = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("testuser", "");
        });
        assertEquals("Password cannot be empty", exception1.getMessage());
        
        // Test null password
        AuthenticationException exception2 = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("testuser", null);
        });
        assertEquals("Password cannot be empty", exception2.getMessage());
        
        assertFalse(Session.getInstance().isActive());
    }
    
    @Test
    void testAuthenticate_InactiveUser_ThrowsAuthenticationException() throws Exception {
        // Arrange
        testUser.setStatus("INACTIVE");
        when(userDAO.findByUsername("testuser")).thenReturn(testUser);
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("testuser", "password123");
        });
        
        assertEquals("Account is inactive. Please contact administrator.", exception.getMessage());
        assertFalse(Session.getInstance().isActive());
        
        verify(userDAO).findByUsername("testuser");
        verify(userDAO, never()).updateLastLogin(anyString());
    }
    
    @Test
    void testAuthenticate_DatabaseError_ThrowsAuthenticationException() throws Exception {
        // Arrange
        when(userDAO.findByUsername("testuser")).thenThrow(new SQLException("Database connection failed"));
        
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.authenticate("testuser", "password123");
        });
        
        assertEquals("System error occurred. Please try again later.", exception.getMessage());
        assertFalse(Session.getInstance().isActive());
        
        verify(userDAO).findByUsername("testuser");
    }
    
    @Test
    void testLogout_ClearsSession() {
        // Arrange - create an active session
        Session session = Session.getInstance();
        session.setUsername("testuser");
        session.setFullName("Test User");
        session.setRole(UserRole.ADMIN);
        session.setLoginTime(LocalDateTime.now());
        
        assertTrue(session.isActive());
        
        // Act
        authService.logout();
        
        // Assert
        Session newSession = Session.getInstance();
        assertFalse(newSession.isActive());
        assertNull(newSession.getUsername());
        assertNull(newSession.getFullName());
        assertNull(newSession.getRole());
        assertNull(newSession.getLoginTime());
    }
    
    @Test
    void testHasRole_AdminRole_ReturnsCorrectly() {
        // Arrange
        Session session = Session.getInstance();
        session.setUsername("admin");
        session.setRole(UserRole.ADMIN);
        
        // Act & Assert
        assertTrue(authService.hasRole(UserRole.ADMIN));
        assertFalse(authService.hasRole(UserRole.PHARMACIST));
    }
    
    @Test
    void testHasRole_PharmacistRole_ReturnsCorrectly() {
        // Arrange
        Session session = Session.getInstance();
        session.setUsername("pharmacist");
        session.setRole(UserRole.PHARMACIST);
        
        // Act & Assert
        assertTrue(authService.hasRole(UserRole.PHARMACIST));
        assertFalse(authService.hasRole(UserRole.ADMIN));
    }
    
    @Test
    void testHasRole_NoActiveSession_ReturnsFalse() {
        // Ensure no active session
        Session.clear();
        
        // Act & Assert
        assertFalse(authService.hasRole(UserRole.ADMIN));
        assertFalse(authService.hasRole(UserRole.PHARMACIST));
    }
    
    @Test
    void testIsAdmin_AdminUser_ReturnsTrue() {
        // Arrange
        Session session = Session.getInstance();
        session.setUsername("admin");
        session.setRole(UserRole.ADMIN);
        
        // Act & Assert
        assertTrue(authService.isAdmin());
    }
    
    @Test
    void testIsAdmin_PharmacistUser_ReturnsFalse() {
        // Arrange
        Session session = Session.getInstance();
        session.setUsername("pharmacist");
        session.setRole(UserRole.PHARMACIST);
        
        // Act & Assert
        assertFalse(authService.isAdmin());
    }
    
    @Test
    void testIsAuthenticated_ActiveSession_ReturnsTrue() {
        // Arrange
        Session session = Session.getInstance();
        session.setUsername("testuser");
        
        // Act & Assert
        assertTrue(authService.isAuthenticated());
    }
    
    @Test
    void testIsAuthenticated_NoActiveSession_ReturnsFalse() {
        // Ensure no active session
        Session.clear();
        
        // Act & Assert
        assertFalse(authService.isAuthenticated());
    }
    
    @Test
    void testGetCurrentUsername_ActiveSession_ReturnsUsername() {
        // Arrange
        Session session = Session.getInstance();
        session.setUsername("testuser");
        
        // Act & Assert
        assertEquals("testuser", authService.getCurrentUsername());
    }
    
    @Test
    void testGetCurrentUsername_NoActiveSession_ReturnsNull() {
        // Ensure no active session
        Session.clear();
        
        // Act & Assert
        assertNull(authService.getCurrentUsername());
    }
}