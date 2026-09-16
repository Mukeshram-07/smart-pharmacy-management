package com.pharmacy.management.ui.components;

import javax.swing.*;
import java.awt.*;

/**
 * Custom panel with rounded corners and optional shadow effect.
 * Used for card-style layouts throughout the application.
 * 
 * Validates Requirements:
 * - 9.1: Modern UI rendering
 * - 9.4: Rounded cards, consistent spacing, modern typography
 */
public class RoundedPanel extends JPanel {
    
    private int cornerRadius = 15;
    private Color backgroundColor = Color.WHITE;
    private boolean hasShadow = true;
    private Color shadowColor = new Color(0, 0, 0, 30);
    
    /**
     * Create a rounded panel with default settings.
     */
    public RoundedPanel() {
        this(null);
    }
    
    /**
     * Create a rounded panel with specified layout manager.
     * 
     * @param layout layout manager
     */
    public RoundedPanel(LayoutManager layout) {
        super(layout);
        setOpaque(false);
        setBackground(backgroundColor);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw shadow if enabled
        if (hasShadow) {
            g2.setColor(shadowColor);
            g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 
                           cornerRadius, cornerRadius);
        }
        
        // Draw rounded panel background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 
                       cornerRadius, cornerRadius);
        
        g2.dispose();
    }
    
    @Override
    public void setBackground(Color bg) {
        super.setBackground(bg);
        this.backgroundColor = bg;
    }
    
    // Getters and setters
    
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }
    
    public int getCornerRadius() {
        return cornerRadius;
    }
    
    public void setHasShadow(boolean hasShadow) {
        this.hasShadow = hasShadow;
        repaint();
    }
    
    public boolean isHasShadow() {
        return hasShadow;
    }
    
    public void setShadowColor(Color shadowColor) {
        this.shadowColor = shadowColor;
        repaint();
    }
}
