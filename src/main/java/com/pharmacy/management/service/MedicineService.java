package com.pharmacy.management.service;

import com.pharmacy.management.dao.MedicineDAO;
import com.pharmacy.management.model.Medicine;
import com.pharmacy.management.model.Session;
import com.pharmacy.management.model.UserRole;
import com.pharmacy.management.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

/**
 * Medicine service layer.
 * Requirements: 5.2, 5.5, 6.1, 6.3
 */
public class MedicineService {
    
    private static final Logger logger = LoggerFactory.getLogger(MedicineService.class);
    private final MedicineDAO medicineDAO;
    
    public MedicineService() {
        this.medicineDAO = new MedicineDAO();
    }
    
    public MedicineService(MedicineDAO medicineDAO) {
        this.medicineDAO = medicineDAO;
    }
    
    public void addMedicine(Medicine medicine) throws ValidationException, ServiceException {
        try {
            ValidationUtil.validateMedicine(medicine);
            medicineDAO.insert(medicine);
            logger.info("Medicine added: {}", medicine.getMedicineName());
        } catch (SQLException e) {
            logger.error("Failed to add medicine", e);
            throw new ServiceException("Failed to add medicine: " + e.getMessage());
        }
    }
    
    public Medicine getMedicineById(int medicineId) throws ServiceException {
        try {
            return medicineDAO.findById(medicineId);
        } catch (SQLException e) {
            logger.error("Failed to get medicine by ID: {}", medicineId, e);
            throw new ServiceException("Failed to retrieve medicine");
        }
    }
    
    public List<Medicine> getAllMedicines() throws ServiceException {
        try {
            return medicineDAO.findAll();
        } catch (SQLException e) {
            logger.error("Failed to get all medicines", e);
            throw new ServiceException("Failed to retrieve medicines");
        }
    }
    
    public List<Medicine> searchMedicines(String searchTerm) throws ServiceException {
        try {
            return medicineDAO.search(searchTerm);
        } catch (SQLException e) {
            logger.error("Failed to search medicines", e);
            throw new ServiceException("Failed to search medicines");
        }
    }
    
    public void updateMedicine(Medicine medicine) throws ValidationException, ServiceException {
        try {
            ValidationUtil.validateMedicine(medicine);
            medicineDAO.update(medicine);
            logger.info("Medicine updated: {}", medicine.getMedicineName());
        } catch (SQLException e) {
            logger.error("Failed to update medicine", e);
            throw new ServiceException("Failed to update medicine");
        }
    }
    
    public void deleteMedicine(int medicineId) throws AuthorizationException, ServiceException {
        Session session = Session.getInstance();
        if (session.getRole() != UserRole.ADMIN) {
            throw new AuthorizationException("Only administrators can delete medicines");
        }
        
        try {
            medicineDAO.delete(medicineId);
            logger.info("Medicine deleted: {}", medicineId);
        } catch (SQLException e) {
            logger.error("Failed to delete medicine", e);
            throw new ServiceException("Failed to delete medicine");
        }
    }
}