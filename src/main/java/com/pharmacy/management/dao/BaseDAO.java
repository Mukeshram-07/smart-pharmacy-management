package com.pharmacy.management.dao;

import com.pharmacy.management.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Abstract base class for all DAO (Data Access Object) classes.
 * Provides common database utility methods for connection management and resource cleanup.
 * 
 * Validates Requirement 12.2: Separate database access logic (DAO layer) from business logic
 */
public abstract class BaseDAO {
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Get a database connection from the connection pool.
     * 
     * @return Connection object from the pool
     * @throws SQLException if connection cannot be obtained
     */
    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }
    
    /**
     * Close database resources safely.
     * This method handles null checks and logs any exceptions during cleanup.
     * 
     * Usage: Call this method in finally blocks or use try-with-resources instead.
     * 
     * @param conn Connection to close
     * @param ps PreparedStatement to close
     * @param rs ResultSet to close
     */
    protected void closeResources(Connection conn, PreparedStatement ps, ResultSet rs) {
        // Close ResultSet
        if (rs != null) {
            try {
                rs.close();
                logger.debug("ResultSet closed");
            } catch (SQLException e) {
                logger.warn("Error closing ResultSet", e);
            }
        }
        
        // Close PreparedStatement
        if (ps != null) {
            try {
                ps.close();
                logger.debug("PreparedStatement closed");
            } catch (SQLException e) {
                logger.warn("Error closing PreparedStatement", e);
            }
        }
        
        // Close Connection
        if (conn != null) {
            try {
                conn.close();
                logger.debug("Connection returned to pool");
            } catch (SQLException e) {
                logger.warn("Error closing Connection", e);
            }
        }
    }
    
    /**
     * Close database resources safely (without ResultSet).
     * Overloaded version for operations that don't use ResultSet.
     * 
     * @param conn Connection to close
     * @param ps PreparedStatement to close
     */
    protected void closeResources(Connection conn, PreparedStatement ps) {
        closeResources(conn, ps, null);
    }
    
    /**
     * Close a single connection safely.
     * 
     * @param conn Connection to close
     */
    protected void closeConnection(Connection conn) {
        closeResources(conn, null, null);
    }
}
