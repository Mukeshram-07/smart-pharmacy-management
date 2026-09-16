package com.pharmacy.management.model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Session singleton class
 * Validates Requirements 3.1, 3.2
 */
class SessionTest {
    
    @BeforeEach
    void setUp() {
        // Clear session before each test
        Session.clear();
    }
    
    @AfterEach
    void tearDown() {
        // Clean up after each test
        Session.clear();
    }
    
    @Test
    void testGetInstance_ReturnsSameInstance() {
        // Test singleton pattern - should return same instance
        Session session1 = Session.getInstance();
        Session session2 = Session.getInstance();
        
        assertSame(session1, session2, "getInstance should return the same instance");
    }
    
    @Test
    void testSessionFields_CanBeSetAndRetrieved() {
        // Test setting and getting session fields
        Session session = Session.getInstance();
        LocalDateTime loginTime = LocalDateTime.now();
        
        session.setUsername("testuser");
        session.setFullName("Test User");
        session.setRole(UserRole.ADMIN);
        session.setLoginTime(loginTime);
        
        assertEquals("testuser", session.getUsername());
        assertEquals("Test User", session.getFullName());
        assertEquals(UserRole.ADMIN, session.getRole());
        assertEquals(loginTime, session.getLoginTime());
    }
    
    @Test
    void testClear_RemovesSessionData() {
        // Test that clear() properly removes session data
        Session session = Session.getInstance();
        session.setUsername("testuser");
        session.setFullName("Test User");
        session.setRole(UserRole.PHARMACIST);
        session.setLoginTime(LocalDateTime.now());
        
        // Clear the session
        Session.clear();
        
        // Get new instance after clear
        Session newSession = Session.getInstance();
        
        assertNull(newSession.getUsername(), "Username should be null after clear");
        assertNull(newSession.getFullName(), "Full name should be null after clear");
        assertNull(newSession.getRole(), "Role should be null after clear");
        assertNull(newSession.getLoginTime(), "Login time should be null after clear");
    }
    
    @Test
    void testIsActive_ReturnsTrueWhenUsernameSet() {
        // Test isActive method
        Session session = Session.getInstance();
        
        assertFalse(session.isActive(), "Session should not be active initially");
        
        session.setUsername("testuser");
        assertTrue(session.isActive(), "Session should be active when username is set");
        
        session.setUsername("");
        assertFalse(session.isActive(), "Session should not be active with empty username");
        
        session.setUsername("   ");
        assertFalse(session.isActive(), "Session should not be active with whitespace username");
    }
    
    @Test
    void testIsAdmin_ReturnsTrueForAdminRole() {
        // Test isAdmin method
        Session session = Session.getInstance();
        
        assertFalse(session.isAdmin(), "Should return false when role is not set");
        
        session.setRole(UserRole.ADMIN);
        assertTrue(session.isAdmin(), "Should return true for ADMIN role");
        
        session.setRole(UserRole.PHARMACIST);
        assertFalse(session.isAdmin(), "Should return false for PHARMACIST role");
    }
    
    @Test
    void testSessionPersistence_DataPersistsAcrossGetInstance() {
        // Test that session data persists across getInstance calls
        Session session1 = Session.getInstance();
        session1.setUsername("user1");
        session1.setFullName("User One");
        session1.setRole(UserRole.PHARMACIST);
        
        Session session2 = Session.getInstance();
        
        assertEquals("user1", session2.getUsername(), "Username should persist");
        assertEquals("User One", session2.getFullName(), "Full name should persist");
        assertEquals(UserRole.PHARMACIST, session2.getRole(), "Role should persist");
    }
}
