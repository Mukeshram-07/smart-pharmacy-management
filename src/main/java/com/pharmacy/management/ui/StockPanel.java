package com.pharmacy.management.ui;

import com.pharmacy.management.model.Medicine;
import com.pharmacy.management.model.Session;
import com.pharmacy.management.model.Stock;
import com.pharmacy.management.model.StockTransaction;
import com.pharmacy.management.service.MedicineService;
import com.pharmacy.management.service.ServiceException;
import com.pharmacy.management.service.StockService;
import com.pharmacy.management.service.ValidationException;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.ui.components.CustomTextField;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Stock Management Panel — Module 3.
 * Displays stock inventory with CRUD operations, stock alerts, and transaction history.
 */
public class StockPanel extends JPanel {

    private final StockService stockService;
    private DefaultTableModel tableModel;
    private JTable stockTable;
    private CustomTextField searchField;
    private JComboBox<String> filterCombo;

    // Dashboard stats labels
    private JLabel totalQtyLabel;
    private JLabel lowStockLabel;
    private JLabel outOfStockLabel;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter DATE_TIME_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private static final String[] COLUMNS = {
        "ID", "Medicine Name", "Batch No.", "Category",
        "Qty", "Min Level", "Expiry Date", "Supplier", "Status"
    };

    public StockPanel() {
        this.stockService = new StockService();
        initializeUI();
        loadStock("ALL");
    }

    // -----------------------------------------------------------------------
    // UI construction
    // -----------------------------------------------------------------------

    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topSection = new JPanel(new BorderLayout(0, 12));
        topSection.setBackground(UIConstants.BACKGROUND_COLOR);
        topSection.add(createHeader(), BorderLayout.NORTH);
        topSection.add(createStatsPanel(), BorderLayout.CENTER);
        topSection.add(createToolbar(), BorderLayout.SOUTH);

