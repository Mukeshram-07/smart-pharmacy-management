package com.pharmacy.management.ui;

import com.pharmacy.management.model.Medicine;
import com.pharmacy.management.service.MedicineService;
import com.pharmacy.management.service.ServiceException;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.ui.components.CustomTextField;
import com.pharmacy.management.ui.components.MedicineTableModel;
import com.pharmacy.management.ui.components.StatusBadge;
import com.pharmacy.management.ui.dialogs.MedicineFormDialog;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * Medicine management panel.
 * Requirements: 5.3, 5.4, 5.6, 9.7, 9.8
 */
public class MedicinePanel extends JPanel {
    
    private MedicineService medicineService;
    private MedicineTableModel tableModel;
    private JTable medicineTable;
    private CustomTextField searchField;
    
    public MedicinePanel() {
        this.medicineService = new MedicineService();
        this.tableModel = new MedicineTableModel();
        initializeUI();
        loadMedicines();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        add(createHeader(), BorderLayout.NORTH);
        
        // Table
        add(createTablePanel(), BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Medicine Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        searchField = new CustomTextField(20);
        searchField.setPreferredSize(new Dimension(200, 35));
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { searchMedicines(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { searchMedicines(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { searchMedicines(); }
        });
        
        CustomButton addButton = new CustomButton("Add Medicine");
        addButton.setBackgroundColor(UIConstants.SUCCESS_COLOR);
        addButton.addActionListener(e -> showAddMedicineDialog());
        
        CustomButton refreshButton = new CustomButton("Refresh");
        refreshButton.addActionListener(e -> loadMedicines());
        
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(addButton);
        searchPanel.add(refreshButton);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(searchPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        
        medicineTable = new JTable(tableModel);
        medicineTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        medicineTable.setRowHeight(30);
        medicineTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        medicineTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Status column renderer
        medicineTable.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                if (value != null) {
                    return new StatusBadge(value.toString());
                }
                return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        });
        
        // Actions column click handler
        medicineTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = medicineTable.rowAtPoint(e.getPoint());
                int column = medicineTable.columnAtPoint(e.getPoint());
                
                if (column == 7 && row >= 0) { // Actions column
                    Medicine medicine = tableModel.getMedicineAt(row);
                    if (medicine != null) {
                        showActionsMenu(e.getComponent(), e.getX(), e.getY(), medicine);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(medicineTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIConstants.BORDER_COLOR));
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private void showActionsMenu(Component parent, int x, int y, Medicine medicine) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem editItem = new JMenuItem("Edit");
        editItem.addActionListener(e -> showEditMedicineDialog(medicine));
        menu.add(editItem);
        
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> deleteMedicine(medicine));
        menu.add(deleteItem);
        
        menu.show(parent, x, y);
    }
    
    private void loadMedicines() {
        SwingWorker<List<Medicine>, Void> worker = new SwingWorker<List<Medicine>, Void>() {
            @Override
            protected List<Medicine> doInBackground() throws Exception {
                return medicineService.getAllMedicines();
            }
            
            @Override
            protected void done() {
                try {
                    List<Medicine> medicines = get();
                    tableModel.setMedicines(medicines);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MedicinePanel.this, 
                        "Failed to load medicines: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void searchMedicines() {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadMedicines();
            return;
        }
        
        SwingWorker<List<Medicine>, Void> worker = new SwingWorker<List<Medicine>, Void>() {
            @Override
            protected List<Medicine> doInBackground() throws Exception {
                return medicineService.searchMedicines(searchTerm);
            }
            
            @Override
            protected void done() {
                try {
                    List<Medicine> medicines = get();
                    tableModel.setMedicines(medicines);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MedicinePanel.this, 
                        "Search failed: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void showAddMedicineDialog() {
        MedicineFormDialog dialog = new MedicineFormDialog((Frame) SwingUtilities.getWindowAncestor(this), null);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadMedicines();
        }
    }
    
    private void showEditMedicineDialog(Medicine medicine) {
        MedicineFormDialog dialog = new MedicineFormDialog((Frame) SwingUtilities.getWindowAncestor(this), medicine);
        dialog.setVisible(true);
        
        if (dialog.isSaved()) {
            loadMedicines();
        }
    }
    
    private void deleteMedicine(Medicine medicine) {
        int result = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete " + medicine.getMedicineName() + "?",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            try {
                medicineService.deleteMedicine(medicine.getMedicineId());
                JOptionPane.showMessageDialog(this, "Medicine deleted successfully");
                loadMedicines();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Failed to delete medicine: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}