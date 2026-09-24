package com.pharmacy.management.ui;

import com.pharmacy.management.model.MedicinePurchase;
import com.pharmacy.management.service.ServiceException;
import com.pharmacy.management.service.SupplierService;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.ui.components.CustomTextField;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

/**
 * Purchase History Panel — Module 5.
 * Shows all medicine purchases with search/filter capability.
 */
public class PurchaseHistoryPanel extends JPanel {

    private final SupplierService supplierService = new SupplierService();

    private DefaultTableModel tableModel;
    private JTable historyTable;
    private CustomTextField searchField;
    private JLabel totalAmountLabel;

    private static final String[] COLS = {
        "ID", "Date", "Supplier", "Company", "Medicine",
        "Batch", "Qty", "Price ₹", "Total ₹", "Expiry"
    };

    public PurchaseHistoryPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        loadHistory();
    }

    // =========================================================================
    // UI
    // =========================================================================

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        JLabel title = new JLabel("📋 Purchase History");
        title.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 28));
        title.setForeground(UIConstants.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("All medicine purchases from suppliers.");
        subtitle.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 14));
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        // Toolbar
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setBackground(Color.WHITE);
        JLabel lbl = new JLabel("Search (supplier / medicine / batch): ");
        lbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        searchField = new CustomTextField(30);
        searchField.setPreferredSize(new Dimension(280, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
        });
        searchPanel.add(lbl);
        searchPanel.add(searchField);

        CustomButton refreshBtn = new CustomButton("↻ Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        refreshBtn.addActionListener(e -> loadHistory());
        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(refreshBtn, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setBackground(UIConstants.BACKGROUND_COLOR);
        top.add(titlePanel, BorderLayout.NORTH);
        top.add(toolbar, BorderLayout.SOUTH);
        panel.add(top, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(36);
        historyTable.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.setShowVerticalLines(false);
        historyTable.setGridColor(new Color(245, 245, 245));

        JTableHeader header = historyTable.getTableHeader();
        header.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 13));
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 40));

        int[] widths = {40, 110, 150, 170, 200, 110, 50, 80, 90, 100};
        for (int i = 0; i < widths.length && i < COLS.length; i++)
            historyTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Right-align numeric columns
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        historyTable.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);
        historyTable.getColumnModel().getColumn(7).setCellRenderer(rightRenderer);
        historyTable.getColumnModel().getColumn(8).setCellRenderer(rightRenderer);

        // Alternating row renderer
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
                return c;
            }
        };
        for (int i = 0; i < COLS.length; i++)
            historyTable.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);

        JScrollPane sp = new JScrollPane(historyTable);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        sp.getViewport().setBackground(Color.WHITE);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFooter() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        JLabel lbl = new JLabel("Total Purchase Amount: ");
        lbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 14));
        lbl.setForeground(UIConstants.TEXT_PRIMARY);
        totalAmountLabel = new JLabel("₹ 0.00");
        totalAmountLabel.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 16));
        totalAmountLabel.setForeground(UIConstants.PRIMARY_COLOR);
        panel.add(lbl);
        panel.add(totalAmountLabel);
        return panel;
    }

    // =========================================================================
    // Data loading
    // =========================================================================

    private void loadHistory() {
        new SwingWorker<List<MedicinePurchase>, Void>() {
            @Override protected List<MedicinePurchase> doInBackground() throws Exception {
                return supplierService.getAllPurchases();
            }
            @Override protected void done() {
                try {
                    populateTable(get());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PurchaseHistoryPanel.this,
                        "Failed to load purchase history: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doSearch() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) { loadHistory(); return; }
        new SwingWorker<List<MedicinePurchase>, Void>() {
            @Override protected List<MedicinePurchase> doInBackground() throws Exception {
                return supplierService.searchPurchases(term);
            }
            @Override protected void done() {
                try { populateTable(get()); } catch (Exception ignored) {}
            }
        }.execute();
    }

    private void populateTable(List<MedicinePurchase> list) {
        tableModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (MedicinePurchase p : list) {
            tableModel.addRow(new Object[]{
                p.getPurchaseId(),
                p.getPurchaseDate() != null ? p.getPurchaseDate().toLocalDate().toString() : "-",
                p.getSupplierName() != null ? p.getSupplierName() : "-",
                p.getCompanyName()  != null ? p.getCompanyName()  : "-",
                p.getMedicineName() != null ? p.getMedicineName() : "-",
                p.getBatchNumber(),
                p.getQuantity(),
                p.getPurchasePrice() != null ? p.getPurchasePrice() : BigDecimal.ZERO,
                p.getTotalAmount(),
                p.getExpiryDate() != null ? p.getExpiryDate().toString() : "-"
            });
            total = total.add(p.getTotalAmount());
        }
        totalAmountLabel.setText("₹ " + String.format("%.2f", total));
    }
}
