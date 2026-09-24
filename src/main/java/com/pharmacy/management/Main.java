package com.pharmacy.management;

import com.formdev.flatlaf.FlatLightLaf;
import com.pharmacy.management.ui.LoginFrame;
import com.pharmacy.management.util.SchemaInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Main application entry point.
 * Initializes FlatLaf Look and Feel and starts the application.
 * 
 * Validates Requirement 9.1: Use FlatLaf Look and Feel for modern UI rendering
 */
public class Main {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            // Set FlatLaf Light theme (Requirement 9.1)
            FlatLightLaf.setup();
            
            // Configure UIManager properties for rounded components
            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            
            logger.info("FlatLaf Look and Feel initialized successfully");

            // Initialise Module 3 database schema (safe to run every startup)
            SchemaInitializer.initializeStockSchema();

            // Initialise Module 4 billing schema (safe to run every startup)
            SchemaInitializer.initializeBillingSchema();

            // Initialise Module 5 supplier schema (safe to run every startup)
            SchemaInitializer.initializeSupplierSchema();

            // Start the application on EDT
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
                logger.info("Application started successfully");
            });
            
        } catch (Exception e) {
            logger.error("Failed to initialize application", e);
            JOptionPane.showMessageDialog(
                null,
                "Failed to start application: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }
}
