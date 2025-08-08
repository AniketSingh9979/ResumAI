package com.resumai.mailservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.resumai.mailservice.dto.MailRequest;
import com.resumai.mailservice.dto.TestLinkRequest;

/**
 * Service class for handling mail operations
 */
@Service
public class MailService {
    
    private static final Logger logger = LoggerFactory.getLogger(MailService.class);
    
    @Autowired
    private JavaMailSender javaMailSender;
    
    @Value("${spring.mail.from:mail.ctstest@gmail.com}")
    private String fromEmail;
    
    private boolean isMailConfigured() {
        try {
            // Test if mail is properly configured
            SimpleMailMessage testMessage = new SimpleMailMessage();
            testMessage.setFrom(fromEmail);
            testMessage.setTo("test@example.com");
            testMessage.setSubject("Test");
            testMessage.setText("Test");
            return true;
        } catch (Exception e) {
            logger.error("Mail configuration test failed", e);
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
        message.setFrom(fromEmail);
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
        message.setFrom(fromEmail);
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
    
    /**
     * Send test link to candidate via email
     * @param testLinkRequest Contains candidate details and test link
     */
    public void sendTestLinkToCandidate(TestLinkRequest testLinkRequest) {
        try {
            logger.info("Sending test link to candidate: {}", testLinkRequest.getCandidate().getName());
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(testLinkRequest.getCandidate().getEmail());
            message.setSubject("Interview Test - ResumAI");
            
            String emailBody = String.format(
                "Dear %s,\n\n" +
                "We are pleased to invite you to take our online interview test.\n\n" +
                "Please click on the link below to start your test:\n" +
                "%s\n\n" +
                "Important Instructions:\n" +
                "- Make sure you have a stable internet connection\n" +
                "- Complete the test in one sitting\n" +
                "- Ensure you have adequate time before starting\n" +
                "- Contact us if you face any technical issues\n\n" +
                "Best of luck!\n\n" +
                "Best regards,\n" +
                "ResumAI Team",
                testLinkRequest.getCandidate().getName(),
                testLinkRequest.getTestLink()
            );
            
            message.setText(emailBody);
            
            javaMailSender.send(message);
            logger.info("Test link email sent successfully to candidate: {}", testLinkRequest.getCandidate().getEmail());
            
        } catch (Exception e) {
            logger.error("Failed to send test link email to candidate: {} - Error: {}", 
                        testLinkRequest.getCandidate().getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send test link email: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }
    }
    
    /**
     * Send a test email to verify mail configuration
     * @param email Target email address
     * @return Result message
     */
    public String sendTestEmail(String email) {
        try {
            logger.info("Sending test email to: {}", email);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("ResumAI Mail Service Test");
            message.setText("This is a test email from ResumAI mail service. If you receive this, the mail configuration is working correctly.\n\nFrom: " + fromEmail + "\nTime: " + java.time.LocalDateTime.now());
            
            javaMailSender.send(message);
            
            logger.info("Test email sent successfully to: {}", email);
            return "Test email sent successfully to: " + email;
            
        } catch (Exception e) {
            logger.error("Failed to send test email to: {} - Error: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send test email: " + e.getClass().getSimpleName() + " - " + e.getMessage(), e);
        }
    }
} 