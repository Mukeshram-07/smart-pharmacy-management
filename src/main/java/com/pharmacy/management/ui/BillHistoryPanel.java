package com.pharmacy.management.ui;

import com.pharmacy.management.model.Bill;
import com.pharmacy.management.model.BillItem;
import com.pharmacy.management.service.BillingService;
import com.pharmacy.management.service.ServiceException;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.ui.components.CustomTextField;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Bill history panel — view and search all past bills.
 * Module 4 - Real-Time Billing
 */
public class BillHistoryPanel extends JPanel {

    private final BillingService billingService;
    private DefaultTableModel tableModel;
    private JTable historyTable;
    private CustomTextField searchField;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    private static final String[] COLUMNS = {
        "Bill ID", "Bill Number", "Date", "Pharmacist", "Grand Total", "Payment", "Amount Paid"
    };

    public BillHistoryPanel() {
        this.billingService = new BillingService();
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        initUI();
        loadBills();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout(0, 0));
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 14, 0));

        JLabel titleLbl = new JLabel("Bill History");
        titleLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 26));
        titleLbl.setForeground(UIConstants.TEXT_PRIMARY);

        JLabel subLbl = new JLabel("View and search all generated bills.");
        subLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        subLbl.setForeground(UIConstants.TEXT_SECONDARY);

        JPanel titleStack = new JPanel(new GridLayout(2, 1, 0, 2));
        titleStack.setBackground(UIConstants.BACKGROUND_COLOR);
        titleStack.add(titleLbl);
        titleStack.add(subLbl);

        // Search + refresh
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchBar.setBackground(UIConstants.BACKGROUND_COLOR);
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        searchField = new CustomTextField(20);
        searchField.setPreferredSize(new Dimension(200, 34));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { doSearch(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        CustomButton refreshBtn = new CustomButton("Refresh");
        refreshBtn.setPreferredSize(new Dimension(95, 34));
        refreshBtn.addActionListener(e -> loadBills());

        searchBar.add(searchLbl);
        searchBar.add(searchField);
        searchBar.add(refreshBtn);

        header.add(titleStack, BorderLayout.WEST);
        header.add(searchBar, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(tableModel);
        historyTable.setRowHeight(34);
        historyTable.setFont(new Font(UIConstants.FONT_FAMILY, Font.PLAIN, 13));
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getTableHeader().setFont(new Font(UIConstants.FONT_FAMILY, Font.BOLD, 13));
        historyTable.getTableHeader().setBackground(new Color(249, 250, 251));
        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) viewSelectedBill();
            }
        });

        JScrollPane sp = new JScrollPane(historyTable);
        sp.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        add(sp, BorderLayout.CENTER);

        // Bottom actions
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        actions.setBackground(UIConstants.BACKGROUND_COLOR);

        CustomButton viewBtn = new CustomButton("View Bill Details");
        viewBtn.setPreferredSize(new Dimension(160, 36));
        viewBtn.addActionListener(e -> viewSelectedBill());
        actions.add(viewBtn);

        add(actions, BorderLayout.SOUTH);
    }

    // -----------------------------------------------------------------------
    // Data loading
    // -----------------------------------------------------------------------

    private void loadBills() {
        new SwingWorker<List<Bill>, Void>() {
            @Override protected List<Bill> doInBackground() throws Exception {
                return billingService.getAllBills();
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (InterruptedException | ExecutionException ignored) {}
            }
        }.execute();
    }

    private void doSearch() {
        String term = searchField.getText().trim();
        if (term.isEmpty()) { loadBills(); return; }
        new SwingWorker<List<Bill>, Void>() {
            @Override protected List<Bill> doInBackground() throws Exception {
                return billingService.searchBills(term);
            }
            @Override protected void done() {
                try { populateTable(get()); }
                catch (InterruptedException | ExecutionException ignored) {}
            }
        }.execute();
    }

    private void populateTable(List<Bill> bills) {
        tableModel.setRowCount(0);
        for (Bill b : bills) {
            tableModel.addRow(new Object[]{
                b.getBillId(),
                b.getBillNumber(),
                b.getBillDate() != null ? b.getBillDate().format(DATE_FMT) : "-",
                b.getPerformedBy(),
                "₹" + b.getGrandTotal(),
                b.getPaymentMethod(),
                "₹" + b.getAmountPaid()
            });
        }
    }

    // -----------------------------------------------------------------------
    // View selected bill details
    // -----------------------------------------------------------------------

    private void viewSelectedBill() {
        int row = historyTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Please select a bill first.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int billId = (int) tableModel.getValueAt(row, 0);
        try {
            Bill bill = billingService.getBillById(billId);
            if (bill == null) {
                JOptionPane.showMessageDialog(this, "Bill not found.");
                return;
            }

            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Bill    : %s%n", bill.getBillNumber()));
            sb.append(String.format("Date    : %s%n",
                    bill.getBillDate() != null ? bill.getBillDate().format(DATE_FMT) : ""));
            sb.append(String.format("Pharmacist: %s%n", bill.getPerformedBy()));
            sb.append("\n");
            sb.append(String.format("%-25s %5s %12s%n", "Medicine", "Qty", "Total"));
            sb.append("-------------------------------------------\n");

            if (bill.getItems() != null) {
                for (BillItem item : bill.getItems()) {
                    sb.append(String.format("%-25s %5d  ₹%s%n",
                            truncate(item.getMedicineName(), 25),
                            item.getQuantity(),
                            item.getTotal()));
                }
            }

            sb.append("-------------------------------------------\n");
            sb.append(String.format("Subtotal      : ₹%s%n", bill.getSubtotal()));
            sb.append(String.format("Discount      : ₹%s (%.1f%%)%n",
                    bill.getDiscountAmount(), bill.getDiscountPercent().doubleValue()));
            sb.append(String.format("Tax           : ₹%s (%.1f%%)%n",
                    bill.getTaxAmount(), bill.getTaxPercent().doubleValue()));
            sb.append(String.format("GRAND TOTAL   : ₹%s%n", bill.getGrandTotal()));
            sb.append(String.format("Payment       : %s%n", bill.getPaymentMethod()));
            sb.append(String.format("Paid          : ₹%s   Balance: ₹%s%n",
                    bill.getAmountPaid(), bill.getBalance()));

            JTextArea ta = new JTextArea(sb.toString());
            ta.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            ta.setEditable(false);
            ta.setBackground(Color.WHITE);

            JScrollPane sp = new JScrollPane(ta);
            sp.setPreferredSize(new Dimension(460, 360));

            JOptionPane.showMessageDialog(this, sp,
                    "Bill Details — " + bill.getBillNumber(), JOptionPane.PLAIN_MESSAGE);

        } catch (ServiceException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() <= maxLen ? s : s.substring(0, maxLen - 1) + ".";
    }
}
