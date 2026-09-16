package com.pharmacy.management.service;

import com.pharmacy.management.dao.MedicineDAO;
import com.pharmacy.management.model.Medicine;
import com.pharmacy.management.util.ImageUtil;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OCR service for prescription processing.
 * Requirements: 4.1, 4.2, 4.3, 4.6, 13.1, 13.2, 13.3
 */
public class OCRService {
    
    private static final Logger logger = LoggerFactory.getLogger(OCRService.class);
    private final Tesseract tesseract;
    private final MedicineDAO medicineDAO;
    
    // Common medicine name patterns
    private static final Pattern[] MEDICINE_PATTERNS = {
        Pattern.compile("\\b[A-Z][a-z]+(?:cillin|mycin|prazole|ine|ol|ide)\\b"),
        Pattern.compile("\\b(?:Paracetamol|Ibuprofen|Aspirin|Cetirizine|Omeprazole|Metformin)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\b[A-Z][a-z]{4,}\\s*\\d+\\s*mg\\b")
    };
    
    public OCRService() {
        this.medicineDAO = new MedicineDAO();
        this.tesseract = new Tesseract();
        initializeTesseract();
    }
    
    public OCRService(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO;
        this.tesseract = new Tesseract();
        initializeTesseract();
    }
    
    private void initializeTesseract() {
        try {
            // Set Tesseract data path (requires Tesseract installation)
            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata");
            tesseract.setLanguage("eng");
            tesseract.setPageSegMode(1);
            tesseract.setOcrEngineMode(1);
            logger.info("Tesseract OCR initialized successfully");
        } catch (Exception e) {
            logger.warn("Tesseract OCR initialization failed: {}", e.getMessage());
        }
    }
    
    /**
     * Extract text from prescription image.
     * Requirements 4.1, 4.2, 4.3
     */
    public OCRResult extractText(File imageFile) throws OCRException {
        try {
            logger.info("Processing prescription image: {}", imageFile.getName());
            
            // Preprocess image for better OCR accuracy (Requirement 4.2)
            BufferedImage processedImage = ImageUtil.preprocessForOCR(imageFile);
            
            // Extract text using Tesseract
            String rawText = tesseract.doOCR(processedImage);
            
            if (rawText == null || rawText.trim().isEmpty()) {
                throw new OCRException("No text could be extracted from the image");
            }
            
            // Identify medicine names from extracted text (Requirement 4.3)
            List<String> identifiedMedicines = identifyMedicineNames(rawText);
            
            // Find matches in database
            List<Medicine> matchedMedicines = new ArrayList<>();
            List<String> unmatchedText = new ArrayList<>();
            
            for (String medicineName : identifiedMedicines) {
                try {
                    List<Medicine> matches = medicineDAO.search(medicineName);
                    if (!matches.isEmpty()) {
                        matchedMedicines.addAll(matches);
                    } else {
                        unmatchedText.add(medicineName);
                    }
                } catch (SQLException e) {
                    logger.warn("Database search failed for: {}", medicineName, e);
                    unmatchedText.add(medicineName);
                }
            }
            
            logger.info("OCR completed: {} medicines identified, {} matched in database", 
                       identifiedMedicines.size(), matchedMedicines.size());
            
            return new OCRResult(rawText, identifiedMedicines, matchedMedicines, unmatchedText);
            
        } catch (TesseractException e) {
            logger.error("Tesseract OCR failed", e);
            throw new OCRException("OCR processing failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Image processing failed", e);
            throw new OCRException("Image processing failed: " + e.getMessage());
        }
    }
    
    /**
     * Identify medicine names using pattern matching.
     * Requirement 4.3
     */
    public List<String> identifyMedicineNames(String text) {
        List<String> medicines = new ArrayList<>();
        
        // Handle null or empty text
        if (text == null || text.trim().isEmpty()) {
            return medicines;
        }
        
        // Split text into lines and words
        String[] lines = text.split("\\n");
        
        for (String line : lines) {
            // Apply medicine name patterns
            for (Pattern pattern : MEDICINE_PATTERNS) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String medicine = matcher.group().trim();
                    if (!medicines.contains(medicine) && medicine.length() > 2) {
                        medicines.add(medicine);
                    }
                }
            }
        }
        
        return medicines;
    }
    
    /**
     * OCR result container class.
     */
    public static class OCRResult {
        private final String rawText;
        private final List<String> identifiedMedicines;
        private final List<Medicine> matchedMedicines;
        private final List<String> unmatchedText;
        
        public OCRResult(String rawText, List<String> identifiedMedicines, 
                        List<Medicine> matchedMedicines, List<String> unmatchedText) {
            this.rawText = rawText;
            this.identifiedMedicines = identifiedMedicines;
            this.matchedMedicines = matchedMedicines;
            this.unmatchedText = unmatchedText;
        }
        
        public String getRawText() { return rawText; }
        public List<String> getIdentifiedMedicines() { return identifiedMedicines; }
        public List<Medicine> getMatchedMedicines() { return matchedMedicines; }
        public List<String> getUnmatchedText() { return unmatchedText; }
    }
}