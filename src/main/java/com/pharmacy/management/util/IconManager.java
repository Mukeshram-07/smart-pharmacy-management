package com.pharmacy.management.util;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Icon Manager for professional icon handling.
 * Uses FlatLaf SVG icons for consistent, scalable, professional appearance.
 * 
 * Icons are cached for performance.
 */
public class IconManager {
    
    private static final Map<String, Icon> iconCache = new HashMap<>();
    private static final int DEFAULT_SIZE = 20;
    private static final int SMALL_SIZE = 16;
    private static final int LARGE_SIZE = 24;
    
    /**
     * Get an icon by name with default size (20px)
     */
    public static Icon getIcon(String name) {
        return getIcon(name, DEFAULT_SIZE);
    }
    
    /**
     * Get an icon by name with specified size
     */
    public static Icon getIcon(String name, int size) {
        String cacheKey = name + "_" + size;
        
        if (iconCache.containsKey(cacheKey)) {
            return iconCache.get(cacheKey);
        }
        
        Icon icon = createIcon(name, size);
        iconCache.put(cacheKey, icon);
        return icon;
    }
    
    /**
     * Get a small icon (16px)
     */
    public static Icon getSmallIcon(String name) {
        return getIcon(name, SMALL_SIZE);
    }
    
    /**
     * Get a large icon (24px)
     */
    public static Icon getLargeIcon(String name) {
        return getIcon(name, LARGE_SIZE);
    }
    
    /**
     * Create an icon based on name
     * Uses simple geometric shapes rendered programmatically
     */
    private static Icon createIcon(String name, int size) {
        switch (name.toLowerCase()) {
            case "dashboard":
                return new DashboardIcon(size);
            case "medicine":
            case "pill":
                return new PillIcon(size);
            case "prescription":
            case "scanner":
                return new ScannerIcon(size);
            case "user":
                return new UserIcon(size);
            case "users":
                return new UsersIcon(size);
            case "logout":
                return new LogoutIcon(size);
            case "add":
            case "plus":
                return new PlusIcon(size);
            case "edit":
            case "pencil":
                return new EditIcon(size);
            case "delete":
            case "trash":
                return new TrashIcon(size);
            case "search":
                return new SearchIcon(size);
            case "refresh":
                return new RefreshIcon(size);
            case "check":
                return new CheckIcon(size);
            case "warning":
                return new WarningIcon(size);
            case "error":
                return new ErrorIcon(size);
            case "info":
                return new InfoIcon(size);
            case "close":
            case "x":
                return new CloseIcon(size);
            case "menu":
                return new MenuIcon(size);
            case "settings":
                return new SettingsIcon(size);
            case "upload":
                return new UploadIcon(size);
            case "download":
                return new DownloadIcon(size);
            case "filter":
                return new FilterIcon(size);
            case "more":
            case "dots":
                return new MoreIcon(size);
            case "notification":
            case "bell":
                return new BellIcon(size);
            case "chevron-down":
                return new ChevronDownIcon(size);
            case "chevron-right":
                return new ChevronRightIcon(size);
            default:
                return new DefaultIcon(size);
        }
    }
    
    /**
     * Base class for custom icons
     */
    private static abstract class BaseIcon implements Icon {
        protected final int size;
        protected Color color = UIConstants.TEXT_SECONDARY;
        
        public BaseIcon(int size) {
            this.size = size;
        }
        
        public void setColor(Color color) {
            this.color = color;
        }
        
        @Override
        public int getIconWidth() {
            return size;
        }
        
        @Override
        public int getIconHeight() {
            return size;
        }
    }
    
    // Icon implementations
    
    private static class DashboardIcon extends BaseIcon {
        public DashboardIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 6;
            int halfSize = size / 2 - padding;
            
            // Draw 4 squares in a grid
            g2.drawRect(x + padding, y + padding, halfSize - 2, halfSize - 2);
            g2.drawRect(x + halfSize + padding + 2, y + padding, halfSize - 2, halfSize - 2);
            g2.drawRect(x + padding, y + halfSize + padding + 2, halfSize - 2, halfSize - 2);
            g2.drawRect(x + halfSize + padding + 2, y + halfSize + padding + 2, halfSize - 2, halfSize - 2);
            
