package com.pharmacy.management.ui;

import com.pharmacy.management.model.Session;
import com.pharmacy.management.service.AuthenticationService;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import java.awt.*;

/**
 * Main application frame with sidebar navigation and dynamic content area.
 * 
 * Validates Requirements:
 * - 9.2: Sidebar navigation with Dashboard, Medicines, Prescription Scanner, Logout
 * - 9.3: Top header with application title, username, and role display
 * - 3.2: Display current user's name and role in header
 * - 3.3: Logout handler to clear session
 * - 14.3: Navigation between different views
 */
public class MainFrame extends JFrame {
    
    private JPanel contentPanel;
    private JLabel userInfoLabel;
    private AuthenticationService authService;
    private String currentView = "Dashboard";
    
    /**
     * Create the main application frame.
     */
    public MainFrame() {
        this.authService = new AuthenticationService();
        initializeUI();
        showDashboard(); // Show dashboard by default
    }
    
    /**
     * Initialize the main UI layout.
     */
    private void initializeUI() {
        setTitle("Smart Pharmacy Management System");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Main layout
        setLayout(new BorderLayout());
        
        // Create header
        add(createHeader(), BorderLayout.NORTH);
        
        // Create sidebar
        add(createSidebar(), BorderLayout.WEST);
        
        // Create content area
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        add(contentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Create the header panel with title and user info.
     * Validates Requirement 9.3 and 3.2
     */
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, UIConstants.BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 60));
        
        // Application title
        JLabel titleLabel = new JLabel("  Smart Pharmacy Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        header.add(titleLabel, BorderLayout.WEST);
        
        // User info (Requirement 3.2)
        Session session = Session.getInstance();
        String userText = String.format("👤 %s (%s)  ", 
                                       session.getFullName(), 
                                       session.getRole().toString());
        userInfoLabel = new JLabel(userText);
        userInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        userInfoLabel.setForeground(UIConstants.TEXT_SECONDARY);
        header.add(userInfoLabel, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * Create the sidebar with navigation buttons.
     * Validates Requirement 9.2
     */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIConstants.SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(200, 0));
        
        // Add spacing at top
        sidebar.add(Box.createVerticalStrut(20));
        
        // Navigation buttons
        addSidebarButton(sidebar, "📊 Dashboard", this::showDashboard);
        addSidebarButton(sidebar, "💊 Medicines", this::showMedicines);
        addSidebarButton(sidebar, "📦 Stock", this::showStock);
        addSidebarButton(sidebar, "💰 Billing", this::showBilling);
        addSidebarButton(sidebar, "📜 Bill History", this::showBillHistory);
        addSidebarButton(sidebar, "🏭 Suppliers", this::showSuppliers);
        addSidebarButton(sidebar, "📋 Purchases", this::showPurchaseHistory);
        addSidebarButton(sidebar, "📄 Scanner", this::showScanner);
        
        // Push logout button to bottom
        sidebar.add(Box.createVerticalGlue());
        
        addSidebarButton(sidebar, "🚪 Logout", this::logout);
        sidebar.add(Box.createVerticalStrut(20));
        
        return sidebar;
    }
    
    /**
     * Add a navigation button to the sidebar.
     */
    private void addSidebarButton(JPanel sidebar, String text, Runnable action) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(180, 40));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(UIConstants.TEXT_ON_DARK);
        button.setBackground(UIConstants.SIDEBAR_COLOR);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setOpaque(true);
                button.setBackground(UIConstants.SIDEBAR_HOVER);
                button.setContentAreaFilled(true);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setContentAreaFilled(false);
            }
        });
        
        button.addActionListener(e -> action.run());
        
        sidebar.add(button);
        sidebar.add(Box.createVerticalStrut(5));
    }
    
    /**
     * Switch the content panel to a new view.
     * Validates Requirement 14.3
     */
    private void switchPanel(JPanel newPanel, String viewName) {
        contentPanel.removeAll();
        contentPanel.add(newPanel, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
        currentView = viewName;
    }
    
    /**
     * Show the dashboard view.
     */
    private void showDashboard() {
        DashboardPanel dashboard = new DashboardPanel();
        switchPanel(dashboard, "Dashboard");
    }
    
    /**
     * Show the medicines management view.
     */
    private void showMedicines() {
        MedicinePanel medicinePanel = new MedicinePanel();
        switchPanel(medicinePanel, "Medicines");
    }
    
    /**
     * Show the stock management view.
     */
    private void showStock() {
        StockPanel stockPanel = new StockPanel();
        switchPanel(stockPanel, "Stock");
    }

    /**
     * Show the billing view.
     */
    private void showBilling() {
        BillingPanel billingPanel = new BillingPanel();
        switchPanel(billingPanel, "Billing");
    }

    /**
     * Show the bill history view.
     */
    private void showBillHistory() {
        BillHistoryPanel historyPanel = new BillHistoryPanel();
        switchPanel(historyPanel, "BillHistory");
    }

    /**
     * Show the supplier management view.
     */
    private void showSuppliers() {
        SupplierPanel panel = new SupplierPanel();
        switchPanel(panel, "Suppliers");
    }

    /**
     * Show the purchase history view.
     */
    private void showPurchaseHistory() {
        PurchaseHistoryPanel panel = new PurchaseHistoryPanel();
        switchPanel(panel, "PurchaseHistory");
    }

    /**
     * Show the prescription scanner view.
     */
    private void showScanner() {
        JPanel placeholder = createPlaceholder("📄 Prescription Scanner", 
                                              "OCR prescription scanning will be implemented here");
        switchPanel(placeholder, "Scanner");
    }
    
    /**
     * Handle logout action.
     * Validates Requirement 3.3
     */
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginFrame loginFrame = new LoginFrame();
                loginFrame.setVisible(true);
            });
        }
    }
    
    /**
     * Create a placeholder panel for views not yet implemented.
     */
    private JPanel createPlaceholder(String title, String message) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        messageLabel.setForeground(UIConstants.TEXT_SECONDARY);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(messageLabel);
        
        panel.add(card);
        return panel;
    }
}
