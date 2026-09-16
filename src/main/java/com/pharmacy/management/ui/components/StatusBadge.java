package com.pharmacy.management.ui.components;

import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Modern status badge with glow effect and smooth animations
 * Requirement 9.4
 */
public class StatusBadge extends JLabel {
    
    private final Color bgColor;
    private final Color glowColor;
    private final String iconSymbol;
    
    public StatusBadge(String status) {
        super(status);
        setOpaque(false);
        setHorizontalAlignment(CENTER);
        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        
        if ("ACTIVE".equals(status)) {
            bgColor = new Color(16, 185, 129);
            glowColor = new Color(16, 185, 129, 60);
            iconSymbol = "● ";
            setForeground(Color.WHITE);
            setText(iconSymbol + status);
        } else {
            bgColor = new Color(239, 68, 68);
            glowColor = new Color(239, 68, 68, 60);
            iconSymbol = "● ";
            setForeground(Color.WHITE);
            setText(iconSymbol + status);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        int arc = height;
        
        // Outer glow
        g2.setColor(glowColor);
        g2.fill(new RoundRectangle2D.Float(2, 2, width - 4, height - 4, arc, arc));
        
        // Gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, bgColor,
            0, height, new Color(
                Math.max(0, bgColor.getRed() - 20),
                Math.max(0, bgColor.getGreen() - 20),
                Math.max(0, bgColor.getBlue() - 20)
            )
        );
        g2.setPaint(gradient);
        g2.fill(new RoundRectangle2D.Float(0, 0, width, height, arc, arc));
        
        // Highlight
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fill(new RoundRectangle2D.Float(1, 1, width - 2, height / 2, arc, arc));
        
        g2.dispose();
        super.paintComponent(g);
    }
    
    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.height = Math.max(size.height, 28);
        return size;
    }
}