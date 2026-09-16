package com.pharmacy.management.ui.dialogs;

import com.pharmacy.management.model.Medicine;
import com.pharmacy.management.service.MedicineService;
import com.pharmacy.management.service.ValidationException;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.ui.components.CustomTextField;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Dialog for adding/editing medicines.
 * Requirements: 5.1, 5.2, 5.5, 9.6
 */
public class MedicineFormDialog extends JDialog {
    
    private MedicineService medicineService;
    private Medicine medicine;
    private boolean saved = false;
    
    // Form fields
    private CustomTextField nameField;
    private CustomTextField genericNameField;
    private CustomTextField activeIngredientField;
    private CustomTextField brandNameField;
    private CustomTextField manufacturerField;
    private JComboBox<String> dosageFormField;
    private CustomTextField strengthField;
    private JComboBox<String> categoryField;
    private CustomTextField barcodeField;
    private CustomTextField priceField;
    private JCheckBox prescriptionRequiredField;
    private JTextArea descriptionField;
    private JComboBox<String> statusField;
    
    public MedicineFormDialog(Frame parent, Medicine medicine) {
        super(parent, medicine == null ? "Add Medicine" : "Edit Medicine", true);
        this.medicine = medicine;
        this.medicineService = new MedicineService();
        initializeUI();
        
        if (medicine != null) {
            populateFields();
        }
    }
    
    private void initializeUI() {
        setSize(500, 700);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Form fields
        addFormField(mainPanel, "Medicine Name *", nameField = new CustomTextField(20));
        addFormField(mainPanel, "Generic Name *", genericNameField = new CustomTextField(20));
        addFormField(mainPanel, "Active Ingredient *", activeIngredientField = new CustomTextField(20));
        addFormField(mainPanel, "Brand Name", brandNameField = new CustomTextField(20));
        addFormField(mainPanel, "Manufacturer", manufacturerField = new CustomTextField(20));
        
        dosageFormField = new JComboBox<>(new String[]{"Tablet", "Capsule", "Syrup", "Injection", "Cream", "Inhaler", "Drops"});
        addFormField(mainPanel, "Dosage Form *", dosageFormField);
        
        addFormField(mainPanel, "Strength *", strengthField = new CustomTextField(20));
        
        categoryField = new JComboBox<>(new String[]{"Analgesic", "Antibiotic", "NSAID", "Antihistamine", "PPI", "Antidiabetic", "Antihypertensive", "Bronchodilator"});
        addFormField(mainPanel, "Category", categoryField);
        
        addFormField(mainPanel, "Barcode", barcodeField = new CustomTextField(20));
        addFormField(mainPanel, "Selling Price *", priceField = new CustomTextField(20));
        
        prescriptionRequiredField = new JCheckBox("Prescription Required");
        addFormField(mainPanel, "", prescriptionRequiredField);
        
        descriptionField = new JTextArea(3, 20);
        descriptionField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane descScrollPane = new JScrollPane(descriptionField);
        addFormField(mainPanel, "Description", descScrollPane);
        
        statusField = new JComboBox<>(new String[]{"ACTIVE", "INACTIVE"});
        addFormField(mainPanel, "Status", statusField);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBackground(Color.WHITE);
        
        CustomButton saveButton = new CustomButton("Save");
        saveButton.setBackgroundColor(UIConstants.SUCCESS_COLOR);
        saveButton.addActionListener(e -> saveMedicine());
        
        CustomButton cancelButton = new CustomButton("Cancel");
        cancelButton.setBackgroundColor(UIConstants.BORDER_COLOR);
        cancelButton.setTextColor(UIConstants.TEXT_PRIMARY);
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(buttonPanel);
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    private void addFormField(JPanel parent, String labelText, JComponent field) {
        if (!labelText.isEmpty()) {
            JLabel label = new JLabel(labelText);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            parent.add(label);
            parent.add(Box.createVerticalStrut(5));
        }
        
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, field.getPreferredSize().height));
        parent.add(field);
        parent.add(Box.createVerticalStrut(15));
    }
    
    private void populateFields() {
        nameField.setText(medicine.getMedicineName());
        genericNameField.setText(medicine.getGenericName());
        activeIngredientField.setText(medicine.getActiveIngredient());
        brandNameField.setText(medicine.getBrandName());
        manufacturerField.setText(medicine.getManufacturer());
        dosageFormField.setSelectedItem(medicine.getDosageForm());
        strengthField.setText(medicine.getStrength());
        categoryField.setSelectedItem(medicine.getCategory());
        barcodeField.setText(medicine.getBarcode());
        priceField.setText(medicine.getSellingPrice() != null ? medicine.getSellingPrice().toString() : "");
        prescriptionRequiredField.setSelected(medicine.isPrescriptionRequired());
        descriptionField.setText(medicine.getDescription());
        statusField.setSelectedItem(medicine.getStatus());
    }
    
    private void saveMedicine() {
        try {
            // Validate required fields
            if (nameField.getText().trim().isEmpty()) {
                throw new ValidationException("Medicine name is required");
            }
            
            // Create or update medicine
            Medicine med = medicine != null ? medicine : new Medicine();
            
            med.setMedicineName(nameField.getText().trim());
            med.setGenericName(genericNameField.getText().trim());
            med.setActiveIngredient(activeIngredientField.getText().trim());
            med.setBrandName(brandNameField.getText().trim());
            med.setManufacturer(manufacturerField.getText().trim());
            med.setDosageForm(dosageFormField.getSelectedItem().toString());
            med.setStrength(strengthField.getText().trim());
            med.setCategory(categoryField.getSelectedItem().toString());
            med.setBarcode(barcodeField.getText().trim());
            med.setSellingPrice(new BigDecimal(priceField.getText().trim()));
            med.setPrescriptionRequired(prescriptionRequiredField.isSelected());
            med.setDescription(descriptionField.getText().trim());
            med.setStatus(statusField.getSelectedItem().toString());
            
            if (medicine == null) {
                medicineService.addMedicine(med);
                JOptionPane.showMessageDialog(this, "Medicine added successfully");
            } else {
                medicineService.updateMedicine(med);
                JOptionPane.showMessageDialog(this, "Medicine updated successfully");
            }
            
            saved = true;
            dispose();
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error saving medicine: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public boolean isSaved() {
        return saved;
    }
}