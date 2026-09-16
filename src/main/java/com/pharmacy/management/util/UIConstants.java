package com.pharmacy.management.util;

import java.awt.Color;
import java.awt.Font;

/**
 * UI Constants for consistent professional design system.
 * Premium Healthcare SaaS Design Language
 * Validates Requirement 9.1: Modern UI rendering
 */
public class UIConstants {
    
    // Primary Brand Colors - Medical Professional Palette
    public static final Color PRIMARY_COLOR = new Color(30, 64, 175);       // Deep Medical Blue #1e40af
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);        // Darker Blue #1d4ed8
    public static final Color PRIMARY_LIGHT = new Color(59, 130, 246);      // Lighter Blue #3b82f6
    public static final Color SECONDARY_COLOR = new Color(5, 150, 105);     // Healthcare Green #059669
    public static final Color ACCENT_COLOR = new Color(16, 185, 129);       // Teal Accent #10b981
    
    // Status Colors
    public static final Color SUCCESS_COLOR = new Color(16, 185, 129);      // Green #10b981
    public static final Color DANGER_COLOR = new Color(239, 68, 68);        // Red #ef4444
    public static final Color WARNING_COLOR = new Color(245, 158, 11);      // Amber #f59e0b
    public static final Color INFO_COLOR = new Color(59, 130, 246);         // Blue #3b82f6
    
    // Background Colors - Cool Gray Scale
    public static final Color BACKGROUND_COLOR = new Color(248, 250, 252);  // Very Light Cool Gray #f8fafc
    public static final Color BACKGROUND_SECONDARY = new Color(241, 245, 249); // Slightly darker #f1f5f9
    public static final Color CARD_BACKGROUND = Color.WHITE;                // Pure White #ffffff
    public static final Color CARD_HOVER = new Color(249, 250, 251);        // Subtle hover #f9fafb
    
    // Sidebar Colors - Professional Dark
    public static final Color SIDEBAR_COLOR = new Color(30, 41, 59);        // Dark Slate #1e293b
    public static final Color SIDEBAR_HOVER = new Color(51, 65, 85);        // Lighter Slate #334155
    public static final Color SIDEBAR_ACTIVE = new Color(51, 65, 85);       // Active State #334155
    public static final Color SIDEBAR_BORDER = new Color(51, 65, 85);       // Border #334155
    
    // Text Colors - Professional Hierarchy
    public static final Color TEXT_PRIMARY = new Color(17, 24, 39);         // Almost Black #111827
    public static final Color TEXT_SECONDARY = new Color(107, 114, 128);    // Slate Gray #6b7280
    public static final Color TEXT_TERTIARY = new Color(156, 163, 175);     // Light Gray #9ca3af
    public static final Color TEXT_ON_DARK = Color.WHITE;                   // White on Dark
    public static final Color TEXT_ON_PRIMARY = Color.WHITE;                // White on Primary
    
    // Border & Divider Colors
    public static final Color BORDER_COLOR = new Color(229, 231, 235);      // Light Gray #e5e7eb
    public static final Color BORDER_LIGHT = new Color(243, 244, 246);      // Very Light #f3f4f6
    public static final Color DIVIDER_COLOR = new Color(229, 231, 235);     // Same as border #e5e7eb
    
    // Status Badge Colors
    public static final Color STATUS_ACTIVE = new Color(16, 185, 129);      // Green #10b981
    public static final Color STATUS_ACTIVE_BG = new Color(209, 250, 229);  // Light Green #d1fae5
    public static final Color STATUS_INACTIVE = new Color(107, 114, 128);   // Gray #6b7280
    public static final Color STATUS_INACTIVE_BG = new Color(243, 244, 246);// Light Gray #f3f4f6
    public static final Color STATUS_WARNING_BG = new Color(254, 243, 199); // Light Amber #fef3c7
    public static final Color STATUS_DANGER_BG = new Color(254, 226, 226);  // Light Red #fee2e2
    
    // Shadow Colors (with alpha for layering)
    public static final Color SHADOW_LIGHT = new Color(0, 0, 0, 10);        // 4% opacity
    public static final Color SHADOW_MEDIUM = new Color(0, 0, 0, 20);       // 8% opacity
    public static final Color SHADOW_STRONG = new Color(0, 0, 0, 30);       // 12% opacity
    
    // Interactive State Colors
    public static final Color HOVER_OVERLAY = new Color(0, 0, 0, 5);        // Subtle hover darkening
    public static final Color SELECTED_OVERLAY = new Color(30, 64, 175, 10); // Primary tint
    public static final Color FOCUS_RING = new Color(59, 130, 246);         // Blue focus ring
    
    // Typography - Professional Font Stack
    public static final String FONT_FAMILY = "Segoe UI";                    // Primary font
    public static final String FONT_FAMILY_FALLBACK = "Inter";              // Fallback
    
    // Font Sizes
    public static final int FONT_SIZE_LARGE = 32;                           // Page titles
    public static final int FONT_SIZE_TITLE = 24;                           // Section titles
    public static final int FONT_SIZE_HEADING = 18;                         // Headings
    public static final int FONT_SIZE_BODY = 14;                            // Body text
    public static final int FONT_SIZE_SMALL = 13;                           // Small text
    public static final int FONT_SIZE_TINY = 12;                            // Captions
    
    // Font Weights (via Font styles)
    public static final int FONT_WEIGHT_REGULAR = Font.PLAIN;
    public static final int FONT_WEIGHT_MEDIUM = Font.PLAIN;                // Segoe UI doesn't have medium
    public static final int FONT_WEIGHT_BOLD = Font.BOLD;
    
    // Spacing System (8px base unit)
    public static final int SPACING_XS = 4;                                 // Extra small
    public static final int SPACING_SM = 8;                                 // Small
    public static final int SPACING_MD = 16;                                // Medium
    public static final int SPACING_LG = 24;                                // Large
    public static final int SPACING_XL = 32;                                // Extra large
    public static final int SPACING_2XL = 48;                               // 2X large
    
    // Border Radius
    public static final int RADIUS_NONE = 0;
    public static final int RADIUS_SM = 4;                                  // Small radius
    public static final int RADIUS_MD = 8;                                  // Medium radius
    public static final int RADIUS_LG = 12;                                 // Large radius
    public static final int RADIUS_XL = 16;                                 // Extra large radius
    public static final int RADIUS_FULL = 9999;                             // Pill shape
    
    // Component Heights
    public static final int HEIGHT_INPUT = 40;                              // Input fields
    public static final int HEIGHT_BUTTON = 40;                             // Buttons
    public static final int HEIGHT_BUTTON_SM = 32;                          // Small buttons
    public static final int HEIGHT_HEADER = 64;                             // App header
    public static final int HEIGHT_TABLE_ROW = 48;                          // Table rows
    
    // Component Widths
    public static final int WIDTH_SIDEBAR = 240;                            // Sidebar
    public static final int WIDTH_SIDEBAR_COLLAPSED = 64;                   // Collapsed sidebar
    
    // Z-Index Layers
    public static final int Z_INDEX_BASE = 0;
    public static final int Z_INDEX_DROPDOWN = 100;
    public static final int Z_INDEX_MODAL = 200;
    public static final int Z_INDEX_TOAST = 300;
    public static final int Z_INDEX_TOOLTIP = 400;
    
    // Animation Durations (milliseconds)
    public static final int DURATION_FAST = 150;                            // Fast transitions
    public static final int DURATION_NORMAL = 300;                          // Normal transitions
    public static final int DURATION_SLOW = 500;                            // Slow transitions
    
    private UIConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
