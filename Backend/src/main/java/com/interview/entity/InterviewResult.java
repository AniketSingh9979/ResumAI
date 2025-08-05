package com.interview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing complete interview results
 * Stores overall performance metrics and candidate information
 */
@Entity
@Table(name = "interview_results")
public class InterviewResult {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "result_id")
    private UUID resultId;
    
    @Column(name = "resume_id", nullable = false, length = 100)
    private String resumeId;
    
    @Column(name = "candidate_name", nullable = false, length = 200)
    private String candidateName;
    
    @Column(name = "candidate_email", length = 200)
    private String candidateEmail;
    
    @Column(name = "domain", nullable = false, length = 100)
    private String domain;
    
    @Column(name = "experience_level", nullable = false, length = 50)
    private String experienceLevel;
    
    @Column(name = "total_questions")
    private Integer totalQuestions;
    
    @Column(name = "coding_questions")
    private Integer codingQuestions = 0;
    
    @Column(name = "mcq_questions")
    private Integer mcqQuestions = 0;
    
    @Column(name = "subjective_questions")
    private Integer subjectiveQuestions = 0;
    
    @Column(name = "correct_answers")
    private Integer correctAnswers;
    
    @Column(name = "score_percentage", precision = 5)
    private Float scorePercentage;
    
    @Column(name = "coding_score", precision = 5)
    private Float codingScore = 0.0f;
    
    @Column(name = "mcq_score", precision = 5)
    private Float mcqScore = 0.0f;
    
    @Column(name = "subjective_score", precision = 5)
    private Float subjectiveScore = 0.0f;
    
    @Column(name = "feedback_summary", columnDefinition = "TEXT")
    private String feedbackSummary;
    
    @Column(name = "interview_time")
    private LocalDateTime interviewTime;
    
    @Column(name = "interview_duration")
    private Long interviewDuration; // in minutes
    
    @Column(name = "completed_on_time")
    private Boolean completedOnTime = true;
    
    @Column(name = "status", length = 50)
    private String status = "COMPLETED";
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "session_metadata", columnDefinition = "TEXT")
    private String sessionMetadata;

    // Constructors
    public InterviewResult() {
        this.interviewTime = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public InterviewResult(String resumeId, String candidateName, String domain, String experienceLevel) {
        this();
        this.resumeId = resumeId;
        this.candidateName = candidateName;
        this.domain = domain;
        this.experienceLevel = experienceLevel;
    }

    // Getter and Setter methods
    public UUID getResultId() {
        return resultId;
    }

    public void setResultId(UUID resultId) {
        this.resultId = resultId;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public void setCandidateName(String candidateName) {
        this.candidateName = candidateName;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }

    public void setCandidateEmail(String candidateEmail) {
        this.candidateEmail = candidateEmail;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCodingQuestions() {
        return codingQuestions;
    }

    public void setCodingQuestions(Integer codingQuestions) {
        this.codingQuestions = codingQuestions;
    }

    public Integer getMcqQuestions() {
        return mcqQuestions;
    }

    public void setMcqQuestions(Integer mcqQuestions) {
        this.mcqQuestions = mcqQuestions;
    }

    public Integer getSubjectiveQuestions() {
        return subjectiveQuestions;
    }

    public void setSubjectiveQuestions(Integer subjectiveQuestions) {
        this.subjectiveQuestions = subjectiveQuestions;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public Float getScorePercentage() {
        return scorePercentage;
    }

    public void setScorePercentage(Float scorePercentage) {
        this.scorePercentage = scorePercentage;
    }

    public Float getCodingScore() {
        return codingScore;
    }

    public void setCodingScore(Float codingScore) {
        this.codingScore = codingScore;
    }

    public Float getMcqScore() {
        return mcqScore;
    }

    public void setMcqScore(Float mcqScore) {
        this.mcqScore = mcqScore;
    }

    public Float getSubjectiveScore() {
        return subjectiveScore;
    }

    public void setSubjectiveScore(Float subjectiveScore) {
        this.subjectiveScore = subjectiveScore;
    }

    public String getFeedbackSummary() {
        return feedbackSummary;
    }

    public void setFeedbackSummary(String feedbackSummary) {
        this.feedbackSummary = feedbackSummary;
    }

    public LocalDateTime getInterviewTime() {
        return interviewTime;
    }

    public void setInterviewTime(LocalDateTime interviewTime) {
        this.interviewTime = interviewTime;
    }

    public Long getInterviewDuration() {
        return interviewDuration;
    }

    public void setInterviewDuration(Long interviewDuration) {
        this.interviewDuration = interviewDuration;
    }

    public Boolean getCompletedOnTime() {
        return completedOnTime;
    }

    public void setCompletedOnTime(Boolean completedOnTime) {
        this.completedOnTime = completedOnTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getSessionMetadata() {
        return sessionMetadata;
    }

    public void setSessionMetadata(String sessionMetadata) {
        this.sessionMetadata = sessionMetadata;
    }

    // Utility methods
    public Float calculateOverallScore() {
        if (totalQuestions == null || totalQuestions == 0) {
            return 0.0f;
        }
        
        float totalScore = 0.0f;
        int sections = 0;
        
        if (codingQuestions != null && codingQuestions > 0) {
            totalScore += (codingScore != null ? codingScore : 0.0f);
            sections++;
        }
        if (mcqQuestions != null && mcqQuestions > 0) {
            totalScore += (mcqScore != null ? mcqScore : 0.0f);
            sections++;
        }
        if (subjectiveQuestions != null && subjectiveQuestions > 0) {
            totalScore += (subjectiveScore != null ? subjectiveScore : 0.0f);
            sections++;
        }
        
        return sections > 0 ? totalScore / sections : 0.0f;
    }

    public String getPerformanceRating() {
        Float overall = calculateOverallScore();
        if (overall >= 90) return "Excellent";
        if (overall >= 80) return "Good";
        if (overall >= 70) return "Average";
        if (overall >= 60) return "Below Average";
        return "Poor";
    }

    public boolean isCompleted() {
        return "COMPLETED".equals(this.status);
    }

    public void markAsCompleted() {
        this.status = "COMPLETED";
        this.updatedAt = LocalDateTime.now();
    }

    public String getFormattedScore() {
        return String.format("%.1f%%", scorePercentage != null ? scorePercentage : 0.0f);
    }

    public String getFormattedDuration() {
        if (interviewDuration == null) return "N/A";
        long hours = interviewDuration / 60;
        long minutes = interviewDuration % 60;
        return String.format("%dh %dm", hours, minutes);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 