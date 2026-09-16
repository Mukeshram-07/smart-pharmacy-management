package com.pharmacy.management.ui;

import com.pharmacy.management.model.Medicine;
import com.pharmacy.management.service.OCRException;
import com.pharmacy.management.service.OCRService;
import com.pharmacy.management.ui.components.CustomButton;
import com.pharmacy.management.ui.components.RoundedPanel;
import com.pharmacy.management.util.UIConstants;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * Prescription scanner panel for OCR processing.
 * Requirements: 4.1, 4.4, 4.5, 9.7
 */
public class PrescriptionPanel extends JPanel {
    
    private OCRService ocrService;
    private JLabel imageLabel;
    private JTable matchedMedicinesTable;
    private JTextArea unmatchedTextArea;
    private JTextArea rawTextArea;
    private CustomButton uploadButton;
    private CustomButton processButton;
    private File selectedImage;
    
    public PrescriptionPanel() {
        this.ocrService = new OCRService();
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(UIConstants.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        add(createHeader(), BorderLayout.NORTH);
        
        // Main content
        add(createMainContent(), BorderLayout.CENTER);
    }
    
    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UIConstants.BACKGROUND_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Prescription Scanner");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(UIConstants.TEXT_PRIMARY);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        uploadButton = new CustomButton("Upload Image");
        uploadButton.setBackgroundColor(UIConstants.PRIMARY_COLOR);
        uploadButton.addActionListener(e -> uploadImage());
        
        processButton = new CustomButton("Process Prescription");
        processButton.setBackgroundColor(UIConstants.SUCCESS_COLOR);
        processButton.setEnabled(false);
        processButton.addActionListener(e -> processPrescription());
        
        buttonPanel.add(uploadButton);
        buttonPanel.add(processButton);
        
        header.add(titleLabel, BorderLayout.WEST);
        header.add(buttonPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createMainContent() {
        JPanel mainContent = new JPanel(new BorderLayout(10, 10));
        mainContent.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Left panel - Image preview
        JPanel leftPanel = createImagePanel();
        leftPanel.setPreferredSize(new Dimension(400, 0));
        
        // Right panel - Results
        JPanel rightPanel = createResultsPanel();
        
        mainContent.add(leftPanel, BorderLayout.WEST);
        mainContent.add(rightPanel, BorderLayout.CENTER);
        
        return mainContent;
    }
    
    private JPanel createImagePanel() {
        RoundedPanel imagePanel = new RoundedPanel();
        imagePanel.setLayout(new BorderLayout());
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel panelTitle = new JLabel("Prescription Image");
        panelTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panelTitle.setHorizontalAlignment(SwingConstants.CENTER);
        
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setBackground(UIConstants.BACKGROUND_COLOR);
        imageLabel.setOpaque(true);
        imageLabel.setBorder(BorderFactory.createDashedBorder(UIConstants.BORDER_COLOR, 2, 5, 5, false));
        imageLabel.setText("<html><center>📷<br>Click 'Upload Image' to select<br>PNG, JPG, JPEG files</center></html>");
        imageLabel.setForeground(UIConstants.TEXT_SECONDARY);
        
        JScrollPane imageScrollPane = new JScrollPane(imageLabel);
        imageScrollPane.setPreferredSize(new Dimension(350, 400));
        
        imagePanel.add(panelTitle, BorderLayout.NORTH);
        imagePanel.add(imageScrollPane, BorderLayout.CENTER);
        
        return imagePanel;
    }
    
    private JPanel createResultsPanel() {
        JPanel resultsPanel = new JPanel(new BorderLayout(0, 10));
        resultsPanel.setBackground(UIConstants.BACKGROUND_COLOR);
        
        // Matched medicines table
        JPanel matchedPanel = createMatchedMedicinesPanel();
        
        // Unmatched text area
        JPanel unmatchedPanel = createUnmatchedTextPanel();
        
        // Raw text area
        JPanel rawTextPanel = createRawTextPanel();
        
        JSplitPane topSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, matchedPanel, unmatchedPanel);
        topSplit.setResizeWeight(0.5);
        
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topSplit, rawTextPanel);
        mainSplit.setResizeWeight(0.7);
        
        resultsPanel.add(mainSplit, BorderLayout.CENTER);
        
        return resultsPanel;
    }
    
