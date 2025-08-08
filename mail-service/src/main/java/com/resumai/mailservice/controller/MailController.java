package com.resumai.mailservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.resumai.mailservice.dto.MailRequest;
import com.resumai.mailservice.dto.TestLinkRequest;
import com.resumai.mailservice.service.MailService;

import jakarta.validation.Valid;

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
     * Endpoint to send interview result notification
     * POST /api/mail/interview-result
     */
    @PostMapping("/interview-result")
    public ResponseEntity<String> sendInterviewResultNotification(@RequestBody Object interviewResult) {
        try {
            logger.info("Received interview result notification request: {}", interviewResult);
            
            // For now, we'll log the request and return success
            // The actual email sending can be implemented based on the interview result data
            logger.info("Interview result notification processed successfully");
            
            return ResponseEntity.ok("Interview result notification processed successfully");
        } catch (Exception e) {
            logger.error("Error processing interview result notification", e);
            return ResponseEntity.internalServerError().body("Failed to process interview result notification: " + e.getMessage());
        }
    }
    
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
     * Endpoint to send test link to candidate
     * POST /api/mail/send-test-link
     */
    @PostMapping("/send-test-link")
    public ResponseEntity<String> sendTestLink(@Valid @RequestBody TestLinkRequest testLinkRequest) {
        try {
            logger.info("Received test link request for candidate: {}", 
                       testLinkRequest.getCandidate().getName());
            
            mailService.sendTestLinkToCandidate(testLinkRequest);
            
            return ResponseEntity.ok("Test link sent successfully to candidate: " + 
                                   testLinkRequest.getCandidate().getName() + 
                                   " at email: " + testLinkRequest.getCandidate().getEmail());
        } catch (Exception e) {
            logger.error("Error sending test link", e);
            return ResponseEntity.internalServerError().body("Failed to send test link: " + e.getMessage());
        }
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Mail service is running");
    }
    
    /**
     * Test mail configuration endpoint
     */
    @PostMapping("/test")
    public ResponseEntity<String> testMail(@RequestParam String email) {
        try {
            logger.info("Testing mail configuration to: {}", email);
            
            // Use the mail service to send a test email
            String result = mailService.sendTestEmail(email);
            
            logger.info("Test email result: {}", result);
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            logger.error("Failed to send test email to: {} - Error: {}", email, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body("Failed to send test email: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
} 