package com.pharmacy.management.ui;

import com.pharmacy.management.dao.StockDAO;
import com.pharmacy.management.model.*;
import com.pharmacy.management.service.*;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.ui.components.CustomTextField;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Main billing screen — search medicines, build cart, generate bills.
 * Module 4 - Real-Time Billing
 */
public class BillingPanel extends JPanel {

    private final MedicineService medicineService;
    private final BillingService billingService;

    // Left panel — search
    private CustomTextField searchField;
    private DefaultTableModel searchTableModel;
    private JTable searchResultsTable;
    private Medicine selectedMedicine;
    private Stock selectedStock;
    private int totalAvailableForSelected;

    // Detail labels
    private JLabel detailName, detailBatch, detailExpiry, detailStock, detailPrice;

    // Cart
    private final List<CartItem> cart = new ArrayList<>();
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JSpinner qtySpinner;

    // Totals
    private JLabel subtotalLabel, grandTotalLabel, balanceLabel;
    private JSpinner discountSpinner, taxSpinner;
    private JComboBox<String> paymentMethodCombo;
    private JTextField amountPaidField;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public BillingPanel() {
        this.medicineService = new MedicineService();
        this.billingService = new BillingService();
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        initUI();
        // Load medicines on open
        searchMedicines("");
    }

    // -----------------------------------------------------------------------
    // UI Construction
    // -----------------------------------------------------------------------

    private void initUI() {
        add(createHeader(), BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, createLeftPanel(), createRightPanel());
        splitPane.setDividerLocation(490);
        splitPane.setResizeWeight(0.42);
        splitPane.setBorder(null);
        add(splitPane, BorderLayout.CENTER);
    }

