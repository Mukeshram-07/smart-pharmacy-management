package com.pharmacy.management.ui;

import com.pharmacy.management.model.User;
import com.pharmacy.management.service.AuthenticationException;
import com.pharmacy.management.service.AuthenticationService;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.ui.components.CustomTextField;
import com.pharmacy.management.ui.components.RoundedPanel;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Login frame for user authentication.
 * 
 * Validates Requirements:
 * - 1.1: User authentication with credential verification
 * - 1.2: Display error messages for authentication failures
 * - 9.6: Form validation before submission
 */
public class LoginFrame extends JFrame {
    
    private CustomTextField usernameField;
    private JPasswordField passwordField;
    private AuthenticationService authService;
    
    public LoginFrame() {
        this.authService = new AuthenticationService();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Login - Smart Pharmacy Management System");
        setSize(450, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        RoundedPanel loginCard = new RoundedPanel();
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        loginCard.setPreferredSize(new Dimension(380, 450));
        
        // Logo/Title
        JLabel titleLabel = new JLabel("Smart Pharmacy");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UIConstants.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel subtitleLabel = new JLabel("Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(UIConstants.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        loginCard.add(titleLabel);
        loginCard.add(Box.createVerticalStrut(5));
        loginCard.add(subtitleLabel);
        loginCard.add(Box.createVerticalStrut(40));
        
        // Username field
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        usernameField = new CustomTextField(20);
        usernameField.setMaximumSize(new Dimension(300, 40));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        loginCard.add(usernameLabel);
        loginCard.add(Box.createVerticalStrut(8));
        loginCard.add(usernameField);
        loginCard.add(Box.createVerticalStrut(20));
        
        // Password field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setMaximumSize(new Dimension(300, 40));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        loginCard.add(passwordLabel);
        loginCard.add(Box.createVerticalStrut(8));
        loginCard.add(passwordField);
        loginCard.add(Box.createVerticalStrut(30));
        
        // Login button
        CustomButton loginButton = new CustomButton("Login");
        loginButton.setMaximumSize(new Dimension(300, 45));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setBackgroundColor(UIConstants.PRIMARY_COLOR);
        loginButton.addActionListener(e -> handleLogin());
        
        loginCard.add(loginButton);
        loginCard.add(Box.createVerticalStrut(15));
        
        // Default credentials hint
        JLabel hintLabel = new JLabel("<html><center>Default: admin/password123<br>or pharmacist1/password123</center></html>");
        hintLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        hintLabel.setForeground(UIConstants.TEXT_SECONDARY);
        hintLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCard.add(hintLabel);
        
        // Enter key support
        passwordField.addActionListener(e -> handleLogin());
        
        mainPanel.add(loginCard);
        add(mainPanel);
    }
    
    /**
     * Handle login button click.
     * Validates Requirements 1.1, 1.2, 9.6
     */
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validate input (Requirement 9.6)
        if (username.isEmpty()) {
            showError("Please enter username");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Please enter password");
            passwordField.requestFocus();
            return;
        }
        
        try {
            // Attempt authentication (Requirement 1.1)
            User user = authService.authenticate(username, password);
            
            // Success - open main frame
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame();
                mainFrame.setVisible(true);
            });
            
        } catch (AuthenticationException e) {
            // Display error message (Requirement 1.2)
            showError(e.getMessage());
            passwordField.setText("");
            passwordField.requestFocus();
        }
    }
    
    /**
     * Show error message dialog.
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Login Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}
