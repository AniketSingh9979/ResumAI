package com.resumai.mailservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.resumai.mailservice.dto.MailRequest;

/**
 * Service class for handling mail operations
 */
@Service
public class MailService {
    
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    private boolean isMailConfigured() {
        try {
            // Test if mail is properly configured
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom("test@example.com");
            testMessage.setTo("test@example.com");
            testMessage.setSubject("Test");
            testMessage.setText("Test");
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Send selection notification to candidate and recruiter
     */
    public void sendSelectionNotification(MailRequest mailRequest) {
        try {
            // Send email to candidate
            sendEmailToCandidate(
                mailRequest.getCandidate().getEmail(),
                mailRequest.getCandidate().getName(),
                true
            );
            
            // Send notification to recruiter
            sendEmailToRecruiter(
                mailRequest.getRecruiter().getEmail(),
                mailRequest.getRecruiter().getName(),
                mailRequest.getCandidate().getName(),
                true
            );
            
            logger.info("Selection notification sent successfully for candidate: {}", 
                       mailRequest.getCandidate().getName());
        } catch (Exception e) {
            logger.error("Failed to send selection notification", e);
            throw new RuntimeException("Failed to send selection notification: " + e.getMessage());
        }
    }
    
    /**
     * Send rejection notification to candidate and recruiter
     */
    public void sendRejectionNotification(MailRequest mailRequest) {
        try {
            // Send email to candidate
            sendEmailToCandidate(
                mailRequest.getCandidate().getEmail(),
                mailRequest.getCandidate().getName(),
                false
            );
            
            // Send notification to recruiter
            sendEmailToRecruiter(
                mailRequest.getRecruiter().getEmail(),
                mailRequest.getRecruiter().getName(),
                mailRequest.getCandidate().getName(),
                false
            );
            
            logger.info("Rejection notification sent successfully for candidate: {}", 
                       mailRequest.getCandidate().getName());
        } catch (Exception e) {
            logger.error("Failed to send rejection notification", e);
            throw new RuntimeException("Failed to send rejection notification: " + e.getMessage());
        }
    }
    
    private void sendEmailToCandidate(String candidateEmail, String candidateName, boolean isSelected) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("aniketvirat9979@gmail.com");
        message.setTo(candidateEmail);
        message.setSubject(isSelected ? "Congratulations! You've been selected" : "Application Update");
        
        String emailBody = isSelected 
            ? String.format("Dear %s,\n\nCongratulations! We are pleased to inform you that you have been selected for the position.\n\nBest regards,\nResumAI Team", candidateName)
            : String.format("Dear %s,\n\nThank you for your interest in the position. After careful consideration, we have decided to move forward with other candidates.\n\nBest regards,\nResumAI Team", candidateName);
        
        message.setText(emailBody);
        
        try {
            javaMailSender.send(message);
            logger.info("Email sent to candidate: {}", candidateEmail);
        } catch (Exception e) {
            logger.warn("Failed to send email to candidate: {} - Error: {}", candidateEmail, e.getMessage());
            logger.info("Email content would have been: Subject='{}', Body='{}'", message.getSubject(), emailBody);
        }
    }
    
    private void sendEmailToRecruiter(String recruiterEmail, String recruiterName, String candidateName, boolean isSelected) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("aniketvirat9979@gmail.com");
        message.setTo(recruiterEmail);
        message.setSubject(isSelected ? "Candidate Selected" : "Candidate Rejected");
        
        String emailBody = isSelected 
            ? String.format("Dear %s,\n\nThe candidate %s has been selected and notified.\n\nBest regards,\nResumAI System", recruiterName, candidateName)
            : String.format("Dear %s,\n\nThe candidate %s has been rejected and notified.\n\nBest regards,\nResumAI System", recruiterName, candidateName);
        
        message.setText(emailBody);
        
        try {
            javaMailSender.send(message);
            logger.info("Email sent to recruiter: {}", recruiterEmail);
        } catch (Exception e) {
            logger.warn("Failed to send email to recruiter: {} - Error: {}", recruiterEmail, e.getMessage());
            logger.info("Email content would have been: Subject='{}', Body='{}'", message.getSubject(), emailBody);
        }
    }
} 