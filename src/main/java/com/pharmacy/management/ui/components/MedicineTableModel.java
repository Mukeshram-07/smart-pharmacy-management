package com.pharmacy.management.ui.components;

import com.pharmacy.management.model.Medicine;

import javax.swing.table.AbstractTableModel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for medicine display.
 * Requirements: 5.3, 5.6
 */
public class MedicineTableModel extends AbstractTableModel {
    
    private static final String[] COLUMNS = {
        "ID", "Name", "Generic Name", "Active Ingredient", 
        "Strength", "Price", "Status", "Actions"
    };
    
    private List<Medicine> medicines = new ArrayList<>();
    
    public void setMedicines(List<Medicine> medicines) {
        this.medicines = medicines != null ? medicines : new ArrayList<>();
        fireTableDataChanged();
    }
    
    public Medicine getMedicineAt(int row) {
        if (row >= 0 && row < medicines.size()) {
            return medicines.get(row);
        }
        return null;
    }
    
    @Override
    public int getRowCount() {
        return medicines.size();
    }
    
    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }
    
    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }
    
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= medicines.size()) {
            return null;
        }
        
        Medicine medicine = medicines.get(rowIndex);
        
        switch (columnIndex) {
            case 0: return medicine.getMedicineId();
            case 1: return medicine.getMedicineName();
            case 2: return medicine.getGenericName();
            case 3: return medicine.getActiveIngredient();
            case 4: return medicine.getStrength();
            case 5: return medicine.getSellingPrice();
            case 6: return medicine.getStatus();
            case 7: return "Edit | Delete";
            default: return null;
        }
    }
    
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0: return Integer.class;
            case 5: return BigDecimal.class;
            default: return String.class;
        }
    }
}