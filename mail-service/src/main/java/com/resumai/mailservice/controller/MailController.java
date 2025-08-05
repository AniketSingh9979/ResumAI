package com.resumai.mailservice.controller;

import com.resumai.mailservice.dto.MailRequest;
import com.resumai.mailservice.service.MailService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Mail Service operations
 */
@RestController
@RequestMapping("/api/mail")
public class MailController {
    
    private static final Logger logger = LoggerFactory.getLogger(MailController.class);
    
    @Autowired
    private MailService mailService;
    
    /**
     * Endpoint to send selection notification
     * POST /api/mail/selected
     */
    @PostMapping("/selected")
    public ResponseEntity<String> sendSelectionNotification(@Valid @RequestBody MailRequest mailRequest) {
        try {
            logger.info("Received selection notification request for candidate: {}", 
                       mailRequest.getCandidate().getName());
            
            mailService.sendSelectionNotification(mailRequest);
            
            return ResponseEntity.ok("Selection notification sent successfully to candidate: " + 
                                   mailRequest.getCandidate().getName() + " and recruiter: " + 
                                   mailRequest.getRecruiter().getName());
        } catch (Exception e) {
            logger.error("Error sending selection notification", e);
            return ResponseEntity.internalServerError().body("Failed to send selection notification: " + e.getMessage());
        }
    }
    
    /**
     * Endpoint to send rejection notification  
     * POST /api/mail/not-selected
     */
    @PostMapping("/not-selected")
    public ResponseEntity<String> sendRejectionNotification(@Valid @RequestBody MailRequest mailRequest) {
        try {
            logger.info("Received rejection notification request for candidate: {}", 
                       mailRequest.getCandidate().getName());
            
            mailService.sendRejectionNotification(mailRequest);
            
            return ResponseEntity.ok("Rejection notification sent successfully to candidate: " + 
                                   mailRequest.getCandidate().getName() + " and recruiter: " + 
                                   mailRequest.getRecruiter().getName());
        } catch (Exception e) {
            logger.error("Error sending rejection notification", e);
            return ResponseEntity.internalServerError().body("Failed to send rejection notification: " + e.getMessage());
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Mail service is running");
    }
} 