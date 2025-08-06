package com.resumai.resumeparserservice.service;

import com.resumai.resumeparserservice.dto.PanelMemberRequest;
import com.resumai.resumeparserservice.dto.PanelMemberResponse;
import com.resumai.resumeparserservice.entity.PanelMember;
import com.resumai.resumeparserservice.repository.PanelMemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class PanelMemberService {
    
    private static final Logger logger = LoggerFactory.getLogger(PanelMemberService.class);
    
    @Autowired
    private PanelMemberRepository panelMemberRepository;
    
    /**
     * Create a new panel member
     */
    public PanelMemberResponse createPanelMember(PanelMemberRequest request) {
        logger.info("Creating panel member with email: {}", request.getEmail());
        
        try {
            // Check if email already exists
            if (panelMemberRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            
            // Check if employee ID already exists
            if (panelMemberRepository.findByEmployeeId(request.getEmployeeId()).isPresent()) {
                throw new IllegalArgumentException("Employee ID already exists: " + request.getEmployeeId());
            }
            
            // Convert DTO to Entity and save
            PanelMember panelMember = request.toEntity();
            PanelMember savedPanelMember = panelMemberRepository.save(panelMember);
            
            logger.info("Panel member created successfully with ID: {}", savedPanelMember.getId());
            return new PanelMemberResponse(savedPanelMember);
            
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while creating panel member", e);
            throw new IllegalArgumentException("Duplicate email or employee ID provided");
        } catch (Exception e) {
            logger.error("Error creating panel member", e);
            throw new RuntimeException("Failed to create panel member: " + e.getMessage());
        }
    }
    
    /**
     * Get panel member by ID
     */
    @Transactional(readOnly = true)
    public Optional<PanelMemberResponse> getPanelMemberById(Long id) {
        logger.info("Fetching panel member with ID: {}", id);
        
        Optional<PanelMember> panelMember = panelMemberRepository.findById(id);
        return panelMember.map(PanelMemberResponse::new);
    }
    
    /**
     * Get panel member by email
     */
    @Transactional(readOnly = true)
    public Optional<PanelMemberResponse> getPanelMemberByEmail(String email) {
        logger.info("Fetching panel member with email: {}", email);
        
        Optional<PanelMember> panelMember = panelMemberRepository.findByEmail(email);
        return panelMember.map(PanelMemberResponse::new);
    }
    
    /**
     * Get panel member by employee ID
     */
    @Transactional(readOnly = true)
    public Optional<PanelMemberResponse> getPanelMemberByEmployeeId(String employeeId) {
        logger.info("Fetching panel member with employee ID: {}", employeeId);
        
        Optional<PanelMember> panelMember = panelMemberRepository.findByEmployeeId(employeeId);
        return panelMember.map(PanelMemberResponse::new);
    }
    
    /**
     * Get all active panel members
     */
    @Transactional(readOnly = true)
    public List<PanelMemberResponse> getAllActivePanelMembers() {
        logger.info("Fetching all active panel members");
        
        List<PanelMember> panelMembers = panelMemberRepository.findByIsActiveTrue();
        return panelMembers.stream()
                .map(PanelMemberResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get panel members by availability status
     */
    @Transactional(readOnly = true)
    public List<PanelMemberResponse> getPanelMembersByAvailabilityStatus(PanelMember.AvailabilityStatus status) {
        logger.info("Fetching panel members with availability status: {}", status);
        
        List<PanelMember> panelMembers = panelMemberRepository.findByAvailabilityStatusAndIsActiveTrue(status);
        return panelMembers.stream()
                .map(PanelMemberResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get panel members by department
     */
    @Transactional(readOnly = true)
    public List<PanelMemberResponse> getPanelMembersByDepartment(String department) {
        logger.info("Fetching panel members from department: {}", department);
        
        List<PanelMember> panelMembers = panelMemberRepository.findByDepartmentAndIsActiveTrue(department);
        return panelMembers.stream()
                .map(PanelMemberResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Get panel members by location
     */
    @Transactional(readOnly = true)
    public List<PanelMemberResponse> getPanelMembersByLocation(String location) {
        logger.info("Fetching panel members from location: {}", location);
        
        List<PanelMember> panelMembers = panelMemberRepository.findByLocationAndIsActiveTrue(location);
        return panelMembers.stream()
                .map(PanelMemberResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Search panel members by expertise
     */
    @Transactional(readOnly = true)
    public List<PanelMemberResponse> searchPanelMembersByExpertise(String expertise) {
        logger.info("Searching panel members with expertise: {}", expertise);
        
        List<PanelMember> panelMembers = panelMemberRepository.findByExpertiseContainingIgnoreCaseAndIsActiveTrue(expertise);
        return panelMembers.stream()
                .map(PanelMemberResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Search panel members by name
     */
    @Transactional(readOnly = true)
    public List<PanelMemberResponse> searchPanelMembersByName(String name) {
        logger.info("Searching panel members with name: {}", name);
        
        List<PanelMember> panelMembers = panelMemberRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(name);
        return panelMembers.stream()
                .map(PanelMemberResponse::new)
                .collect(Collectors.toList());
    }
    
    /**
     * Update panel member
     */
    public PanelMemberResponse updatePanelMember(Long id, PanelMemberRequest request) {
        logger.info("Updating panel member with ID: {}", id);
        
        try {
            Optional<PanelMember> optionalPanelMember = panelMemberRepository.findById(id);
            if (optionalPanelMember.isEmpty()) {
                throw new IllegalArgumentException("Panel member not found with ID: " + id);
            }
            
            PanelMember existingPanelMember = optionalPanelMember.get();
            
            // Check if email is being changed and if new email already exists
            if (!existingPanelMember.getEmail().equals(request.getEmail()) &&
                panelMemberRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
                throw new IllegalArgumentException("Email already exists: " + request.getEmail());
            }
            
            // Check if employee ID is being changed and if new employee ID already exists
            if (!existingPanelMember.getEmployeeId().equals(request.getEmployeeId()) &&
                panelMemberRepository.existsByEmployeeIdAndIdNot(request.getEmployeeId(), id)) {
                throw new IllegalArgumentException("Employee ID already exists: " + request.getEmployeeId());
            }
            
            // Update entity fields
            existingPanelMember.setName(request.getName());
            existingPanelMember.setEmail(request.getEmail());
            existingPanelMember.setEmployeeId(request.getEmployeeId());
            existingPanelMember.setDesignation(request.getDesignation());
            existingPanelMember.setDepartment(request.getDepartment());
            existingPanelMember.setLocation(request.getLocation());
            existingPanelMember.setExpertise(request.getExpertise());
            existingPanelMember.setMobileNumber(request.getMobileNumber());
            existingPanelMember.setAvailabilityStatus(request.getAvailabilityStatus());
            
            PanelMember updatedPanelMember = panelMemberRepository.save(existingPanelMember);
            
            logger.info("Panel member updated successfully with ID: {}", updatedPanelMember.getId());
            return new PanelMemberResponse(updatedPanelMember);
            
        } catch (DataIntegrityViolationException e) {
            logger.error("Data integrity violation while updating panel member", e);
            throw new IllegalArgumentException("Duplicate email or employee ID provided");
        } catch (Exception e) {
            logger.error("Error updating panel member", e);
            throw new RuntimeException("Failed to update panel member: " + e.getMessage());
        }
    }
    
    /**
     * Soft delete panel member (set isActive to false)
     */
    public void deactivatePanelMember(Long id) {
        logger.info("Deactivating panel member with ID: {}", id);
        
        Optional<PanelMember> optionalPanelMember = panelMemberRepository.findById(id);
        if (optionalPanelMember.isEmpty()) {
            throw new IllegalArgumentException("Panel member not found with ID: " + id);
        }
        
        PanelMember panelMember = optionalPanelMember.get();
        panelMember.setIsActive(false);
        panelMemberRepository.save(panelMember);
        
        logger.info("Panel member deactivated successfully with ID: {}", id);
    }
    
    /**
     * Permanently delete panel member
     */
    public void deletePanelMember(Long id) {
        logger.info("Permanently deleting panel member with ID: {}", id);
        
        if (!panelMemberRepository.existsById(id)) {
            throw new IllegalArgumentException("Panel member not found with ID: " + id);
        }
        
        panelMemberRepository.deleteById(id);
        logger.info("Panel member permanently deleted with ID: {}", id);
    }
    
    /**
     * Update availability status
     */
    public PanelMemberResponse updateAvailabilityStatus(Long id, PanelMember.AvailabilityStatus status) {
        logger.info("Updating availability status for panel member ID: {} to {}", id, status);
        
        Optional<PanelMember> optionalPanelMember = panelMemberRepository.findById(id);
        if (optionalPanelMember.isEmpty()) {
            throw new IllegalArgumentException("Panel member not found with ID: " + id);
        }
        
        PanelMember panelMember = optionalPanelMember.get();
        panelMember.setAvailabilityStatus(status);
        PanelMember updatedPanelMember = panelMemberRepository.save(panelMember);
        
        logger.info("Availability status updated successfully for panel member ID: {}", id);
        return new PanelMemberResponse(updatedPanelMember);
    }
} 