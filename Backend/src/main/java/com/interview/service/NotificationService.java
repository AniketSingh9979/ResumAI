package com.interview.service;

import com.interview.client.MailServiceClient;
import com.interview.client.MailServiceClient.InterviewResultMailRequest;
import com.interview.entity.InterviewResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Service for handling notifications via external services
 * Currently supports email notifications through mail-service
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private static final Float PASSING_THRESHOLD = 60.0f;
    
    @Autowired(required = false)
    private MailServiceClient mailServiceClient;
    
    /**
     * Send interview result notification email to candidate and recruiter
     * @param result Complete interview result data
     */
    public void sendInterviewResultNotification(InterviewResult result) {
        try {
            logger.info("Preparing to send interview result notification for candidate: {}", result.getCandidateName());
            
            // Check if mail service client is available
            if (mailServiceClient == null) {
                logger.warn("Mail service client not available. Email notification skipped for candidate: {}", 
                           result.getCandidateName());
                return;
            }
            
            // Calculate overall score and pass/fail status
            Float overallScore = result.calculateOverallScore();
            Boolean passed = overallScore != null && overallScore >= PASSING_THRESHOLD;
            
            // Create mail request with all interview details
            InterviewResultMailRequest mailRequest = new InterviewResultMailRequest(
                result.getCandidateName(),
                result.getCandidateEmail(),
                result.getDomain(),
                result.getExperienceLevel(),
                overallScore,
                passed
            );
            
            // Set performance rating
            mailRequest.setPerformanceRating(result.getPerformanceRating());
            
            // Set section-specific data
            mailRequest.setCodingQuestions(result.getCodingQuestions());
            mailRequest.setMcqQuestions(result.getMcqQuestions());
            mailRequest.setSubjectiveQuestions(result.getSubjectiveQuestions());
            mailRequest.setCodingScore(result.getCodingScore());
            mailRequest.setMcqScore(result.getMcqScore());
            mailRequest.setSubjectiveScore(result.getSubjectiveScore());
            
            // Set additional interview details
            mailRequest.setTotalQuestions(result.getTotalQuestions());
            mailRequest.setCorrectAnswers(result.getCorrectAnswers());
            mailRequest.setInterviewDuration(result.getInterviewDuration());
            mailRequest.setCompletedOnTime(result.getCompletedOnTime());
            mailRequest.setFeedbackSummary(result.getFeedbackSummary());
            
            // Send notification via mail service
            var response = mailServiceClient.sendInterviewResultNotification(mailRequest);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Successfully sent interview result notification for candidate: {} - Status: {} (Score: {}%)", 
                           result.getCandidateName(), passed ? "PASSED" : "FAILED", overallScore);
            } else {
                logger.warn("Mail service returned non-success status: {} for candidate: {}", 
                           response.getStatusCode(), result.getCandidateName());
            }
            
        } catch (Exception e) {
            logger.error("Failed to send interview result notification for candidate: {} - Error: {}", 
                        result.getCandidateName(), e.getMessage(), e);
            
            // Don't throw exception to avoid breaking the main interview submission flow
            // The interview result is still saved even if email fails
            logger.warn("Interview result was saved successfully, but email notification failed. " +
                       "Manual notification may be required for candidate: {}", result.getCandidateName());
        }
    }
    
    /**
     * Check if mail service is available
     * @return true if mail service is reachable, false otherwise
     */
    public boolean isMailServiceAvailable() {
        try {
            if (mailServiceClient == null) {
                logger.debug("Mail service client not configured");
                return false;
            }
            var response = mailServiceClient.checkHealth();
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.warn("Mail service health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the passing threshold for interviews
     * @return passing score threshold (60%)
     */
    public Float getPassingThreshold() {
        return PASSING_THRESHOLD;
    }
} 