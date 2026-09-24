package com.pharmacy.management.ui;

import com.pharmacy.management.model.MedicinePurchase;
import com.pharmacy.management.model.Medicine;
import com.pharmacy.management.model.Session;
import com.pharmacy.management.model.Supplier;
import com.pharmacy.management.service.MedicineService;
import com.pharmacy.management.service.ServiceException;
import com.pharmacy.management.service.SupplierService;
import com.pharmacy.management.service.ValidationException;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.ui.components.CustomTextField;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Supplier Management Panel — Module 5.
 * Add / View / Search / Edit / Deactivate suppliers and view purchase history.
 */
public class SupplierPanel extends JPanel {

    private final SupplierService supplierService = new SupplierService();
    private final MedicineService medicineService = new MedicineService();

    private DefaultTableModel tableModel;
    private JTable supplierTable;
    private CustomTextField searchField;

    private static final String[] COLS = {
        "ID", "Supplier Name", "Company Name", "Phone", "Email", "GST Number", "Status", "Created"
    };

    public SupplierPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(createHeader(), BorderLayout.NORTH);
        add(createTablePanel(), BorderLayout.CENTER);
        add(createActionsPanel(), BorderLayout.SOUTH);

        loadSuppliers();
    }

    // =========================================================================
    // UI construction
    // =========================================================================

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UIConstants.BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));

        // Title
        JPanel titlePanel = new JPanel(new GridLayout(2, 1));
        titlePanel.setBackground(UIConstants.BACKGROUND_COLOR);
        JLabel title = new JLabel("🏭 Supplier Management");
        title.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 28));
        title.setForeground(UIConstants.TEXT_PRIMARY);
        JLabel subtitle = new JLabel("Manage suppliers and medicine purchase history.");
        subtitle.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 14));
        subtitle.setForeground(UIConstants.TEXT_SECONDARY);
        titlePanel.add(title);
        titlePanel.add(subtitle);

        // Add supplier button
        CustomButton addBtn = new CustomButton("+ Add Supplier");
        addBtn.setBackgroundColor(UIConstants.PRIMARY_COLOR);
        addBtn.setPreferredSize(new Dimension(150, 40));
        addBtn.addActionListener(e -> showSupplierDialog(null));

        // Toolbar (search + add)
        JPanel toolbar = new JPanel(new BorderLayout(8, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.setBackground(Color.WHITE);
        JLabel lbl = new JLabel("Search: ");
        lbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        searchField = new CustomTextField(25);
        searchField.setPreferredSize(new Dimension(260, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
        });
        searchPanel.add(lbl);
        searchPanel.add(searchField);

        CustomButton refreshBtn = new CustomButton("↻ Refresh");
        refreshBtn.setPreferredSize(new Dimension(100, 34));
        refreshBtn.addActionListener(e -> loadSuppliers());

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(refreshBtn, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setBackground(UIConstants.BACKGROUND_COLOR);

        JPanel row1 = new JPanel(new BorderLayout());
        row1.setBackground(UIConstants.BACKGROUND_COLOR);
        row1.add(titlePanel, BorderLayout.WEST);
        row1.add(addBtn, BorderLayout.EAST);

        top.add(row1, BorderLayout.NORTH);
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

        supplierTable = new JTable(tableModel);
        supplierTable.setRowHeight(36);
        supplierTable.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        supplierTable.setShowVerticalLines(false);
        supplierTable.setGridColor(new Color(245, 245, 245));
        supplierTable.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = supplierTable.getTableHeader();
        header.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 13));
        header.setBackground(new Color(249, 250, 251));
        header.setForeground(UIConstants.TEXT_PRIMARY);
        header.setPreferredSize(new Dimension(0, 40));

        int[] widths = {40, 180, 200, 120, 200, 130, 90, 130};
        for (int i = 0; i < widths.length && i < COLS.length; i++)
            supplierTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        // Alternating row renderer
        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (!sel) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 251, 252));
                return c;
            }
        };
        for (int i = 0; i < COLS.length - 1; i++)
            supplierTable.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);

        // Status column renderer
        supplierTable.getColumnModel().getColumn(COLS.length - 2).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(
                        JTable t, Object v, boolean sel, boolean foc, int row, int col) {
                    JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                    lbl.setHorizontalAlignment(SwingConstants.CENTER);
                    if (!sel) {
                        if ("ACTIVE".equals(v)) {
                            lbl.setBackground(UIConstants.STATUS_ACTIVE_BG);
                            lbl.setForeground(UIConstants.STATUS_ACTIVE);
                        } else {
                            lbl.setBackground(UIConstants.STATUS_INACTIVE_BG);
                            lbl.setForeground(UIConstants.STATUS_INACTIVE);
                        }
                    }
                    return lbl;
                }
            });

        JScrollPane sp = new JScrollPane(supplierTable);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        sp.getViewport().setBackground(Color.WHITE);
        panel.add(sp, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        panel.setBackground(UIConstants.BACKGROUND_COLOR);

        CustomButton editBtn = new CustomButton("✎ Edit");
        editBtn.setPreferredSize(new Dimension(110, 38));
        editBtn.addActionListener(e -> editSelected());

        CustomButton deactivateBtn = new CustomButton("✕ Deactivate");
        deactivateBtn.setBackgroundColor(UIConstants.DANGER_COLOR);
        deactivateBtn.setPreferredSize(new Dimension(130, 38));
        deactivateBtn.addActionListener(e -> deactivateSelected());

        CustomButton purchaseBtn = new CustomButton("🛒 New Purchase");
        purchaseBtn.setBackgroundColor(UIConstants.SUCCESS_COLOR);
        purchaseBtn.setPreferredSize(new Dimension(150, 38));
        purchaseBtn.addActionListener(e -> showPurchaseDialog());

        CustomButton historyBtn = new CustomButton("📋 Purchase History");
        historyBtn.setBackgroundColor(UIConstants.INFO_COLOR);
        historyBtn.setPreferredSize(new Dimension(160, 38));
        historyBtn.addActionListener(e -> showSupplierHistory());

        panel.add(editBtn);
        panel.add(deactivateBtn);
        panel.add(purchaseBtn);
        panel.add(historyBtn);
        return panel;
    }

    // =========================================================================
    // Data loading
    // =========================================================================

    private void loadSuppliers() {
        new SwingWorker<List<Supplier>, Void>() {
            @Override protected List<Supplier> doInBackground() throws Exception {
                return supplierService.getAllSuppliers();
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Supplier s : get()) {
                        tableModel.addRow(new Object[]{
                            s.getSupplierId(), s.getSupplierName(), s.getCompanyName(),
                            s.getPhone(), s.getEmail() != null ? s.getEmail() : "-",
                            s.getGstNumber() != null ? s.getGstNumber() : "-",
                            s.getStatus(),
                            s.getCreatedAt() != null
                                ? s.getCreatedAt().toLocalDate().toString() : "-"
                        });
                    }
                } catch (Exception ex) {
                    showError("Failed to load suppliers: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void doSearch() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) { loadSuppliers(); return; }
        new SwingWorker<List<Supplier>, Void>() {
            @Override protected List<Supplier> doInBackground() throws Exception {
                return supplierService.searchSuppliers(term);
            }
            @Override protected void done() {
                try {
                    tableModel.setRowCount(0);
                    for (Supplier s : get()) {
                        tableModel.addRow(new Object[]{
                            s.getSupplierId(), s.getSupplierName(), s.getCompanyName(),
                            s.getPhone(), s.getEmail() != null ? s.getEmail() : "-",
                            s.getGstNumber() != null ? s.getGstNumber() : "-",
                            s.getStatus(),
                            s.getCreatedAt() != null
                                ? s.getCreatedAt().toLocalDate().toString() : "-"
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();
    }

    // =========================================================================
    // Dialogs
    // =========================================================================

    private void showSupplierDialog(Supplier existing) {
        boolean isEdit = (existing != null);
        String dialogTitle = isEdit ? "Edit Supplier" : "Add New Supplier";

        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), dialogTitle, true);
        dialog.setSize(480, 440);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        String[] labels = {"Supplier Name *", "Company Name *", "Phone *",
                           "Email", "Address", "GST/Tax Number"};
        JTextField[] fields = new JTextField[labels.length];

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0;
            form.add(bold(labels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            fields[i] = new JTextField(isEdit ? getFieldValue(existing, i) : "");
            fields[i].setPreferredSize(new Dimension(250, 34));
            form.add(fields[i], gbc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton(isEdit ? "Update" : "Add Supplier");
        saveBtn.setBackground(UIConstants.PRIMARY_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                Supplier s = isEdit ? existing : new Supplier();
                s.setSupplierName(fields[0].getText().trim());
                s.setCompanyName(fields[1].getText().trim());
                s.setPhone(fields[2].getText().trim());
                s.setEmail(fields[3].getText().trim().isEmpty() ? null : fields[3].getText().trim());
                s.setAddress(fields[4].getText().trim().isEmpty() ? null : fields[4].getText().trim());
                s.setGstNumber(fields[5].getText().trim().isEmpty() ? null : fields[5].getText().trim());
                if (!isEdit) s.setStatus("ACTIVE");

                if (isEdit) supplierService.updateSupplier(s);
                else        supplierService.addSupplier(s);

                JOptionPane.showMessageDialog(dialog,
                    isEdit ? "Supplier updated! ✅" : "Supplier added! ✅",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadSuppliers();

            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(),
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private String getFieldValue(Supplier s, int idx) {
        return switch (idx) {
            case 0 -> s.getSupplierName() != null ? s.getSupplierName() : "";
            case 1 -> s.getCompanyName()  != null ? s.getCompanyName()  : "";
            case 2 -> s.getPhone()        != null ? s.getPhone()        : "";
            case 3 -> s.getEmail()        != null ? s.getEmail()        : "";
            case 4 -> s.getAddress()      != null ? s.getAddress()      : "";
            case 5 -> s.getGstNumber()    != null ? s.getGstNumber()    : "";
            default -> "";
        };
    }

    private void editSelected() {
        int row = supplierTable.getSelectedRow();
        if (row < 0) { showWarn("Please select a supplier first."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            Supplier s = supplierService.getSupplierById(id);
            if (s != null) showSupplierDialog(s);
        } catch (ServiceException ex) {
            showError(ex.getMessage());
        }
    }

    private void deactivateSelected() {
        int row = supplierTable.getSelectedRow();
        if (row < 0) { showWarn("Please select a supplier first."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        String status = (String) tableModel.getValueAt(row, COLS.length - 2);

        if ("INACTIVE".equals(status)) { showWarn("Supplier is already inactive."); return; }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Deactivate supplier \"" + name + "\"?\n" +
            "Purchase history will be preserved.",
            "Confirm Deactivation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                supplierService.deactivateSupplier(id);
                JOptionPane.showMessageDialog(this, "Supplier deactivated.", "Done",
                    JOptionPane.INFORMATION_MESSAGE);
                loadSuppliers();
            } catch (ServiceException ex) {
                showError(ex.getMessage());
            }
        }
    }

    /** New Purchase dialog — creates a stock-receiving entry for any supplier. */
    private void showPurchaseDialog() {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), "New Medicine Purchase", true);
        dialog.setSize(520, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Supplier combo
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        form.add(bold("Supplier *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JComboBox<Supplier> supplierCombo = new JComboBox<>();
        supplierCombo.setPreferredSize(new Dimension(280, 34));
        loadActiveSuppliers(supplierCombo);
        form.add(supplierCombo, gbc);

        // Medicine combo
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        form.add(bold("Medicine *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        JComboBox<String> medicineCombo = new JComboBox<>();
        medicineCombo.setPreferredSize(new Dimension(280, 34));
        java.util.Map<String, Integer> medMap = loadMedicinesForCombo(medicineCombo);
        form.add(medicineCombo, gbc);

        // Text fields
        String[] fLabels = {
            "Batch Number *", "Quantity *", "Purchase Price ₹ *", "Selling Price ₹",
            "Mfg Date (YYYY-MM-DD)", "Expiry Date * (YYYY-MM-DD)", "Min Stock Level"
        };
        String[] defaults = { "", "1", "0.00", "", "", "", "10" };
        JTextField[] fields = new JTextField[fLabels.length];
        for (int i = 0; i < fLabels.length; i++) {
            gbc.gridx = 0; gbc.gridy = i + 2; gbc.weightx = 0;
            form.add(bold(fLabels[i]), gbc);
            gbc.gridx = 1; gbc.weightx = 1;
            fields[i] = new JTextField(defaults[i]);
            fields[i].setPreferredSize(new Dimension(280, 34));
            form.add(fields[i], gbc);
        }

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, UIConstants.BORDER_COLOR));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.addActionListener(e -> dialog.dispose());

        JButton saveBtn = new JButton("Save Purchase");
        saveBtn.setBackground(UIConstants.SUCCESS_COLOR);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.addActionListener(e -> {
            try {
                Supplier sup = (Supplier) supplierCombo.getSelectedItem();
                if (sup == null) throw new ValidationException("Please select a supplier.");

                String medName = (String) medicineCombo.getSelectedItem();
                if (medName == null || !medMap.containsKey(medName))
                    throw new ValidationException("Please select a medicine.");

                MedicinePurchase purchase = new MedicinePurchase();
                purchase.setSupplierId(sup.getSupplierId());
                purchase.setMedicineId(medMap.get(medName));
                purchase.setBatchNumber(fields[0].getText().trim());
                purchase.setQuantity(Integer.parseInt(fields[1].getText().trim()));
                purchase.setPurchasePrice(new BigDecimal(fields[2].getText().trim()));

                String sp = fields[3].getText().trim();
                if (!sp.isEmpty()) purchase.setSellingPrice(new BigDecimal(sp));

                String mfg = fields[4].getText().trim();
                if (!mfg.isEmpty()) purchase.setManufacturingDate(LocalDate.parse(mfg));

                purchase.setExpiryDate(LocalDate.parse(fields[5].getText().trim()));
                purchase.setMinimumStockLevel(Integer.parseInt(fields[6].getText().trim()));
                purchase.setPerformedBy(Session.getInstance().getUsername());

                supplierService.recordPurchase(purchase);

                JOptionPane.showMessageDialog(dialog,
                    "Purchase recorded successfully! ✅\nStock updated.",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();

            } catch (ValidationException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(),
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid number: " + ex.getMessage(),
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Invalid date format. Use YYYY-MM-DD (e.g. 2028-09-10).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            } catch (ServiceException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnPanel.add(cancelBtn);
        btnPanel.add(saveBtn);
        dialog.add(new JScrollPane(form), BorderLayout.CENTER);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /** Purchase history for selected supplier. */
    private void showSupplierHistory() {
        int row = supplierTable.getSelectedRow();
        if (row < 0) { showWarn("Please select a supplier first."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);

        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Purchase History — " + name, false);
        dialog.setSize(900, 480);
        dialog.setLocationRelativeTo(this);

        String[] cols = {"ID", "Date", "Medicine", "Batch", "Qty", "Price ₹", "Total ₹", "Expiry"};
        DefaultTableModel hModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable hTable = new JTable(hModel);
        hTable.setRowHeight(32);
        hTable.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 12));
        hTable.getTableHeader().setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 12));

        new SwingWorker<List<MedicinePurchase>, Void>() {
            @Override protected List<MedicinePurchase> doInBackground() throws Exception {
                return supplierService.getPurchasesBySupplier(id);
            }
            @Override protected void done() {
                try {
                    for (MedicinePurchase p : get()) {
                        hModel.addRow(new Object[]{
                            p.getPurchaseId(),
                            p.getPurchaseDate() != null ? p.getPurchaseDate().toLocalDate() : "-",
                            p.getMedicineName(),
                            p.getBatchNumber(),
                            p.getQuantity(),
                            p.getPurchasePrice(),
                            p.getTotalAmount(),
                            p.getExpiryDate() != null ? p.getExpiryDate() : "-"
                        });
                    }
                } catch (Exception ignored) {}
            }
        }.execute();

        dialog.add(new JScrollPane(hTable));
        dialog.setVisible(true);
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private void loadActiveSuppliers(JComboBox<Supplier> combo) {
        try {
            for (Supplier s : supplierService.getActiveSuppliers()) combo.addItem(s);
        } catch (ServiceException ignored) {}
    }

    private java.util.Map<String, Integer> loadMedicinesForCombo(JComboBox<String> combo) {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        try {
            for (Medicine m : medicineService.getAllMedicines()) {
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

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
