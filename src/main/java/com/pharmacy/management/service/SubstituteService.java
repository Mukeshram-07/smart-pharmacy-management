package com.pharmacy.management.service;

import com.pharmacy.management.dao.MedicineDAO;
import com.pharmacy.management.model.Medicine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Substitute service for finding medicine alternatives.
 * Requirements: 7.1, 7.2, 7.4
 */
public class SubstituteService {
    
    private static final Logger logger = LoggerFactory.getLogger(SubstituteService.class);
    private final MedicineDAO medicineDAO;
    
    public SubstituteService() {
        this.medicineDAO = new MedicineDAO();
    }
    
    public SubstituteService(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO;
    }
    
    /**
     * Find substitute medicines for the given medicine.
     * Substitutes are medicines with the same active ingredient, strength, and dosage form.
     * Results are sorted by selling price in ascending order (cheapest first).
     * 
     * @param medicine The original medicine to find substitutes for
     * @return List of substitute medicines sorted by price (ascending)
     * @throws ServiceException if database error occurs
     */
    public List<Medicine> findSubstitutes(Medicine medicine) throws ServiceException {
        if (medicine == null) {
            throw new IllegalArgumentException("Medicine cannot be null");
        }
        
        if (medicine.getActiveIngredient() == null || medicine.getActiveIngredient().trim().isEmpty()) {
            throw new IllegalArgumentException("Active ingredient is required to find substitutes");
        }
        
        if (medicine.getStrength() == null || medicine.getStrength().trim().isEmpty()) {
            throw new IllegalArgumentException("Strength is required to find substitutes");
        }
        
        if (medicine.getDosageForm() == null || medicine.getDosageForm().trim().isEmpty()) {
            throw new IllegalArgumentException("Dosage form is required to find substitutes");
        }
        
        try {
            List<Medicine> substitutes = medicineDAO.findSubstitutes(
                medicine.getActiveIngredient().trim(),
                medicine.getStrength().trim(),
                medicine.getDosageForm().trim(),
                medicine.getMedicineId()
            );
            
            logger.info("Found {} substitutes for medicine: {} (ID: {})", 
                       substitutes.size(), medicine.getMedicineName(), medicine.getMedicineId());
            
            return substitutes;
            
        } catch (SQLException e) {
            logger.error("Failed to find substitutes for medicine: {} (ID: {})", 
                        medicine.getMedicineName(), medicine.getMedicineId(), e);
            throw new ServiceException("Failed to find substitutes: " + e.getMessage());
        }
    }
    
    /**
     * Find substitute medicines by medicine ID.
     * Convenience method that first retrieves the medicine by ID, then finds substitutes.
     * 
     * @param medicineId The ID of the medicine to find substitutes for
     * @return List of substitute medicines sorted by price (ascending)
     * @throws ServiceException if database error occurs or medicine not found
     */
    public List<Medicine> findSubstitutesByMedicineId(int medicineId) throws ServiceException {
        try {
            Medicine medicine = medicineDAO.findById(medicineId);
            if (medicine == null) {
                throw new ServiceException("Medicine with ID " + medicineId + " not found");
            }
            
            return findSubstitutes(medicine);
            
        } catch (SQLException e) {
            logger.error("Failed to retrieve medicine with ID: {}", medicineId, e);
            throw new ServiceException("Failed to retrieve medicine: " + e.getMessage());
        }
    }
}