        add(topSection, BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createActionsPanel(), BorderLayout.SOUTH);
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);

        JLabel title = new JLabel("📦 Stock Management");
        title.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 28));
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Manage medicine inventory, stock levels, and expiry dates.");
        subtitle.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 14));
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);

        titlePanel.add(title);
        titlePanel.add(subtitle);

        CustomButton addBtn = new CustomButton("+ Add Stock");
        addBtn.setBackgroundColor(UIConstants.PRIMARY_COLOR);
        addBtn.setPreferredSize(new Dimension(130, 40));
        addBtn.addActionListener(e -> showAddStockDialog());

        header.add(titlePanel, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);
        return header;
    }

    private JPanel createStatsPanel() {
        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setBackground(UIConstants.BACKGROUND_COLOR);
        stats.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

        totalQtyLabel = new JLabel("0");
        lowStockLabel = new JLabel("0");
        outOfStockLabel = new JLabel("0");

        stats.add(createStatCard("Total Stock Qty", totalQtyLabel, UIConstants.PRIMARY_COLOR));
        stats.add(createStatCard("Low Stock Items", lowStockLabel, UIConstants.WARNING_COLOR));
        stats.add(createStatCard("Out of Stock", outOfStockLabel, UIConstants.DANGER_COLOR));

        return stats;
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 12));
        titleLbl.setForeground(UIConstants.TEXT_SECONDARY);

        valueLabel.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 28));
        valueLabel.setForeground(accent);

        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        // Search
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setBackground(Color.WHITE);
        JLabel searchLbl = new JLabel("Search: ");
        searchLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));

        searchField = new CustomTextField(25);
        searchField.setPreferredSize(new Dimension(220, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
        });

        searchPanel.add(searchLbl);
        searchPanel.add(searchField);

        // Filter + refresh
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setBackground(Color.WHITE);

        String[] filterItems = {
            "All", "Available", "Low Stock", "Out of Stock",
            "Expired", "Expiring Soon (30 days)", "Expiring in 90 days"
        };
        filterCombo = new JComboBox<>(filterItems);
        filterCombo.setPreferredSize(new Dimension(210, 34));
        filterCombo.addActionListener(e -> loadStock(getSelectedFilter()));

        CustomButton refreshBtn = new CustomButton("↻ Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        refreshBtn.addActionListener(e -> loadStock(getSelectedFilter()));

        filterPanel.add(new JLabel("Filter: "));
        filterPanel.add(filterCombo);
        filterPanel.add(refreshBtn);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(filterPanel, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        stockTable = new JTable(tableModel);
        stockTable.setRowHeight(36);
        stockTable.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        stockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        stockTable.setShowVerticalLines(false);
        stockTable.setGridColor(new Color(245, 245, 245));
        stockTable.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader tableHeader = stockTable.getTableHeader();
        tableHeader.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 13));
        tableHeader.setBackground(new Color(249, 250, 251));
        tableHeader.setForeground(UIConstants.TEXT_PRIMARY);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 40));

        // Column widths
        int[] widths = {40, 200, 120, 100, 60, 80, 100, 150, 110};
        for (int i = 0; i < widths.length && i < COLUMNS.length; i++) {
            stockTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Alternating row renderer for non-status columns
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
                }
                return c;
            }
        };
        for (int i = 0; i < COLUMNS.length - 1; i++) {
            stockTable.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
        }

        // Colour-coded Status column
        stockTable.getColumnModel().getColumn(COLUMNS.length - 1).setCellRenderer(new StatusCellRenderer());

        JScrollPane scrollPane = new JScrollPane(stockTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        scrollPane.getViewport().setBackground(Color.WHITE);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);

        CustomButton increaseBtn = new CustomButton("↑ Increase Stock");
        increaseBtn.setBackgroundColor(UIConstants.SUCCESS_COLOR);
        increaseBtn.setPreferredSize(new Dimension(150, 38));
        increaseBtn.addActionListener(e -> showAdjustDialog("increase"));

        CustomButton decreaseBtn = new CustomButton("↓ Decrease Stock");
        decreaseBtn.setBackgroundColor(UIConstants.WARNING_COLOR);
        decreaseBtn.setPreferredSize(new Dimension(150, 38));
        decreaseBtn.addActionListener(e -> showAdjustDialog("decrease"));

        CustomButton correctBtn = new CustomButton("✎ Correct Stock");
        correctBtn.setBackgroundColor(UIConstants.INFO_COLOR);
        correctBtn.setPreferredSize(new Dimension(140, 38));
        correctBtn.addActionListener(e -> showAdjustDialog("correction"));

        CustomButton historyBtn = new CustomButton("📋 View History");
        historyBtn.setPreferredSize(new Dimension(140, 38));
        historyBtn.addActionListener(e -> showTransactionHistory());

        panel.add(increaseBtn);
        panel.add(decreaseBtn);
        panel.add(correctBtn);
        panel.add(historyBtn);
        return panel;
    }

    // -----------------------------------------------------------------------
    // Data loading
    // -----------------------------------------------------------------------

    private void loadStock(String filter) {
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                List<Stock> list;
                switch (filter) {
                    case "LOW_STOCK":    list = stockService.getLowStock();          break;
                    case "OUT_OF_STOCK": list = stockService.getOutOfStock();        break;
                    case "EXPIRED":      list = stockService.getExpiredStock();      break;
                    case "EXPIRING_30":  list = stockService.getExpiringSoon(30);    break;
                    case "EXPIRING_90":  list = stockService.getExpiringSoon(90);    break;
                    default:             list = stockService.getAllStock();
                }
                int totalQty  = stockService.getTotalStockQuantity();
                int lowCount  = stockService.getLowStockCount();
                int outCount  = stockService.getOutOfStockCount();
                return new Object[]{list, totalQty, lowCount, outCount};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] result = get();
                    List<Stock> stockList = (List<Stock>) result[0];
                    totalQtyLabel.setText(String.valueOf(result[1]));
                    lowStockLabel.setText(String.valueOf(result[2]));
                    outOfStockLabel.setText(String.valueOf(result[3]));

                    tableModel.setRowCount(0);
                    for (Stock s : stockList) {
                        String status = s.isExpired() ? "Expired" : s.getStockStatus().getDisplayName();
                        tableModel.addRow(new Object[]{
                            s.getStockId(),
                            s.getMedicineName(),
                            s.getBatchNumber(),
                            s.getCategory(),
                            s.getQuantity(),
                            s.getMinimumStockLevel(),
                            s.getExpiryDate() != null ? s.getExpiryDate().format(DATE_FMT) : "-",
                            s.getSupplier() != null ? s.getSupplier() : "-",
                            status
                        });
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(StockPanel.this,
                        "Failed to load stock data: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void doSearch() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) {
            loadStock(getSelectedFilter());
            return;
        }
        new SwingWorker<List<Stock>, Void>() {
            @Override protected List<Stock> doInBackground() throws Exception {
                return stockService.searchStock(term);
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Stock s : get()) {
                        String status = s.isExpired() ? "Expired" : s.getStockStatus().getDisplayName();
                        tableModel.addRow(new Object[]{
                            s.getStockId(), s.getMedicineName(), s.getBatchNumber(),
                            s.getCategory(), s.getQuantity(), s.getMinimumStockLevel(),
                            s.getExpiryDate() != null ? s.getExpiryDate().format(DATE_FMT) : "-",
                            s.getSupplier() != null ? s.getSupplier() : "-",
                            status
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    private String getSelectedFilter() {
        String sel = (String) filterCombo.getSelectedItem();
        if (sel == null) return "ALL";
        switch (sel) {
            case "Low Stock":               return "LOW_STOCK";
            case "Out of Stock":            return "OUT_OF_STOCK";
            case "Expired":                 return "EXPIRED";
            case "Expiring Soon (30 days)": return "EXPIRING_30";
            case "Expiring in 90 days":     return "EXPIRING_90";
            default:                        return "ALL";
        }
    }

    // -----------------------------------------------------------------------
    // Dialogs
    // -----------------------------------------------------------------------

    private void showAddStockDialog() {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Add Stock", true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        form.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // --- Medicine dropdown ---
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(bold("Medicine *"), gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        JComboBox<String> medicineCombo = new JComboBox<>();
        medicineCombo.setPreferredSize(new Dimension(250, 34));

        Map<String, Integer> medicineMap = loadActiveMedicines(medicineCombo);
        medicineCombo.putClientProperty("medicineMap", medicineMap);
        form.add(medicineCombo, gbc);

        // --- Text fields ---
        String[] labels  = {"Batch Number *", "Quantity *", "Min Stock Level *",
                             "Expiry Date * (YYYY-MM-DD)", "Supplier", "Purchase Price"};
        String[] defaults = {"", "0", "10", "", "", ""};
        JTextField[] fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 1; gbc.weightx = 0;
            form.add(bold(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            fields[i] = new JTextField(defaults[i]);
            fields[i].setPreferredSize(new Dimension(250, 34));
            form.add(fields[i], gbc);
        }

        // --- Buttons ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Add Stock");
        saveBtn.setBackground(UIConstants.PRIMARY_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Integer> map =
                    (Map<String, Integer>) medicineCombo.getClientProperty("medicineMap");
                String medName = (String) medicineCombo.getSelectedItem();
                if (medName == null || map == null || !map.containsKey(medName))
                    throw new ValidationException("Please select a valid medicine");

                int medicineId = map.get(medName);
                String batch   = fields[0].getText().trim();
                int qty        = Integer.parseInt(fields[1].getText().trim());
                int minLevel   = Integer.parseInt(fields[2].getText().trim());
                LocalDate expiry = LocalDate.parse(fields[3].getText().trim());
                String supplier  = fields[4].getText().trim();
                String priceStr  = fields[5].getText().trim();
                BigDecimal price = priceStr.isEmpty() ? null : new BigDecimal(priceStr);

                Stock stock = new Stock(medicineId, batch, qty, minLevel, expiry,
                                        supplier.isEmpty() ? null : supplier, price);
                stockService.addStock(stock);

                JOptionPane.showMessageDialog(dialog,
                    "Stock added successfully! ✅", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadStock("ALL");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Invalid number: " + ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Invalid date format. Use YYYY-MM-DD (e.g. 2025-12-31).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(dialog,
                    ex.getMessage(), "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(dialog,
                    ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);

        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showAdjustDialog(String type) {
        int row = stockTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a stock row first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int stockId        = (int) tableModel.getValueAt(row, 0);
        String medicineName = (String) tableModel.getValueAt(row, 1);
        int currentQty     = (int) tableModel.getValueAt(row, 4);

        String title = switch (type) {
            case "increase"   -> "Increase Stock";
            case "decrease"   -> "Decrease Stock";
            default           -> "Correct Stock Quantity";
        };

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        panel.add(new JLabel("Medicine:"));
        panel.add(new JLabel(medicineName));
        panel.add(new JLabel("Current Quantity:"));
        panel.add(new JLabel(String.valueOf(currentQty)));
        panel.add(new JLabel(type.equals("correction") ? "New Quantity:" : "Amount:"));
        JTextField amountField = new JTextField();
        panel.add(amountField);
        panel.add(new JLabel("Notes:"));
        JTextField notesField = new JTextField();
        panel.add(notesField);

        int result = JOptionPane.showConfirmDialog(
            this, panel, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int amount = Integer.parseInt(amountField.getText().trim());
                String notes = notesField.getText().trim();
                String user = Session.getInstance().getUsername();

                switch (type) {
                    case "increase":
                        stockService.increaseStock(stockId, amount, user, notes);
                        break;
                    case "decrease":
                        stockService.decreaseStock(stockId, amount, user, notes);
                        break;
                    default: // correction
                        if (amount < 0)
                            throw new ServiceException("New quantity cannot be negative");
                        Stock stock = stockService.getStockById(stockId);
                        if (stock == null)
                            throw new ServiceException("Stock record not found");
                        stock.setQuantity(amount);
                        stockService.updateStock(stock, user, "correction: " + notes);
                }

                JOptionPane.showMessageDialog(this,
                    "Stock updated successfully! ✅", "Success", JOptionPane.INFORMATION_MESSAGE);
                loadStock(getSelectedFilter());

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid integer.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (ServiceException | ValidationException ex) {
                JOptionPane.showMessageDialog(this,
                    ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showTransactionHistory() {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "Stock Transaction History", false);
        dialog.setSize(960, 500);
        dialog.setLocationRelativeTo(this);

        String[] cols = {
            "ID", "Medicine", "Type", "Qty Changed",
            "Prev Qty", "New Qty", "Date / Time", "Performed By", "Notes"
        };
        DefaultTableModel histModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable histTable = new JTable(histModel);
        histTable.setRowHeight(32);
        histTable.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 12));
        histTable.getTableHeader().setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 12));

        new SwingWorker<List<StockTransaction>, Void>() {
            @Override protected List<StockTransaction> doInBackground() throws Exception {
                return stockService.getTransactionHistory();
            }
            @Override protected void done() {
                try {
                    for (StockTransaction t : get()) {
                        histModel.addRow(new Object[]{
                            t.getTransactionId(),
                            t.getMedicineName(),
                            t.getTransactionType(),
                            t.getQuantityChanged(),
                            t.getPreviousQuantity(),
                            t.getNewQuantity(),
                            t.getTransactionDate() != null
                                ? t.getTransactionDate().format(DATE_TIME_FMT) : "-",
                            t.getPerformedBy(),
                            t.getNotes() != null ? t.getNotes() : ""
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();

        JScrollPane sp = new JScrollPane(histTable);
        sp.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(sp);
        dialog.setVisible(true);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Load active medicines into the combo and return a name→id map. */
    private Map<String, Integer> loadActiveMedicines(JComboBox<String> combo) {
        Map<String, Integer> map = new LinkedHashMap<>();
        try {
            MedicineService ms = new MedicineService();
            List<Medicine> meds = ms.getAllMedicines();
            for (Medicine m : meds) {
                if ("ACTIVE".equals(m.getStatus())) {
                    combo.addItem(m.getMedicineName());
                    map.put(m.getMedicineName(), m.getMedicineId());
                }
            }
        } catch (ServiceException ignored) {}
        return map;
    }

    private JLabel bold(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        return lbl;
    }

    // -----------------------------------------------------------------------
    // Status cell renderer
    // -----------------------------------------------------------------------

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean selected, boolean focused, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                table, value, selected, focused, row, col);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);

            if (!selected) {
                String status = value != null ? value.toString() : "";
                switch (status) {
                    case "Available":
                        label.setBackground(new Color(209, 250, 229));
                        label.setForeground(new Color(5, 150, 105));
                        break;
                    case "Low Stock":
                        label.setBackground(new Color(254, 243, 199));
                        label.setForeground(new Color(180, 83, 9));
                        break;
                    case "Out of Stock":
                        label.setBackground(new Color(254, 226, 226));
                        label.setForeground(new Color(185, 28, 28));
                        break;
                    case "Expired":
                        label.setBackground(new Color(243, 232, 255));
                        label.setForeground(new Color(109, 40, 217));
                        break;
                    default:
                        label.setBackground(Color.WHITE);
                        label.setForeground(UIConstants.TEXT_PRIMARY);
                }
            }
            return label;
        }
    }
}
