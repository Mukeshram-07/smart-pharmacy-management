package com.pharmacy.management.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Database configuration and connection management using HikariCP connection pool.
 * This class provides a singleton connection pool for efficient database access.
 * 
 * Requirements: 12.4 - Reusable database connection utility
 */
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;
    
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/thotho";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "mukesh";
    
    // Static initializer to configure HikariCP connection pool
    static {
        try {
            HikariConfig config = new HikariConfig();
            
            // JDBC connection settings
            config.setJdbcUrl(DB_URL);
            config.setUsername(DB_USERNAME);
            config.setPassword(DB_PASSWORD);
            
            // Connection pool settings
            config.setMaximumPoolSize(10);        // Maximum number of connections in pool
            config.setMinimumIdle(2);             // Minimum number of idle connections
            config.setConnectionTimeout(30000);   // 30 seconds connection timeout
            config.setIdleTimeout(600000);        // 10 minutes idle timeout
            config.setMaxLifetime(1800000);       // 30 minutes max lifetime
            
            // Performance and reliability settings
            config.setAutoCommit(true);
            config.setConnectionTestQuery("SELECT 1");
            
            // Pool name for monitoring
            config.setPoolName("PharmacyDBPool");
            
            // Driver class name
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            // Additional MySQL-specific properties
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            logger.info("HikariCP connection pool initialized successfully");
            
        } catch (Exception e) {
            logger.error("Failed to initialize database connection pool", e);
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }
    
    /**
     * Get a database connection from the connection pool.
     * 
     * @return Connection object from the pool
     * @throws SQLException if connection cannot be obtained
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            logger.error("DataSource is not initialized");
            throw new SQLException("DataSource is not initialized");
        }
        
        Connection connection = dataSource.getConnection();
        logger.debug("Connection obtained from pool. Active connections: {}", 
                     dataSource.getHikariPoolMXBean().getActiveConnections());
        
        return connection;
    }
    
    /**
     * Close the connection pool and release all resources.
     * Should be called when the application shuts down.
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP connection pool closed");
        }
    }
    
    /**
     * Get connection pool statistics for monitoring.
     * 
     * @return String containing pool statistics
     */
    public static String getPoolStats() {
        if (dataSource != null) {
            return String.format(
                "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection()
            );
        }
        return "Pool not initialized";
    }
    
    // Private constructor to prevent instantiation
    private DatabaseConfig() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
