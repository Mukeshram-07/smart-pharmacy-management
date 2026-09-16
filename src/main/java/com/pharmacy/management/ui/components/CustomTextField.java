package com.pharmacy.management.ui.components;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Custom text field with modern styling and rounded borders.
 * 
 * Validates Requirements:
 * - 9.1: Modern UI rendering
 * - 9.4: Rounded cards and consistent spacing
 */
public class CustomTextField extends JTextField {
    
    private int cornerRadius = 10;
    private Color borderColor = new Color(200, 200, 200);
    private Color focusBorderColor = new Color(52, 152, 219);
    
    /**
     * Create a custom text field with default columns.
     */
    public CustomTextField() {
        this(20);
    }
    
    /**
     * Create a custom text field with specified columns.
     * 
     * @param columns number of columns
     */
    public CustomTextField(int columns) {
        super(columns);
        initialize();
    }
    
    /**
     * Create a custom text field with specified text.
     * 
     * @param text initial text
     */
    public CustomTextField(String text) {
        super(text);
        initialize();
    }
    
    /**
     * Initialize text field properties.
     */
    private void initialize() {
        setFont(new Font("Segoe UI", Font.PLAIN, 14));
        setBorder(new RoundedBorder());
        setOpaque(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw rounded background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);
        
        g2.dispose();
        super.paintComponent(g);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw rounded border with different color for focus state
        if (isFocusOwner()) {
            g2.setColor(focusBorderColor);
        } else {
            g2.setColor(borderColor);
        }
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);
        
        g2.dispose();
    }
    
    /**
     * Inner class for rounded border with padding.
     */
    private class RoundedBorder extends EmptyBorder {
        public RoundedBorder() {
            super(8, 12, 8, 12); // Top, Left, Bottom, Right padding
        }
    }
    
    // Getters and setters
    
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }
    
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
        repaint();
    }
    
    public void setFocusBorderColor(Color focusBorderColor) {
        this.focusBorderColor = focusBorderColor;
        repaint();
    }
}
