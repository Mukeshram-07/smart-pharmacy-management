package com.pharmacy.management.ui;

import com.pharmacy.management.service.ServiceException;
import com.pharmacy.management.service.StockService;
import com.pharmacy.management.service.SupplierService;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Smart Dashboard — Module 5.
 * Real-time pharmacy statistics, alerts, and sales summary.
 * All data is loaded from MySQL via service layer (no hardcoded values).
 */
public class DashboardPanel extends JPanel {

    private final StockService    stockService    = new StockService();
    private final SupplierService supplierService = new SupplierService();

    // Stat labels (updated by SwingWorker)
    private JLabel lblTotalMedicines, lblTotalStock, lblLowStock, lblOutOfStock;
    private JLabel lblExpiringSoon, lblExpired, lblTotalSuppliers;
    private JLabel lblTodaySales, lblTodayBills, lblTodayPurchase;
    private JLabel lblWeekSales, lblMonthSales;

    private JPanel alertsPanel;

    public DashboardPanel() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(createHeader(), BorderLayout.NORTH);

        // Main scroll content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UIConstants.BACKGROUND_COLOR);
        content.add(Box.createVerticalStrut(16));
        content.add(createStatsRow());
        content.add(Box.createVerticalStrut(20));
        content.add(createSalesRow());
        content.add(Box.createVerticalStrut(20));
        content.add(createAlertsSection());
        content.add(Box.createVerticalStrut(20));

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UIConstants.BACKGROUND_COLOR);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        loadData();
    }

    // =========================================================================
    // Header
    // =========================================================================

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel titles = new JPanel();
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel title = new JLabel("📊 Dashboard");
        title.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 32));
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Real-time pharmacy overview — all data from database.");
        subtitle.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 14));
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);

        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        CustomButton refreshBtn = new CustomButton("↻ Refresh");
        refreshBtn.setPreferredSize(new Dimension(120, 38));
        refreshBtn.addActionListener(e -> loadData());

        header.add(titles, BorderLayout.WEST);
        header.add(refreshBtn, BorderLayout.EAST);
        return header;
    }

    // =========================================================================
    // Stats cards row
    // =========================================================================

    private JPanel createStatsRow() {
        JPanel section = buildSection("📦 Inventory & Suppliers");

        JPanel cards = new JPanel(new GridLayout(2, 5, 14, 14));
        cards.setBackground(UIConstants.BACKGROUND_COLOR);
        cards.setAlignmentX(LEFT_ALIGNMENT);

        lblTotalMedicines  = addCard(cards, "Total Medicines",  "–", UIConstants.PRIMARY_COLOR);
        lblTotalStock      = addCard(cards, "Total Stock Qty",  "–", UIConstants.SECONDARY_COLOR);
        lblLowStock        = addCard(cards, "Low Stock",        "–", UIConstants.WARNING_COLOR);
        lblOutOfStock      = addCard(cards, "Out of Stock",     "–", UIConstants.DANGER_COLOR);
        lblTotalSuppliers  = addCard(cards, "Total Suppliers",  "–", new Color(139, 92, 246));
        lblExpiringSoon    = addCard(cards, "Expiring (30d)",   "–", new Color(245, 158, 11));
        lblExpired         = addCard(cards, "Expired",          "–", new Color(220, 38, 38));

        // Placeholders to keep grid balanced
        cards.add(new JPanel() {{ setOpaque(false); }});
        cards.add(new JPanel() {{ setOpaque(false); }});
        cards.add(new JPanel() {{ setOpaque(false); }});

        section.add(cards);
        return section;
    }

    // =========================================================================
    // Sales summary row
    // =========================================================================

    private JPanel createSalesRow() {
        JPanel section = buildSection("💰 Sales Summary");

        JPanel cards = new JPanel(new GridLayout(1, 5, 14, 14));
        cards.setBackground(UIConstants.BACKGROUND_COLOR);
        cards.setAlignmentX(LEFT_ALIGNMENT);

        lblTodaySales    = addCard(cards, "Today's Sales",     "–", UIConstants.SUCCESS_COLOR);
        lblTodayBills    = addCard(cards, "Today's Bills",     "–", UIConstants.PRIMARY_COLOR);
        lblTodayPurchase = addCard(cards, "Today's Purchases", "–", new Color(16, 185, 129));
        lblWeekSales     = addCard(cards, "This Week",         "–", UIConstants.INFO_COLOR);
        lblMonthSales    = addCard(cards, "This Month",        "–", new Color(139, 92, 246));

        section.add(cards);
        return section;
    }

    // =========================================================================
    // Alerts section
    // =========================================================================

    private JPanel createAlertsSection() {
        JPanel section = buildSection("🔔 Alerts");
        alertsPanel = new JPanel();
        alertsPanel.setLayout(new BoxLayout(alertsPanel, BoxLayout.Y_AXIS));
        alertsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        alertsPanel.setAlignmentX(LEFT_ALIGNMENT);
        section.add(alertsPanel);
        return section;
    }

    private JPanel buildSection(String title) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(UIConstants.BACKGROUND_COLOR);
        section.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 18));
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        section.add(lbl);
        return section;
    }

    // =========================================================================
    // Card builder
    // =========================================================================

    private JLabel addCard(JPanel parent, String title, String initVal, Color accent) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 40));
                g2.setStroke(new BasicStroke(2));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 14, 14));
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        card.setPreferredSize(new Dimension(160, 90));

        JLabel valLbl = new JLabel(initVal);
        valLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 28));
        valLbl.setForeground(accent);

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 12));
        titleLbl.setForeground(UIConstants.TEXT_SECONDARY);

        card.add(valLbl, BorderLayout.CENTER);
        card.add(titleLbl, BorderLayout.SOUTH);
        parent.add(card);
        return valLbl;
    }

    // =========================================================================
    // Data loading (SwingWorker — never blocks EDT)
    // =========================================================================

    private void loadData() {
        // Reset to loading indicator
        lblTotalMedicines.setText("…");
        lblTotalStock.setText("…");
        lblLowStock.setText("…");
        lblOutOfStock.setText("…");
        lblExpiringSoon.setText("…");
        lblExpired.setText("…");
        lblTotalSuppliers.setText("…");
        lblTodaySales.setText("…");
        lblTodayBills.setText("…");
        lblTodayPurchase.setText("…");
        lblWeekSales.setText("…");
        lblMonthSales.setText("…");

        new SwingWorker<DashboardData, Void>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardData d = new DashboardData();
                try { d.totalMedicines  = supplierService.getTotalMedicinesCount(); } catch (ServiceException ignored) {}
                try { d.totalStock      = stockService.getTotalStockQuantity();     } catch (ServiceException ignored) {}
                try { d.lowStock        = stockService.getLowStockCount();          } catch (ServiceException ignored) {}
                try { d.outOfStock      = stockService.getOutOfStockCount();        } catch (ServiceException ignored) {}
                try { d.expiringSoon    = supplierService.getExpiringSoonCount(30); } catch (ServiceException ignored) {}
                try { d.expired         = supplierService.getExpiredCount();        } catch (ServiceException ignored) {}
                try { d.totalSuppliers  = supplierService.getSupplierCount();       } catch (ServiceException ignored) {}
                try { d.todaySales      = supplierService.getTodaySalesAmount();    } catch (ServiceException ignored) {}
                try { d.todayBills      = supplierService.getTodayBillCount();      } catch (ServiceException ignored) {}
                try { d.todayPurchase   = supplierService.getTodayPurchaseAmount(); } catch (ServiceException ignored) {}
                try { d.weekSales       = supplierService.getWeekSalesAmount();     } catch (ServiceException ignored) {}
                try { d.monthSales      = supplierService.getMonthSalesAmount();    } catch (ServiceException ignored) {}

                // Alerts
                try { d.lowStockItems   = stockService.getLowStock();               } catch (ServiceException ignored) {}
                try { d.outOfStockItems = stockService.getOutOfStock();             } catch (ServiceException ignored) {}
                try { d.expiringSoonItems = stockService.getExpiringSoon(30);       } catch (ServiceException ignored) {}
                try { d.expiredItems    = stockService.getExpiredStock();            } catch (ServiceException ignored) {}
                return d;
            }

            @Override
            protected void done() {
                try {
                    DashboardData d = get();
                    lblTotalMedicines.setText(String.valueOf(d.totalMedicines));
                    lblTotalStock.setText(String.valueOf(d.totalStock));
                    lblLowStock.setText(String.valueOf(d.lowStock));
                    lblOutOfStock.setText(String.valueOf(d.outOfStock));
                    lblExpiringSoon.setText(String.valueOf(d.expiringSoon));
                    lblExpired.setText(String.valueOf(d.expired));
                    lblTotalSuppliers.setText(String.valueOf(d.totalSuppliers));
                    lblTodaySales.setText("₹" + String.format("%.0f", d.todaySales));
                    lblTodayBills.setText(String.valueOf(d.todayBills));
                    lblTodayPurchase.setText("₹" + String.format("%.0f", d.todayPurchase));
                    lblWeekSales.setText("₹" + String.format("%.0f", d.weekSales));
                    lblMonthSales.setText("₹" + String.format("%.0f", d.monthSales));
                    buildAlerts(d);
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void buildAlerts(DashboardData d) {
        alertsPanel.removeAll();

        if (d.outOfStockItems != null) {
            for (var s : d.outOfStockItems) {
                alertsPanel.add(alertRow("🔴 OUT OF STOCK",
                    s.getMedicineName() + " (Batch: " + s.getBatchNumber() + ") is out of stock.",
                    new Color(254, 226, 226), new Color(185, 28, 28)));
            }
        }
        if (d.lowStockItems != null) {
            for (var s : d.lowStockItems) {
                alertsPanel.add(alertRow("🟡 LOW STOCK",
                    s.getMedicineName() + " is below minimum level. Qty: " + s.getQuantity()
                        + " / Min: " + s.getMinimumStockLevel(),
                    new Color(254, 243, 199), new Color(180, 83, 9)));
            }
        }
        if (d.expiringSoonItems != null) {
            for (var s : d.expiringSoonItems) {
                alertsPanel.add(alertRow("🟠 EXPIRING SOON",
                    s.getMedicineName() + " (Batch: " + s.getBatchNumber() + ") expires on "
                        + s.getExpiryDate() + ".",
                    new Color(255, 237, 213), new Color(194, 65, 12)));
            }
        }
        if (d.expiredItems != null) {
            for (var s : d.expiredItems) {
                alertsPanel.add(alertRow("⛔ EXPIRED",
                    s.getMedicineName() + " (Batch: " + s.getBatchNumber() + ") expired on "
                        + s.getExpiryDate() + ". Do not dispense.",
                    new Color(243, 232, 255), new Color(109, 40, 217)));
            }
        }
        if (alertsPanel.getComponentCount() == 0) {
            JLabel ok = new JLabel("  ✅ No alerts at this time. Inventory looks good!");
            ok.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 14));
            ok.setForeground(UIConstants.SECONDARY_COLOR);
            alertsPanel.add(ok);
        }
        alertsPanel.revalidate();
        alertsPanel.repaint();
    }

    private JPanel alertRow(String badge, String msg, Color bg, Color fg) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        row.setBackground(bg);
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(fg.getRed(), fg.getGreen(), fg.getBlue(), 80)),
            BorderFactory.createEmptyBorder(2, 6, 2, 6)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel badgeLbl = new JLabel(badge);
        badgeLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 12));
        badgeLbl.setForeground(fg);

        JLabel msgLbl = new JLabel(msg);
        msgLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        msgLbl.setForeground(UIConstants.TEXT_PRIMARY);

        row.add(badgeLbl);
        row.add(msgLbl);
        return row;
    }

    // =========================================================================
    // Data holder
    // =========================================================================

    private static class DashboardData {
        int totalMedicines, totalStock, lowStock, outOfStock;
        int expiringSoon, expired, totalSuppliers;
        double todaySales, todayPurchase, weekSales, monthSales;
        int todayBills;
        java.util.List<com.pharmacy.management.model.Stock> lowStockItems;
        java.util.List<com.pharmacy.management.model.Stock> outOfStockItems;
        java.util.List<com.pharmacy.management.model.Stock> expiringSoonItems;
        java.util.List<com.pharmacy.management.model.Stock> expiredItems;
    }
}
