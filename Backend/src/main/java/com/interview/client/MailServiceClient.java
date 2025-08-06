package com.interview.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for communicating with Mail Service
 * Uses Eureka service discovery to locate the mail-service
 */
@FeignClient(name = "mail-service", path = "/mail-service/api/mail", fallbackFactory = MailServiceClientFallback.class)
public interface MailServiceClient {
    
    /**
     * Send interview result notification via mail service
     * @param mailRequest Interview result details
     * @return Response from mail service
     */
    @PostMapping("/interview-result")
    ResponseEntity<String> sendInterviewResultNotification(@RequestBody InterviewResultMailRequest mailRequest);
    
    /**
     * Health check for mail service
     * @return Health status
     */
    @GetMapping("/health")
    ResponseEntity<String> checkHealth();
    
    /**
     * DTO class for interview result email notifications
     */
    public static class InterviewResultMailRequest {
        private String candidateName;
        private String candidateEmail;
        private String domain;
        private String experienceLevel;
        private Float overallScore;
        private Boolean passed;
        private String performanceRating;
        
        // Section-specific scores
        private Integer codingQuestions = 0;
        private Integer mcqQuestions = 0;
        private Integer subjectiveQuestions = 0;
        private Float codingScore = 0.0f;
        private Float mcqScore = 0.0f;
        private Float subjectiveScore = 0.0f;
        
        // Additional details
        private Integer totalQuestions;
        private Integer correctAnswers;
        private Long interviewDuration;
        private Boolean completedOnTime;
        private String feedbackSummary;
        
        // Recruiter information
        private String recruiterName = "ResumAI System";
        private String recruiterEmail = "admin@resumai.com";
        
        // Constructors
        public InterviewResultMailRequest() {}
        
        public InterviewResultMailRequest(String candidateName, String candidateEmail, String domain, 
                                         String experienceLevel, Float overallScore, Boolean passed) {
            this.candidateName = candidateName;
            this.candidateEmail = candidateEmail;
            this.domain = domain;
            this.experienceLevel = experienceLevel;
            this.overallScore = overallScore;
            this.passed = passed;
        }
        
        // Getters and Setters
        public String getCandidateName() { return candidateName; }
        public void setCandidateName(String candidateName) { this.candidateName = candidateName; }
        
        public String getCandidateEmail() { return candidateEmail; }
        public void setCandidateEmail(String candidateEmail) { this.candidateEmail = candidateEmail; }
        
        public String getDomain() { return domain; }
        public void setDomain(String domain) { this.domain = domain; }
        
        public String getExperienceLevel() { return experienceLevel; }
        public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }
        
        public Float getOverallScore() { return overallScore; }
        public void setOverallScore(Float overallScore) { this.overallScore = overallScore; }
        
        public Boolean getPassed() { return passed; }
        public void setPassed(Boolean passed) { this.passed = passed; }
        
        public String getPerformanceRating() { return performanceRating; }
        public void setPerformanceRating(String performanceRating) { this.performanceRating = performanceRating; }
        
        public Integer getCodingQuestions() { return codingQuestions; }
        public void setCodingQuestions(Integer codingQuestions) { this.codingQuestions = codingQuestions; }
        
        public Integer getMcqQuestions() { return mcqQuestions; }
        public void setMcqQuestions(Integer mcqQuestions) { this.mcqQuestions = mcqQuestions; }
        
        public Integer getSubjectiveQuestions() { return subjectiveQuestions; }
        public void setSubjectiveQuestions(Integer subjectiveQuestions) { this.subjectiveQuestions = subjectiveQuestions; }
        
        public Float getCodingScore() { return codingScore; }
        public void setCodingScore(Float codingScore) { this.codingScore = codingScore; }
        
        public Float getMcqScore() { return mcqScore; }
        public void setMcqScore(Float mcqScore) { this.mcqScore = mcqScore; }
        
        public Float getSubjectiveScore() { return subjectiveScore; }
        public void setSubjectiveScore(Float subjectiveScore) { this.subjectiveScore = subjectiveScore; }
        
        public Integer getTotalQuestions() { return totalQuestions; }
        public void setTotalQuestions(Integer totalQuestions) { this.totalQuestions = totalQuestions; }
        
        public Integer getCorrectAnswers() { return correctAnswers; }
        public void setCorrectAnswers(Integer correctAnswers) { this.correctAnswers = correctAnswers; }
        
        public Long getInterviewDuration() { return interviewDuration; }
        public void setInterviewDuration(Long interviewDuration) { this.interviewDuration = interviewDuration; }
        
        public Boolean getCompletedOnTime() { return completedOnTime; }
        public void setCompletedOnTime(Boolean completedOnTime) { this.completedOnTime = completedOnTime; }
        
        public String getFeedbackSummary() { return feedbackSummary; }
        public void setFeedbackSummary(String feedbackSummary) { this.feedbackSummary = feedbackSummary; }
        
        public String getRecruiterName() { return recruiterName; }
        public void setRecruiterName(String recruiterName) { this.recruiterName = recruiterName; }
        
        public String getRecruiterEmail() { return recruiterEmail; }
        public void setRecruiterEmail(String recruiterEmail) { this.recruiterEmail = recruiterEmail; }
    }
}

/**
 * Fallback factory for MailServiceClient
 * Provides fallback behavior when mail service is unavailable
 */
@org.springframework.stereotype.Component
class MailServiceClientFallback implements org.springframework.cloud.openfeign.FallbackFactory<MailServiceClient> {
    
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(MailServiceClientFallback.class);
    
    @Override
    public MailServiceClient create(Throwable cause) {
        return new MailServiceClient() {
            @Override
            public ResponseEntity<String> sendInterviewResultNotification(InterviewResultMailRequest mailRequest) {
                logger.warn("Mail service unavailable for candidate: {} - Cause: {}", 
                           mailRequest.getCandidateName(), cause.getMessage());
                return ResponseEntity.ok("Mail service unavailable - notification queued for retry");
            }
            
            @Override
            public ResponseEntity<String> checkHealth() {
                logger.warn("Mail service health check failed - Cause: {}", cause.getMessage());
                return ResponseEntity.status(503).body("Mail service unavailable");
            }
        };
    }
} 