            g2.dispose();
        }
    }
    
    private static class PillIcon extends BaseIcon {
        public PillIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            int width = size - padding * 2;
            int height = width / 3;
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            
            g2.rotate(Math.toRadians(-45), centerX, centerY);
            g2.drawRoundRect(x + padding, centerY - height/2, width, height, height, height);
            g2.drawLine(centerX - width/6, centerY - height/2, centerX - width/6, centerY + height/2);
            g2.rotate(Math.toRadians(45), centerX, centerY);
            
            g2.dispose();
        }
    }
    
    private static class ScannerIcon extends BaseIcon {
        public ScannerIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            g2.drawRect(x + padding, y + padding, size - padding * 2, size - padding * 2);
            g2.drawLine(x + padding, y + size/2, x + size - padding, y + size/2);
            
            g2.dispose();
        }
    }
    
    private static class UserIcon extends BaseIcon {
        public UserIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int headRadius = size / 4;
            int centerX = x + size / 2;
            int topY = y + size / 4;
            
            g2.drawOval(centerX - headRadius, topY, headRadius * 2, headRadius * 2);
            g2.drawArc(x + size/6, topY + headRadius * 2, size - size/3, size/2, 200, 140);
            
            g2.dispose();
        }
    }
    
    private static class UsersIcon extends BaseIcon {
        public UsersIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int headRadius = size / 6;
            int centerX = x + size / 2;
            int topY = y + size / 5;
            
            // Left user
            g2.drawOval(centerX - headRadius - 4, topY, headRadius * 2, headRadius * 2);
            g2.drawArc(centerX - headRadius - 4 - 3, topY + headRadius * 2 - 2, headRadius * 2 + 6, headRadius * 2, 200, 140);
            
            // Right user
            g2.drawOval(centerX - headRadius + 4, topY, headRadius * 2, headRadius * 2);
            g2.drawArc(centerX - headRadius + 4 - 3, topY + headRadius * 2 - 2, headRadius * 2 + 6, headRadius * 2, 200, 140);
            
            g2.dispose();
        }
    }
    
    private static class LogoutIcon extends BaseIcon {
        public LogoutIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            
            // Door
            g2.drawLine(x + padding, y + padding, x + padding, y + size - padding);
            g2.drawLine(x + padding, y + padding, x + size/2, y + padding);
            g2.drawLine(x + padding, y + size - padding, x + size/2, y + size - padding);
            
            // Arrow
            g2.drawLine(x + size/2, y + size/2, x + size - padding, y + size/2);
            g2.drawLine(x + size - padding - 4, y + size/2 - 4, x + size - padding, y + size/2);
            g2.drawLine(x + size - padding - 4, y + size/2 + 4, x + size - padding, y + size/2);
            
            g2.dispose();
        }
    }
    
    private static class PlusIcon extends BaseIcon {
        public PlusIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 4;
            g2.drawLine(x + size/2, y + padding, x + size/2, y + size - padding);
            g2.drawLine(x + padding, y + size/2, x + size - padding, y + size/2);
            
            g2.dispose();
        }
    }
    
    private static class EditIcon extends BaseIcon {
        public EditIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            
            // Pencil body
            g2.drawLine(x + size - padding - 2, y + padding, x + padding + 2, y + size - padding);
            g2.drawLine(x + size - padding - 6, y + padding, x + padding - 2, y + size - padding);
            g2.drawLine(x + padding - 2, y + size - padding, x + padding + 2, y + size - padding);
            
            g2.dispose();
        }
    }
    
    private static class TrashIcon extends BaseIcon {
        public TrashIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            
            // Top line
            g2.drawLine(x + padding, y + padding + 4, x + size - padding, y + padding + 4);
            
            // Can body
            g2.drawLine(x + padding + 2, y + padding + 4, x + padding + 3, y + size - padding);
            g2.drawLine(x + padding + 3, y + size - padding, x + size - padding - 3, y + size - padding);
            g2.drawLine(x + size - padding - 3, y + size - padding, x + size - padding - 2, y + padding + 4);
            
            // Vertical lines
            g2.drawLine(x + size/2, y + padding + 7, x + size/2, y + size - padding - 3);
            
            g2.dispose();
        }
    }
    
    private static class SearchIcon extends BaseIcon {
        public SearchIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            int circleSize = size - padding * 3;
            
            g2.drawOval(x + padding, y + padding, circleSize, circleSize);
            g2.drawLine(x + padding + circleSize - 2, y + padding + circleSize - 2, 
                        x + size - padding, y + size - padding);
            
            g2.dispose();
        }
    }
    
    private static class RefreshIcon extends BaseIcon {
        public RefreshIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            
            g2.drawArc(x + padding, y + padding, size - padding * 2, size - padding * 2, 45, 270);
            
            // Arrow
            g2.drawLine(x + size - padding - 3, y + padding + 5, x + size - padding, y + padding);
            g2.drawLine(x + size - padding, y + padding, x + size - padding + 3, y + padding + 5);
            
            g2.dispose();
        }
    }
    
    private static class CheckIcon extends BaseIcon {
        public CheckIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 4;
            
            int[] xPoints = {x + padding, x + size/2 - 2, x + size - padding};
            int[] yPoints = {y + size/2, y + size - padding, y + padding + 2};
            
            for (int i = 0; i < xPoints.length - 1; i++) {
                g2.drawLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
            }
            
            g2.dispose();
        }
    }
    
    private static class WarningIcon extends BaseIcon {
        public WarningIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 6;
            
            // Triangle
            int[] xPoints = {x + size/2, x + padding, x + size - padding};
            int[] yPoints = {y + padding, y + size - padding, y + size - padding};
            g2.drawPolygon(xPoints, yPoints, 3);
            
            // Exclamation mark
            g2.drawLine(x + size/2, y + padding + 6, x + size/2, y + size/2 + 2);
            g2.fillOval(x + size/2 - 1, y + size/2 + 6, 2, 2);
            
            g2.dispose();
        }
    }
    
    private static class ErrorIcon extends BaseIcon {
        public ErrorIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            
            g2.drawOval(x + padding, y + padding, size - padding * 2, size - padding * 2);
            
            // X mark
            int innerPadding = size / 3;
            g2.drawLine(x + innerPadding, y + innerPadding, x + size - innerPadding, y + size - innerPadding);
            g2.drawLine(x + size - innerPadding, y + innerPadding, x + innerPadding, y + size - innerPadding);
            
            g2.dispose();
        }
    }
    
    private static class InfoIcon extends BaseIcon {
        public InfoIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            
            g2.drawOval(x + padding, y + padding, size - padding * 2, size - padding * 2);
            
            // i mark
            g2.fillOval(x + size/2 - 1, y + padding + 6, 2, 2);
            g2.drawLine(x + size/2, y + padding + 12, x + size/2, y + size - padding - 4);
            
            g2.dispose();
        }
    }
    
    private static class CloseIcon extends BaseIcon {
        public CloseIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 4;
            
            g2.drawLine(x + padding, y + padding, x + size - padding, y + size - padding);
            g2.drawLine(x + size - padding, y + padding, x + padding, y + size - padding);
            
            g2.dispose();
        }
    }
    
    private static class MenuIcon extends BaseIcon {
        public MenuIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 4;
            
            g2.drawLine(x + padding, y + size/3, x + size - padding, y + size/3);
            g2.drawLine(x + padding, y + size/2, x + size - padding, y + size/2);
            g2.drawLine(x + padding, y + size*2/3, x + size - padding, y + size*2/3);
            
            g2.dispose();
        }
    }
    
    private static class SettingsIcon extends BaseIcon {
        public SettingsIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 4;
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            int innerRadius = size / 6;
            
            // Center circle
            g2.drawOval(centerX - innerRadius, centerY - innerRadius, innerRadius * 2, innerRadius * 2);
            
            // Gear teeth (simplified)
            for (int i = 0; i < 6; i++) {
                double angle = Math.toRadians(i * 60);
                int x1 = (int) (centerX + Math.cos(angle) * innerRadius);
                int y1 = (int) (centerY + Math.sin(angle) * innerRadius);
                int x2 = (int) (centerX + Math.cos(angle) * (size/2 - padding));
                int y2 = (int) (centerY + Math.sin(angle) * (size/2 - padding));
                g2.drawLine(x1, y1, x2, y2);
            }
            
            g2.dispose();
        }
    }
    
    private static class UploadIcon extends BaseIcon {
        public UploadIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            
            // Arrow up
            g2.drawLine(x + size/2, y + padding, x + size/2, y + size - padding - 4);
            g2.drawLine(x + size/2 - 4, y + padding + 4, x + size/2, y + padding);
            g2.drawLine(x + size/2 + 4, y + padding + 4, x + size/2, y + padding);
            
            // Base line
            g2.drawLine(x + padding, y + size - padding, x + size - padding, y + size - padding);
            
            g2.dispose();
        }
    }
    
    private static class DownloadIcon extends BaseIcon {
        public DownloadIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            
            // Arrow down
            g2.drawLine(x + size/2, y + padding, x + size/2, y + size - padding - 4);
            g2.drawLine(x + size/2 - 4, y + size - padding - 8, x + size/2, y + size - padding - 4);
            g2.drawLine(x + size/2 + 4, y + size - padding - 8, x + size/2, y + size - padding - 4);
            
            // Base line
            g2.drawLine(x + padding, y + size - padding, x + size - padding, y + size - padding);
            
            g2.dispose();
        }
    }
    
    private static class FilterIcon extends BaseIcon {
        public FilterIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            
            // Funnel shape
            g2.drawLine(x + padding, y + padding, x + size - padding, y + padding);
            g2.drawLine(x + padding, y + padding, x + size/2 - 2, y + size/2);
            g2.drawLine(x + size - padding, y + padding, x + size/2 + 2, y + size/2);
            g2.drawLine(x + size/2 - 2, y + size/2, x + size/2 - 2, y + size - padding);
            g2.drawLine(x + size/2 + 2, y + size/2, x + size/2 + 2, y + size - padding);
            
            g2.dispose();
        }
    }
    
    private static class MoreIcon extends BaseIcon {
        public MoreIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            
            int dotSize = 3;
            int centerY = y + size / 2;
            
            g2.fillOval(x + size/4 - dotSize/2, centerY - dotSize/2, dotSize, dotSize);
            g2.fillOval(x + size/2 - dotSize/2, centerY - dotSize/2, dotSize, dotSize);
            g2.fillOval(x + size*3/4 - dotSize/2, centerY - dotSize/2, dotSize, dotSize);
            
            g2.dispose();
        }
    }
    
    private static class BellIcon extends BaseIcon {
        public BellIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 5;
            
            // Bell shape
            g2.drawArc(x + padding, y + padding + 2, size - padding * 2, size - padding * 2 - 4, 0, -180);
            g2.drawLine(x + padding, y + size - padding - 2, x + size - padding, y + size - padding - 2);
            
            // Clapper
            g2.drawLine(x + size/2, y + padding, x + size/2, y + padding + 4);
            
            g2.dispose();
        }
    }
    
    private static class ChevronDownIcon extends BaseIcon {
        public ChevronDownIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 4;
            
            g2.drawLine(x + padding, y + size/3, x + size/2, y + size*2/3);
            g2.drawLine(x + size/2, y + size*2/3, x + size - padding, y + size/3);
            
            g2.dispose();
        }
    }
    
    private static class ChevronRightIcon extends BaseIcon {
        public ChevronRightIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            int padding = size / 4;
            
            g2.drawLine(x + size/3, y + padding, x + size*2/3, y + size/2);
            g2.drawLine(x + size*2/3, y + size/2, x + size/3, y + size - padding);
            
            g2.dispose();
        }
    }
    
    private static class DefaultIcon extends BaseIcon {
        public DefaultIcon(int size) {
            super(size);
        }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x + size/3, y + size/3, size/3, size/3);
            g2.dispose();
        }
    }
}
