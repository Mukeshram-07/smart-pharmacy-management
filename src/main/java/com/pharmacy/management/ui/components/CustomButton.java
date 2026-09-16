package com.pharmacy.management.ui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Ultra-modern button with gradient, shadow, animations, and SVG-style icons
 * 
 * Validates Requirements:
 * - 9.1: Modern UI rendering
 * - 9.4: Rounded cards and consistent spacing  
 * - 9.5: Hover states with smooth animations
 */
public class CustomButton extends JButton {
    
    private Color backgroundColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor = Color.WHITE;
    private int cornerRadius = 12;
    private float hoverProgress = 0f;
    private float pressProgress = 0f;
    private Timer animationTimer;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private String iconType = null;
    
    /**
     * Create a custom button with default primary color.
     */
    public CustomButton(String text) {
        this(text, new Color(79, 70, 229)); // Modern indigo
    }
    
    /**
     * Create a custom button with icon
     */
    public CustomButton(String text, String iconType) {
        this(text);
        this.iconType = iconType;
    }
    
    /**
     * Create a custom button with specified background color.
     */
    public CustomButton(String text, Color backgroundColor) {
        super(text);
        this.backgroundColor = backgroundColor;
        this.hoverColor = lightenColor(backgroundColor, 0.1f);
        this.pressedColor = darkenColor(backgroundColor, 0.1f);
        
        initialize();
    }
    
    private void initialize() {
        setForeground(textColor);
        setFont(new Font("Segoe UI", Font.BOLD, 14));
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setPreferredSize(new Dimension(120, 42));
        
        // Smooth hover animations
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                startAnimation(true, false);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                startAnimation(false, false);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                isPressed = true;
                startAnimation(true, true);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                startAnimation(false, true);
            }
        });
    }
    
    private void startAnimation(boolean forward, boolean isPress) {
        if (animationTimer != null) animationTimer.stop();
        
        animationTimer = new Timer(16, null);
        animationTimer.addActionListener(e -> {
            if (isPress) {
                if (forward) {
                    pressProgress = Math.min(1f, pressProgress + 0.15f);
                } else {
                    pressProgress = Math.max(0f, pressProgress - 0.15f);
                }
            } else {
                if (forward) {
                    hoverProgress = Math.min(1f, hoverProgress + 0.1f);
                } else {
                    hoverProgress = Math.max(0f, hoverProgress - 0.1f);
                }
            }
            repaint();
            
            if ((forward && (isPress ? pressProgress : hoverProgress) >= 1f) ||
                (!forward && (isPress ? pressProgress : hoverProgress) <= 0f)) {
                animationTimer.stop();
            }
        });
        animationTimer.start();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        int width = getWidth();
        int height = getHeight();
        
        // Calculate transform based on press
        int pressOffset = (int)(pressProgress * 2);
        
        // Drop shadow (elevated on hover)
        int shadowOffset = 3 + (int)(hoverProgress * 3);
        g2.setColor(new Color(0, 0, 0, (int)(20 + hoverProgress * 30)));
        g2.fill(new RoundRectangle2D.Float(
            shadowOffset, shadowOffset + pressOffset, 
            width - shadowOffset * 2, height - shadowOffset * 2, 
            cornerRadius, cornerRadius
        ));
        
        // Interpolate colors
        Color currentBg = interpolateColor(backgroundColor, hoverColor, hoverProgress);
        Color currentEnd = darkenColor(currentBg, 0.08f);
        
        // Gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, currentBg,
            0, height, currentEnd
        );
        g2.setPaint(gradient);
        g2.fill(new RoundRectangle2D.Float(
            pressOffset, pressOffset, 
            width - pressOffset * 2, height - pressOffset * 2, 
            cornerRadius, cornerRadius
        ));
        
        // Glossy highlight
        g2.setColor(new Color(255, 255, 255, (int)(40 - hoverProgress * 10)));
        g2.fill(new RoundRectangle2D.Float(
            pressOffset, pressOffset, 
            width - pressOffset * 2, (height - pressOffset * 2) / 2.5f, 
            cornerRadius, cornerRadius
        ));
        
        // Border glow
        g2.setColor(new Color(255, 255, 255, (int)(50 + hoverProgress * 50)));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new RoundRectangle2D.Float(
            pressOffset + 1, pressOffset + 1,
            width - pressOffset * 2 - 2, height - pressOffset * 2 - 2,
            cornerRadius - 1, cornerRadius - 1
        ));
        
        // Draw icon if present
        if (iconType != null) {
            drawIcon(g2, pressOffset, width, height);
        }
        
        // Draw text
        g2.setColor(textColor);
        g2.setFont(getFont());
        FontMetrics metrics = g2.getFontMetrics();
        int textX = (width - metrics.stringWidth(getText())) / 2;
        int textY = ((height - metrics.getHeight()) / 2) + metrics.getAscent();
        
        // Add icon spacing
        if (iconType != null) {
            textX += 10;
        }
        
        g2.drawString(getText(), textX + pressOffset, textY + pressOffset);
        
        g2.dispose();
    }
    
    private void drawIcon(Graphics2D g2, int offset, int width, int height) {
        int iconSize = 18;
        int iconX = (width - iconSize) / 2 - 30;
        int iconY = (height - iconSize) / 2;
        
        g2.setColor(textColor);
        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        switch (iconType) {
            case "add":
                // Plus sign
                g2.drawLine(iconX + iconSize/2 + offset, iconY + 3 + offset, 
                           iconX + iconSize/2 + offset, iconY + iconSize - 3 + offset);
                g2.drawLine(iconX + 3 + offset, iconY + iconSize/2 + offset,
                           iconX + iconSize - 3 + offset, iconY + iconSize/2 + offset);
                break;
            case "save":
                // Floppy disk
                g2.drawRect(iconX + offset, iconY + offset, iconSize, iconSize);
                g2.fillRect(iconX + 4 + offset, iconY + offset, iconSize - 8, 6);
                break;
            case "search":
                // Magnifying glass
                g2.drawOval(iconX + offset, iconY + offset, iconSize - 5, iconSize - 5);
                g2.drawLine(iconX + iconSize - 6 + offset, iconY + iconSize - 6 + offset,
                           iconX + iconSize + offset, iconY + iconSize + offset);
                break;
        }
    }
    
    private Color lightenColor(Color color, float factor) {
        int r = Math.min(255, (int)(color.getRed() + (255 - color.getRed()) * factor));
        int g = Math.min(255, (int)(color.getGreen() + (255 - color.getGreen()) * factor));
        int b = Math.min(255, (int)(color.getBlue() + (255 - color.getBlue()) * factor));
        return new Color(r, g, b);
    }
    
    private Color darkenColor(Color color, float factor) {
        int r = (int)(color.getRed() * (1 - factor));
        int g = (int)(color.getGreen() * (1 - factor));
        int b = (int)(color.getBlue() * (1 - factor));
        return new Color(r, g, b);
    }
    
    private Color interpolateColor(Color c1, Color c2, float progress) {
        int r = (int)(c1.getRed() + (c2.getRed() - c1.getRed()) * progress);
        int g = (int)(c1.getGreen() + (c2.getGreen() - c1.getGreen()) * progress);
        int b = (int)(c1.getBlue() + (c2.getBlue() - c1.getBlue()) * progress);
        return new Color(r, g, b);
    }
    
    // Setters for customization
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.hoverColor = lightenColor(backgroundColor, 0.1f);
        this.pressedColor = darkenColor(backgroundColor, 0.1f);
        repaint();
    }
    
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
        repaint();
    }
    
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }
}
