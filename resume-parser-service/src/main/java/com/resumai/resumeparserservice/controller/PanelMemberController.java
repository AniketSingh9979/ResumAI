package com.resumai.resumeparserservice.controller;

import com.resumai.resumeparserservice.dto.ApiResponse;
import com.resumai.resumeparserservice.dto.PanelMemberRequest;
import com.resumai.resumeparserservice.dto.PanelMemberResponse;
import com.resumai.resumeparserservice.entity.PanelMember;
import com.resumai.resumeparserservice.service.PanelMemberService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/panel-members")
@CrossOrigin(origins = "*")
public class PanelMemberController {
    
    private static final Logger logger = LoggerFactory.getLogger(PanelMemberController.class);
    
    @Autowired
    private PanelMemberService panelMemberService;
    
    /**
     * POST /api/panel-members - Create a new panel member
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PanelMemberResponse>> createPanelMember(
            @Valid @RequestBody PanelMemberRequest request,
            BindingResult bindingResult) {
        
        logger.info("Received request to create panel member: {}", request.getName());
        
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                
                logger.warn("Validation errors for panel member creation: {}", errors);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Validation failed", errors));
            }
            
            PanelMemberResponse response = panelMemberService.createPanelMember(request);
            
            logger.info("Panel member created successfully with ID: {}", response.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Panel member created successfully", response));
                    
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument for panel member creation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
                    
        } catch (Exception e) {
            logger.error("Error creating panel member", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to create panel member"));
        }
    }
    
    /**
     * GET /api/panel-members/{id} - Get panel member by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PanelMemberResponse>> getPanelMemberById(@PathVariable Long id) {
        logger.info("Received request to get panel member with ID: {}", id);
        
        try {
            Optional<PanelMemberResponse> response = panelMemberService.getPanelMemberById(id);
            
            if (response.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Panel member found", response.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error fetching panel member with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch panel member"));
        }
    }
    
    /**
     * GET /api/panel-members - Get all active panel members with optional filters
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<PanelMemberResponse>>> getAllPanelMembers(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String expertise,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) PanelMember.AvailabilityStatus status) {
        
        logger.info("Received request to get panel members with filters - department: {}, location: {}, expertise: {}, name: {}, status: {}", 
                    department, location, expertise, name, status);
        
        try {
            List<PanelMemberResponse> responses;
            
            // Apply filters based on provided parameters
            if (department != null && !department.trim().isEmpty()) {
                responses = panelMemberService.getPanelMembersByDepartment(department.trim());
            } else if (location != null && !location.trim().isEmpty()) {
                responses = panelMemberService.getPanelMembersByLocation(location.trim());
            } else if (expertise != null && !expertise.trim().isEmpty()) {
                responses = panelMemberService.searchPanelMembersByExpertise(expertise.trim());
            } else if (name != null && !name.trim().isEmpty()) {
                responses = panelMemberService.searchPanelMembersByName(name.trim());
            } else if (status != null) {
                responses = panelMemberService.getPanelMembersByAvailabilityStatus(status);
            } else {
                responses = panelMemberService.getAllActivePanelMembers();
            }
            
            return ResponseEntity.ok(ApiResponse.success("Panel members retrieved successfully", responses));
            
        } catch (Exception e) {
            logger.error("Error fetching panel members", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch panel members"));
        }
    }
    
    /**
     * GET /api/panel-members/email/{email} - Get panel member by email
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<PanelMemberResponse>> getPanelMemberByEmail(@PathVariable String email) {
        logger.info("Received request to get panel member with email: {}", email);
        
        try {
            Optional<PanelMemberResponse> response = panelMemberService.getPanelMemberByEmail(email);
            
            if (response.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Panel member found", response.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error fetching panel member with email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch panel member"));
        }
    }
    
    /**
     * GET /api/panel-members/employee/{employeeId} - Get panel member by employee ID
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<PanelMemberResponse>> getPanelMemberByEmployeeId(@PathVariable String employeeId) {
        logger.info("Received request to get panel member with employee ID: {}", employeeId);
        
        try {
            Optional<PanelMemberResponse> response = panelMemberService.getPanelMemberByEmployeeId(employeeId);
            
            if (response.isPresent()) {
                return ResponseEntity.ok(ApiResponse.success("Panel member found", response.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            logger.error("Error fetching panel member with employee ID: {}", employeeId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to fetch panel member"));
        }
    }
    
    /**
     * PUT /api/panel-members/{id} - Update panel member
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PanelMemberResponse>> updatePanelMember(
            @PathVariable Long id,
            @Valid @RequestBody PanelMemberRequest request,
            BindingResult bindingResult) {
        
        logger.info("Received request to update panel member with ID: {}", id);
        
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                List<String> errors = bindingResult.getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.toList());
                
                logger.warn("Validation errors for panel member update: {}", errors);
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Validation failed", errors));
            }
            
            PanelMemberResponse response = panelMemberService.updatePanelMember(id, request);
            
            logger.info("Panel member updated successfully with ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Panel member updated successfully", response));
                    
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument for panel member update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
                    
        } catch (Exception e) {
            logger.error("Error updating panel member with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update panel member"));
        }
    }
    
    /**
     * PATCH /api/panel-members/{id}/status - Update availability status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PanelMemberResponse>> updateAvailabilityStatus(
            @PathVariable Long id,
            @RequestParam PanelMember.AvailabilityStatus status) {
        
        logger.info("Received request to update availability status for panel member ID: {} to {}", id, status);
        
        try {
            PanelMemberResponse response = panelMemberService.updateAvailabilityStatus(id, status);
            
            logger.info("Availability status updated successfully for panel member ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Availability status updated successfully", response));
                    
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument for availability status update: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
                    
        } catch (Exception e) {
            logger.error("Error updating availability status for panel member ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to update availability status"));
        }
    }
    
    /**
     * DELETE /api/panel-members/{id}/deactivate - Soft delete (deactivate) panel member
     */
    @DeleteMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivatePanelMember(@PathVariable Long id) {
        logger.info("Received request to deactivate panel member with ID: {}", id);
        
        try {
            panelMemberService.deactivatePanelMember(id);
            
            logger.info("Panel member deactivated successfully with ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Panel member deactivated successfully"));
                    
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument for panel member deactivation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
                    
        } catch (Exception e) {
            logger.error("Error deactivating panel member with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to deactivate panel member"));
        }
    }
    
    /**
     * DELETE /api/panel-members/{id} - Permanently delete panel member
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePanelMember(@PathVariable Long id) {
        logger.info("Received request to permanently delete panel member with ID: {}", id);
        
        try {
            panelMemberService.deletePanelMember(id);
            
            logger.info("Panel member permanently deleted with ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success("Panel member deleted successfully"));
                    
        } catch (IllegalArgumentException e) {
            logger.error("Invalid argument for panel member deletion: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
                    
        } catch (Exception e) {
            logger.error("Error deleting panel member with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to delete panel member"));
        }
    }
} 