    private JPanel createHeader() {
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(UIConstants.BACKGROUND_COLOR);
        h.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JLabel title = new JLabel("Billing");
        title.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 26));
        title.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel sub = new JLabel("Generate patient bills and manage payments.");
        sub.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        sub.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel tp = new JPanel(new GridLayout(2, 1, 0, 2));
        tp.setBackground(UIConstants.BACKGROUND_COLOR);
        tp.add(title);
        tp.add(sub);
        h.add(tp, BorderLayout.WEST);
        return h;
    }

    // -----------------------------------------------------------------------
    // LEFT PANEL — Medicine search + add to cart
    // -----------------------------------------------------------------------

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setBackground(Color.WHITE);
        JLabel searchLbl = new JLabel("Search Medicine:");
        searchLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 13));
        searchField = new CustomTextField(20);
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchMedicines(searchField.getText().trim()); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchMedicines(searchField.getText().trim()); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        searchBar.add(searchLbl, BorderLayout.WEST);
        searchBar.add(searchField, BorderLayout.CENTER);

        // Search results table
        String[] cols = {"ID", "Medicine Name", "Category", "Stock", "Unit Price"};
        searchTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        searchResultsTable = new JTable(searchTableModel);
        searchResultsTable.setRowHeight(30);
        searchResultsTable.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 12));
        searchResultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchResultsTable.getTableHeader().setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 12));
        searchResultsTable.getTableHeader().setBackground(new Color(249, 250, 251));
        searchResultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) onMedicineSelected();
        });
        int[] colWidths = {40, 170, 80, 60, 80};
        for (int i = 0; i < colWidths.length; i++)
            searchResultsTable.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);

        JScrollPane searchSP = new JScrollPane(searchResultsTable);
        searchSP.setPreferredSize(new Dimension(440, 200));
        searchSP.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));

        // Details panel
        JPanel details = new JPanel(new GridLayout(5, 2, 4, 6));
        details.setBackground(new Color(248, 250, 252));
        details.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        detailName   = addDetailRow(details, "Medicine:");
        detailBatch  = addDetailRow(details, "Batch:");
        detailExpiry = addDetailRow(details, "Expiry:");
        detailStock  = addDetailRow(details, "Available:");
        detailPrice  = addDetailRow(details, "Unit Price:");

        // Quantity + add row
        JPanel addRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        addRow.setBackground(Color.WHITE);
        JLabel qtyLbl = new JLabel("Quantity:");
        qtyLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        addRow.add(qtyLbl);

        SpinnerNumberModel qtyModel = new SpinnerNumberModel(1, 1, 9999, 1);
        qtySpinner = new JSpinner(qtyModel);
        qtySpinner.setPreferredSize(new Dimension(80, 34));
        addRow.add(qtySpinner);

        CustomButton addBtn = new CustomButton("Add to Cart");
        addBtn.setBackgroundColor(UIConstants.PRIMARY_COLOR);
        addBtn.setPreferredSize(new Dimension(140, 36));
        addBtn.addActionListener(e -> addToCart());
        addRow.add(addBtn);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setBackground(Color.WHITE);
        center.add(searchSP, BorderLayout.NORTH);
        center.add(details, BorderLayout.CENTER);
        center.add(addRow, BorderLayout.SOUTH);

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JLabel addDetailRow(JPanel panel, String labelText) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 12));
        JLabel val = new JLabel("-");
        val.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 12));
        panel.add(lbl);
        panel.add(val);
        return val;
    }

    // -----------------------------------------------------------------------
    // RIGHT PANEL — Cart + totals + payment
    // -----------------------------------------------------------------------

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)));

        // Cart table
        String[] cols = {"#", "Medicine", "Batch", "Qty", "Unit Price", "Total"};
        cartTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartTableModel);
        cartTable.setRowHeight(32);
        cartTable.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 12));
        cartTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        cartTable.getTableHeader().setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 12));
        cartTable.getTableHeader().setBackground(new Color(249, 250, 251));

        JScrollPane cartSP = new JScrollPane(cartTable);
        cartSP.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));

        // Remove button
        CustomButton removeBtn = new CustomButton("Remove Selected");
        removeBtn.setBackgroundColor(UIConstants.DANGER_COLOR);
        removeBtn.setPreferredSize(new Dimension(155, 32));
        removeBtn.addActionListener(e -> removeFromCart());
        JPanel removePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        removePanel.setBackground(Color.WHITE);
        removePanel.add(removeBtn);

        JPanel cartSection = new JPanel(new BorderLayout(0, 4));
        cartSection.setBackground(Color.WHITE);

        JLabel cartTitle = new JLabel("Cart");
        cartTitle.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 14));
        cartTitle.setForeground(UIConstants.TEXT_PRIMARY);
        cartSection.add(cartTitle, BorderLayout.NORTH);
        cartSection.add(cartSP, BorderLayout.CENTER);
        cartSection.add(removePanel, BorderLayout.SOUTH);

        panel.add(cartSection, BorderLayout.CENTER);
        panel.add(createTotalsPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createTotalsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIConstants.BORDER_COLOR),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(3, 4, 3, 4);
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;

        // Subtotal
        subtotalLabel = new JLabel("₹0.00");
        subtotalLabel.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 14));
        addTotalRow(panel, gbc, row++, "Subtotal:", subtotalLabel);

        // Discount spinner
        discountSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.5));
        discountSpinner.setPreferredSize(new Dimension(90, 28));
        ((JSpinner.DefaultEditor) discountSpinner.getEditor()).getTextField().setEditable(true);
        discountSpinner.addChangeListener(e -> recalculate());
        addTotalRow(panel, gbc, row++, "Discount %:", discountSpinner);

        // Tax spinner
        taxSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.5));
        taxSpinner.setPreferredSize(new Dimension(90, 28));
        ((JSpinner.DefaultEditor) taxSpinner.getEditor()).getTextField().setEditable(true);
        taxSpinner.addChangeListener(e -> recalculate());
        addTotalRow(panel, gbc, row++, "Tax %:", taxSpinner);

        // Grand Total
        grandTotalLabel = new JLabel("₹0.00");
        grandTotalLabel.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 18));
        grandTotalLabel.setForeground(UIConstants.PRIMARY_COLOR);
        addTotalRow(panel, gbc, row++, "Grand Total:", grandTotalLabel);

        // Separator
        gbc.gridx = 0; gbc.gridy = row++; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(new JSeparator(), gbc);
        gbc.gridwidth = 1; gbc.fill = GridBagConstraints.NONE;

        // Payment method
        paymentMethodCombo = new JComboBox<>(new String[]{"CASH", "CARD", "UPI"});
        paymentMethodCombo.setPreferredSize(new Dimension(120, 28));
        addTotalRow(panel, gbc, row++, "Payment Method:", paymentMethodCombo);

        // Amount paid
        amountPaidField = new JTextField("0.00");
        amountPaidField.setPreferredSize(new Dimension(120, 28));
        amountPaidField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { recalculate(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { recalculate(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        addTotalRow(panel, gbc, row++, "Amount Paid (₹):", amountPaidField);

        // Balance
        balanceLabel = new JLabel("₹0.00");
        balanceLabel.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 14));
        balanceLabel.setForeground(UIConstants.SUCCESS_COLOR);
        addTotalRow(panel, gbc, row++, "Balance:", balanceLabel);

        // Action buttons
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 4, 4, 4);

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 8, 0));
        btnPanel.setBackground(new Color(248, 250, 252));

        CustomButton generateBtn = new CustomButton("Generate Bill");
        generateBtn.setBackgroundColor(UIConstants.SUCCESS_COLOR);
        generateBtn.addActionListener(e -> generateBill());

        CustomButton clearBtn = new CustomButton("Clear Bill");
        clearBtn.setBackgroundColor(UIConstants.DANGER_COLOR);
        clearBtn.addActionListener(e -> clearBill());

        btnPanel.add(generateBtn);
        btnPanel.add(clearBtn);
        panel.add(btnPanel, gbc);

        return panel;
    }

    private void addTotalRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, Component valueComp) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        panel.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        panel.add(valueComp, gbc);
    }

    // -----------------------------------------------------------------------
    // Business logic — search
    // -----------------------------------------------------------------------

    private void searchMedicines(String term) {
        new SwingWorker<List<Medicine>, Void>() {
            @Override
            protected List<Medicine> doInBackground() throws Exception {
                if (term.isEmpty()) return medicineService.getAllMedicines();
                return medicineService.searchMedicines(term);
            }

            @Override
            protected void done() {
                try {
                    List<Medicine> medicines = get();
                    searchTableModel.setRowCount(0);
                    StockDAO dao = new StockDAO();
                    for (Medicine m : medicines) {
                        if (!"ACTIVE".equals(m.getStatus())) continue;
                        int avail = 0;
                        try {
                            avail = dao.findAvailableStockForMedicine(m.getMedicineId())
                                       .stream().mapToInt(Stock::getQuantity).sum();
                        } catch (Exception ignored) {}
                        searchTableModel.addRow(new Object[]{
                            m.getMedicineId(),
                            m.getMedicineName(),
                            m.getCategory(),
                            avail,
                            "₹" + m.getSellingPrice()
                        });
                    }
                } catch (InterruptedException | ExecutionException ignored) {}
            }
        }.execute();
    }

    // -----------------------------------------------------------------------
    // Business logic — medicine selection
    // -----------------------------------------------------------------------

    private void onMedicineSelected() {
        int row = searchResultsTable.getSelectedRow();
        if (row < 0) return;
        int medicineId = (int) searchTableModel.getValueAt(row, 0);

        new SwingWorker<Void, Void>() {
            private Medicine medicine;
            private Stock firstStock;
            private int totalAvail;

            @Override
            protected Void doInBackground() throws Exception {
                medicine = medicineService.getMedicineById(medicineId);
                try {
                    List<Stock> stocks = new StockDAO().findAvailableStockForMedicine(medicineId);
                    firstStock = stocks.isEmpty() ? null : stocks.get(0);
                    totalAvail = stocks.stream().mapToInt(Stock::getQuantity).sum();
                } catch (Exception ignored) {}
                return null;
            }

            @Override
            protected void done() {
                if (medicine == null) return;
                selectedMedicine = medicine;
                selectedStock = firstStock;
                totalAvailableForSelected = totalAvail;

                detailName.setText(medicine.getMedicineName());
                detailBatch.setText(firstStock != null ? firstStock.getBatchNumber() : "N/A");

                String expiryStr = (firstStock != null && firstStock.getExpiryDate() != null)
                        ? firstStock.getExpiryDate().format(DATE_FMT) : "N/A";
                boolean expired = firstStock != null && firstStock.isExpired();
                detailExpiry.setText(expiryStr + (expired ? "  [EXPIRED]" : ""));
                detailExpiry.setForeground(expired ? UIConstants.DANGER_COLOR : UIConstants.TEXT_PRIMARY);

                detailStock.setText(String.valueOf(totalAvail));
                detailStock.setForeground(totalAvail == 0 ? UIConstants.DANGER_COLOR : UIConstants.TEXT_PRIMARY);
                detailPrice.setText("₹" + medicine.getSellingPrice());

                int maxQty = Math.max(1, totalAvail);
                ((SpinnerNumberModel) qtySpinner.getModel()).setMaximum(maxQty);
                ((SpinnerNumberModel) qtySpinner.getModel()).setValue(1);
            }
        }.execute();
    }

    // -----------------------------------------------------------------------
    // Business logic — cart operations
    // -----------------------------------------------------------------------

    private void addToCart() {
        if (selectedMedicine == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a medicine first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedStock != null && selectedStock.isExpired()) {
            JOptionPane.showMessageDialog(this,
                    "Cannot add expired medicine: " + selectedMedicine.getMedicineName(),
                    "Expired Medicine", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (totalAvailableForSelected == 0) {
            JOptionPane.showMessageDialog(this,
                    "No stock available for: " + selectedMedicine.getMedicineName(),
                    "Out of Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int qty = (int) qtySpinner.getValue();
        int medicineId = selectedMedicine.getMedicineId();

        // Check if already in cart — merge qty
        for (CartItem item : cart) {
            if (item.getMedicineId() == medicineId) {
                int newQty = item.getQuantity() + qty;
                if (newQty > item.getAvailableStock()) {
                    JOptionPane.showMessageDialog(this,
                            "Cannot exceed available stock: " + item.getAvailableStock(),
                            "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                item.setQuantity(newQty);
                refreshCartTable();
                recalculate();
                return;
            }
        }

        if (qty > totalAvailableForSelected) {
            JOptionPane.showMessageDialog(this,
                    "Insufficient stock. Available: " + totalAvailableForSelected,
                    "Insufficient Stock", JOptionPane.WARNING_MESSAGE);
            return;
        }

        CartItem item = new CartItem();
        item.setMedicineId(medicineId);
        item.setMedicineName(selectedMedicine.getMedicineName());
        item.setBatchNumber(selectedStock != null ? selectedStock.getBatchNumber() : "");
        item.setAvailableStock(totalAvailableForSelected);
        item.setQuantity(qty);
        item.setUnitPrice(selectedMedicine.getSellingPrice());
        item.setDiscountPercent(BigDecimal.ZERO);
        item.setTaxPercent(BigDecimal.ZERO);

        cart.add(item);
        refreshCartTable();
        recalculate();
    }

    private void removeFromCart() {
        int row = cartTable.getSelectedRow();
        if (row >= 0 && row < cart.size()) {
            cart.remove(row);
            refreshCartTable();
            recalculate();
        }
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        int idx = 1;
        for (CartItem item : cart) {
            BigDecimal lineTotal = item.getUnitPrice()
                    .multiply(new BigDecimal(item.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            cartTableModel.addRow(new Object[]{
                idx++,
                item.getMedicineName(),
                item.getBatchNumber(),
                item.getQuantity(),
                "₹" + item.getUnitPrice(),
                "₹" + lineTotal
            });
        }
    }

    // -----------------------------------------------------------------------
    // Business logic — totals recalculation
    // -----------------------------------------------------------------------

    private void recalculate() {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem item : cart) {
            subtotal = subtotal.add(item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())));
        }

        double discountPct = ((Number) discountSpinner.getValue()).doubleValue();
        double taxPct      = ((Number) taxSpinner.getValue()).doubleValue();

        BigDecimal discountAmt = subtotal
                .multiply(BigDecimal.valueOf(discountPct))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal taxable  = subtotal.subtract(discountAmt);
        BigDecimal taxAmt   = taxable
                .multiply(BigDecimal.valueOf(taxPct))
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal grandTotal = taxable.add(taxAmt).setScale(2, RoundingMode.HALF_UP);

        subtotalLabel.setText("₹" + subtotal.setScale(2, RoundingMode.HALF_UP));
        grandTotalLabel.setText("₹" + grandTotal);

        try {
            BigDecimal paid    = new BigDecimal(amountPaidField.getText().trim());
            BigDecimal balance = paid.subtract(grandTotal).setScale(2, RoundingMode.HALF_UP);
            balanceLabel.setText("₹" + balance);
            balanceLabel.setForeground(
                    balance.compareTo(BigDecimal.ZERO) >= 0
                    ? UIConstants.SUCCESS_COLOR : UIConstants.DANGER_COLOR);
        } catch (NumberFormatException ignored) {
            balanceLabel.setText("₹0.00");
        }
    }

    // -----------------------------------------------------------------------
    // Business logic — generate bill
    // -----------------------------------------------------------------------

    private void generateBill() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Cart is empty. Add medicines before generating a bill.",
                    "Empty Cart", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal discountPct = BigDecimal.valueOf(((Number) discountSpinner.getValue()).doubleValue());
        BigDecimal taxPct      = BigDecimal.valueOf(((Number) taxSpinner.getValue()).doubleValue());
        String paymentMethod   = (String) paymentMethodCombo.getSelectedItem();

        BigDecimal amountPaid;
        try {
            amountPaid = new BigDecimal(amountPaidField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid amount paid.", "Invalid Amount", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Session session = Session.getInstance();
        List<CartItem> cartSnapshot = new ArrayList<>(cart);

        new SwingWorker<Bill, Void>() {
            @Override
            protected Bill doInBackground() throws Exception {
                return billingService.generateBill(
                        cartSnapshot,
                        discountPct, taxPct, paymentMethod, amountPaid,
                        1,   // userId = 1 as fallback; performedBy carries the identity
                        session.getUsername());
            }

            @Override
            protected void done() {
                try {
                    Bill bill = get();
                    showReceiptDialog(bill);
                    clearBill();
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    String msg = cause != null ? cause.getMessage() : ex.getMessage();
                    JOptionPane.showMessageDialog(BillingPanel.this,
                            msg, "Billing Error", JOptionPane.ERROR_MESSAGE);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    JOptionPane.showMessageDialog(BillingPanel.this,
                            "Bill generation interrupted.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    // -----------------------------------------------------------------------
    // Business logic — clear
    // -----------------------------------------------------------------------

    private void clearBill() {
        cart.clear();
        refreshCartTable();
        recalculate();
        selectedMedicine = null;
        selectedStock = null;
        totalAvailableForSelected = 0;
        detailName.setText("-");
        detailBatch.setText("-");
        detailExpiry.setText("-");
        detailExpiry.setForeground(UIConstants.TEXT_PRIMARY);
        detailStock.setText("-");
        detailPrice.setText("-");
        searchField.setText("");
        amountPaidField.setText("0.00");
        discountSpinner.setValue(0.0);
        taxSpinner.setValue(0.0);
        searchMedicines("");
    }

    // -----------------------------------------------------------------------
    // Receipt dialog
    // -----------------------------------------------------------------------

    private void showReceiptDialog(Bill bill) {
        StringBuilder sb = new StringBuilder();
        sb.append("=================================\n");
        sb.append("        SMART PHARMACY\n");
        sb.append("=================================\n");
        sb.append(String.format("Bill No   : %s%n", bill.getBillNumber()));
        sb.append(String.format("Date      : %s%n", bill.getBillDate() != null
                ? bill.getBillDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")) : ""));
        sb.append(String.format("Pharmacist: %s%n", bill.getPerformedBy()));
        sb.append("---------------------------------\n");
        sb.append(String.format("%-22s %4s %9s%n", "Medicine", "Qty", "Total"));
        sb.append("---------------------------------\n");

        try {
            Bill fullBill = billingService.getBillById(bill.getBillId());
            if (fullBill != null && fullBill.getItems() != null) {
                for (BillItem item : fullBill.getItems()) {
                    sb.append(String.format("%-22s %4d  ₹%s%n",
                            truncate(item.getMedicineName(), 22),
                            item.getQuantity(),
                            item.getTotal()));
                }
            }
        } catch (ServiceException ignored) {}

        sb.append("---------------------------------\n");
        sb.append(String.format("Subtotal          : ₹%s%n", bill.getSubtotal()));
        sb.append(String.format("Discount (%.1f%%)  : ₹%s%n",
                bill.getDiscountPercent().doubleValue(), bill.getDiscountAmount()));
        sb.append(String.format("Tax (%.1f%%)       : ₹%s%n",
                bill.getTaxPercent().doubleValue(), bill.getTaxAmount()));
        sb.append(String.format("GRAND TOTAL       : ₹%s%n", bill.getGrandTotal()));
        sb.append(String.format("Payment           : %s%n", bill.getPaymentMethod()));
        sb.append(String.format("Amount Paid       : ₹%s%n", bill.getAmountPaid()));
        sb.append(String.format("Balance           : ₹%s%n", bill.getBalance()));
        sb.append("=================================\n");
        sb.append("          Thank You!\n");
        sb.append("=================================\n");

        JTextArea receipt = new JTextArea(sb.toString());
        receipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        receipt.setEditable(false);
        receipt.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(receipt);
        scrollPane.setPreferredSize(new Dimension(430, 490));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton printBtn = new JButton("🖨 Print Receipt");
        printBtn.addActionListener(e -> {
            try { receipt.print(); }
            catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage());
            }
        });
        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> {
            Window w = SwingUtilities.getWindowAncestor(btnPanel);
            if (w != null) w.dispose();
        });
        btnPanel.add(printBtn);
        btnPanel.add(closeBtn);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBackground(Color.WHITE);
        content.add(scrollPane, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, content,
                "Bill Receipt — " + bill.getBillNumber(), JOptionPane.PLAIN_MESSAGE);
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + ".";
    }
}
