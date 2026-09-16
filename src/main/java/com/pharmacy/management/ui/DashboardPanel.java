package com.pharmacy.management.ui;

import com.pharmacy.management.service.MedicineService;
import com.pharmacy.management.ui.components.RoundedPanel;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;

/**
 * Modern Dashboard panel with stunning visuals and animations.
 * 
 * Validates Requirements:
 * - 14.1: Display main dashboard on successful login
 * - 14.2: Display summary statistics (total medicines, active users)
 * - 14.4: Use RoundedPanel for card design
 */
public class DashboardPanel extends JPanel {
    
    private MedicineService medicineService;
    private JLabel totalMedicinesValue;
    private JLabel activeMedicinesValue;
    private JLabel activeUsersValue;
    
    public DashboardPanel() {
        this.medicineService = new MedicineService();
        setLayout(new BorderLayout());
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Header with gradient
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);
        
        // Stats cards with modern design
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 25, 25));
        statsPanel.setBackground(new Color(245, 247, 250));
        statsPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        
        // Create cards
        statsPanel.add(createModernStatCard("Total Medicines", "0", 
            new Color(79, 70, 229), "pills"));
        statsPanel.add(createModernStatCard("Active Medicines", "0", 
            new Color(16, 185, 129), "check"));
        statsPanel.add(createModernStatCard("Active Users", "2", 
            new Color(245, 158, 11), "users"));
        
        add(statsPanel, BorderLayout.CENTER);
        
        // Load real statistics
        loadStatistics();
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(245, 247, 250));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        titleLabel.setForeground(new Color(17, 24, 39));
        
        JLabel subtitleLabel = new JLabel("Welcome back! Here's what's happening today.");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(new Color(245, 247, 250));
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitleLabel);
        
        header.add(textPanel, BorderLayout.WEST);
        
        return header;
    }
    
    private void loadStatistics() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            private int totalMedicines = 0;
            private int activeMedicines = 0;
            
            @Override
            protected Void doInBackground() throws Exception {
                var medicines = medicineService.getAllMedicines();
                totalMedicines = medicines.size();
                activeMedicines = (int) medicines.stream()
                    .filter(m -> "ACTIVE".equals(m.getStatus()))
                    .count();
                return null;
            }
            
            @Override
            protected void done() {
                // Animate counter updates
                animateCounter(totalMedicinesValue, totalMedicines);
                animateCounter(activeMedicinesValue, activeMedicines);
            }
        };
        
        worker.execute();
    }
    
    private void animateCounter(JLabel label, int targetValue) {
        Timer timer = new Timer(20, null);
        final int[] currentValue = {0};
        final int increment = Math.max(1, targetValue / 30);
        
        timer.addActionListener(e -> {
            currentValue[0] += increment;
            if (currentValue[0] >= targetValue) {
                currentValue[0] = targetValue;
                timer.stop();
            }
            label.setText(String.valueOf(currentValue[0]));
        });
        
        timer.start();
    }
    
    /**
     * Create a modern statistics card with custom icon and gradient effects
     */
    private GradientStatCard createModernStatCard(String title, String initialValue, 
                                                  Color primaryColor, String iconType) {
        GradientStatCard card = new GradientStatCard(title, initialValue, primaryColor, iconType);
        
        // Store references for updating
        if (title.contains("Total Medicines")) {
            totalMedicinesValue = card.getValueLabel();
        } else if (title.contains("Active Medicines")) {
            activeMedicinesValue = card.getValueLabel();
        } else if (title.contains("Active Users")) {
            activeUsersValue = card.getValueLabel();
        }
        
        return card;
    }
    
    /**
     * Modern gradient stat card with custom painted icons and hover effects
     */
    private static class GradientStatCard extends JPanel {
        private final String title;
        private final JLabel valueLabel;
        private final Color primaryColor;
        private final String iconType;
        private boolean isHovered = false;
        private float hoverProgress = 0f;
        private Timer hoverTimer;
        
        public GradientStatCard(String title, String initialValue, Color primaryColor, String iconType) {
            this.title = title;
            this.primaryColor = primaryColor;
            this.iconType = iconType;
            
            setLayout(new BorderLayout());
            setOpaque(false);
            setPreferredSize(new Dimension(300, 180));
            
            // Icon panel
            IconPanel iconPanel = new IconPanel(iconType, primaryColor);
            iconPanel.setPreferredSize(new Dimension(70, 70));
            
            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setOpaque(false);
            topPanel.setBorder(BorderFactory.createEmptyBorder(25, 25, 10, 25));
            topPanel.add(iconPanel, BorderLayout.WEST);
            
            // Value label
            valueLabel = new JLabel(initialValue);
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));
            valueLabel.setForeground(new Color(17, 24, 39));
            
            // Title label
            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            titleLabel.setForeground(new Color(107, 114, 128));
            
            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);
            textPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 25, 25));
            textPanel.add(valueLabel);
            textPanel.add(Box.createVerticalStrut(5));
            textPanel.add(titleLabel);
            
            add(topPanel, BorderLayout.NORTH);
            add(textPanel, BorderLayout.CENTER);
            
            // Hover effect
            setupHoverEffect();
        }
        
        private void setupHoverEffect() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    startHoverAnimation(true);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    startHoverAnimation(false);
                }
            });
        }
        
        private void startHoverAnimation(boolean forward) {
            if (hoverTimer != null) hoverTimer.stop();
            
            hoverTimer = new Timer(16, null);
            hoverTimer.addActionListener(e -> {
                if (forward) {
                    hoverProgress = Math.min(1f, hoverProgress + 0.1f);
                } else {
                    hoverProgress = Math.max(0f, hoverProgress - 0.1f);
                }
                repaint();
                
                if ((forward && hoverProgress >= 1f) || (!forward && hoverProgress <= 0f)) {
                    hoverTimer.stop();
                }
            });
            hoverTimer.start();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int width = getWidth();
            int height = getHeight();
            
            // Shadow effect
            g2.setColor(new Color(0, 0, 0, (int)(15 + hoverProgress * 15)));
            g2.fillRoundRect(5, 5 + (int)(5 - hoverProgress * 5), width - 10, height - 10, 20, 20);
            
            // Gradient background
            GradientPaint gradient = new GradientPaint(
                0, 0, Color.WHITE,
                0, height, new Color(250, 250, 255)
            );
            g2.setPaint(gradient);
            g2.fillRoundRect(0, 0, width, height, 20, 20);
            
            // Accent border with primary color
            g2.setColor(new Color(primaryColor.getRed(), primaryColor.getGreen(), 
                primaryColor.getBlue(), (int)(50 + hoverProgress * 100)));
            g2.setStroke(new BasicStroke(2 + hoverProgress * 2));
            g2.drawRoundRect(0, 0, width - 1, height - 1, 20, 20);
            
            g2.dispose();
        }
        
        public JLabel getValueLabel() {
            return valueLabel;
        }
    }
    
    /**
     * Custom icon panel with hand-drawn SVG-style icons
     */
    private static class IconPanel extends JPanel {
        private final String iconType;
        private final Color iconColor;
        
        public IconPanel(String iconType, Color iconColor) {
            this.iconType = iconType;
            this.iconColor = iconColor;
            setOpaque(false);
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            
            int size = Math.min(getWidth(), getHeight());
            int padding = size / 6;
            
            // Background circle with gradient
            GradientPaint bgGradient = new GradientPaint(
                0, 0, new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 30),
                size, size, new Color(iconColor.getRed(), iconColor.getGreen(), iconColor.getBlue(), 50)
            );
            g2.setPaint(bgGradient);
            g2.fillOval(0, 0, size, size);
            
            // Draw icon
            g2.setColor(iconColor);
            g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            switch (iconType) {
                case "pills":
                    drawPillsIcon(g2, padding, size);
                    break;
                case "check":
                    drawCheckIcon(g2, padding, size);
                    break;
                case "users":
                    drawUsersIcon(g2, padding, size);
                    break;
            }
            
            g2.dispose();
        }
        
        private void drawPillsIcon(Graphics2D g2, int padding, int size) {
            int cx = size / 2;
            int cy = size / 2;
            int pillWidth = size - padding * 2;
            int pillHeight = pillWidth / 3;
            
            // Capsule pill
            g2.rotate(Math.toRadians(-45), cx, cy);
            g2.drawRoundRect(cx - pillWidth/2, cy - pillHeight/2, pillWidth, pillHeight, pillHeight, pillHeight);
            g2.drawLine(cx - pillWidth/4, cy - pillHeight/2, cx - pillWidth/4, cy + pillHeight/2);
            g2.rotate(Math.toRadians(45), cx, cy);
        }
        
        private void drawCheckIcon(Graphics2D g2, int padding, int size) {
            // Checkmark
            int[] xPoints = {padding + 8, padding + size/3, size - padding - 5};
            int[] yPoints = {size/2, size - padding - 8, padding + 5};
            
            GeneralPath path = new GeneralPath();
            path.moveTo(xPoints[0], yPoints[0]);
            path.lineTo(xPoints[1], yPoints[1]);
            path.lineTo(xPoints[2], yPoints[2]);
            g2.draw(path);
        }
        
        private void drawUsersIcon(Graphics2D g2, int padding, int size) {
            int headRadius = size / 8;
            int centerX = size / 2;
            int topY = padding + headRadius + 5;
            
            // Draw two users
            // User 1
            g2.drawOval(centerX - headRadius - 8, topY, headRadius * 2, headRadius * 2);
            Arc2D body1 = new Arc2D.Float(centerX - headRadius - 8 - 5, topY + headRadius * 2 - 5, 
                headRadius * 2 + 10, headRadius * 3, 200, 140, Arc2D.OPEN);
            g2.draw(body1);
            
            // User 2
            g2.drawOval(centerX - headRadius + 8, topY, headRadius * 2, headRadius * 2);
            Arc2D body2 = new Arc2D.Float(centerX - headRadius + 8 - 5, topY + headRadius * 2 - 5, 
                headRadius * 2 + 10, headRadius * 3, 200, 140, Arc2D.OPEN);
            g2.draw(body2);
        }
    }
}