    private JPanel createMatchedMedicinesPanel() {
        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel title = new JLabel("Matched Medicines");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Name", "Generic", "Price", "Prescription Required"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        matchedMedicinesTable = new JTable(tableModel);
        matchedMedicinesTable.setRowHeight(25);
        matchedMedicinesTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(matchedMedicinesTable);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createUnmatchedTextPanel() {
        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel title = new JLabel("Unmatched Text");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        unmatchedTextArea = new JTextArea();
        unmatchedTextArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        unmatchedTextArea.setEditable(false);
        unmatchedTextArea.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(unmatchedTextArea);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRawTextPanel() {
        RoundedPanel panel = new RoundedPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel title = new JLabel("Raw OCR Text");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        rawTextArea = new JTextArea();
        rawTextArea.setFont(new Font("Courier New", Font.PLAIN, 11));
        rawTextArea.setEditable(false);
        rawTextArea.setBackground(UIConstants.BACKGROUND_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(rawTextArea);
        scrollPane.setPreferredSize(new Dimension(0, 100));
        
        panel.add(title, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void uploadImage() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            selectedImage = fileChooser.getSelectedFile();
            
            try {
                // Load and display image preview
                BufferedImage image = ImageIO.read(selectedImage);
                if (image != null) {
                    // Scale image for preview
                    int maxWidth = 300;
                    int maxHeight = 350;
                    
                    double scale = Math.min((double) maxWidth / image.getWidth(),
                                          (double) maxHeight / image.getHeight());
                    
                    int scaledWidth = (int) (image.getWidth() * scale);
                    int scaledHeight = (int) (image.getHeight() * scale);
                    
                    Image scaledImage = image.getScaledInstance(scaledWidth, scaledHeight, Image.SCALE_SMOOTH);
                    imageLabel.setIcon(new ImageIcon(scaledImage));
                    imageLabel.setText("");
                    
                    processButton.setEnabled(true);
                    
                    // Clear previous results
                    clearResults();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Failed to load image: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void processPrescription() {
        if (selectedImage == null) {
            JOptionPane.showMessageDialog(this, "Please select an image first", "No Image", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Show loading indicator (Requirement 9.7)
        processButton.setEnabled(false);
        processButton.setText("Processing...");
        
        SwingWorker<OCRService.OCRResult, Void> worker = new SwingWorker<OCRService.OCRResult, Void>() {
            @Override
            protected OCRService.OCRResult doInBackground() throws Exception {
                return ocrService.extractText(selectedImage);
            }
            
            @Override
            protected void done() {
                try {
                    OCRService.OCRResult result = get();
                    displayResults(result);
                    
                } catch (Exception e) {
                    String message;
                    if (e.getCause() instanceof OCRException) {
                        message = e.getCause().getMessage();
                    } else {
                        message = "OCR processing failed. Please ensure Tesseract is installed and configured properly.";
                    }
                    
                    JOptionPane.showMessageDialog(PrescriptionPanel.this,
                        message, "OCR Error", JOptionPane.ERROR_MESSAGE);
                    
                } finally {
                    processButton.setEnabled(true);
                    processButton.setText("Process Prescription");
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayResults(OCRService.OCRResult result) {
        // Display matched medicines
        DefaultTableModel tableModel = (DefaultTableModel) matchedMedicinesTable.getModel();
        tableModel.setRowCount(0);
        
        for (Medicine medicine : result.getMatchedMedicines()) {
            tableModel.addRow(new Object[]{
                medicine.getMedicineName(),
                medicine.getGenericName(),
                medicine.getSellingPrice(),
                medicine.isPrescriptionRequired() ? "Yes" : "No"
            });
        }
        
        // Display unmatched text
        unmatchedTextArea.setText(String.join("\n", result.getUnmatchedText()));
        
        // Display raw text
        rawTextArea.setText(result.getRawText());
        
        // Show summary message
        String message = String.format("OCR completed!\n\nFound %d medicines in database\n%d items could not be matched",
                                     result.getMatchedMedicines().size(),
                                     result.getUnmatchedText().size());
        
        JOptionPane.showMessageDialog(this, message, "OCR Results", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void clearResults() {
        DefaultTableModel tableModel = (DefaultTableModel) matchedMedicinesTable.getModel();
        tableModel.setRowCount(0);
        unmatchedTextArea.setText("");
        rawTextArea.setText("");
    }
}