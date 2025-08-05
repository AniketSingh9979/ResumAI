package com.interview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing Q&A chat interactions with OpenAI evaluation
 * Stores user questions, bot responses, and similarity scores
 */
@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "resume_id", nullable = false, length = 100)
    private String resumeId;
    
    @Column(name = "session_id", length = 100)
    private String sessionId;
    
    @Column(name = "user_question", columnDefinition = "TEXT", nullable = false)
    private String userQuestion;
    
    @Column(name = "bot_response", columnDefinition = "TEXT")
    private String botResponse;
    
    @Column(name = "expected_answer", columnDefinition = "TEXT")
    private String expectedAnswer;
    
    @Column(name = "similarity", precision = 5)
    private Float similarity;
    
    @Column(name = "score", precision = 5)
    private Float score;
    
    @Column(name = "is_correct_answer")
    private Boolean isCorrectAnswer = false;
    
    @Column(name = "domain", length = 100)
    private String domain;
    
    @Column(name = "question_type", length = 50)
    private String questionType;
    
    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;
    
    @Column(name = "flagged")
    private Boolean flagged = false;
    
    @Column(name = "response_time_ms")
    private Long responseTimeMs;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;

    // Constructors
    public ChatMessage() {
        this.timestamp = LocalDateTime.now();
    }

    public ChatMessage(String resumeId, String userQuestion, String expectedAnswer, String domain) {
        this();
        this.resumeId = resumeId;
        this.userQuestion = userQuestion;
        this.expectedAnswer = expectedAnswer;
        this.domain = domain;
    }

    // Getter and Setter methods
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserQuestion() {
        return userQuestion;
    }

    public void setUserQuestion(String userQuestion) {
        this.userQuestion = userQuestion;
    }

    public String getBotResponse() {
        return botResponse;
    }

    public void setBotResponse(String botResponse) {
        this.botResponse = botResponse;
    }

    public String getExpectedAnswer() {
        return expectedAnswer;
    }

    public void setExpectedAnswer(String expectedAnswer) {
        this.expectedAnswer = expectedAnswer;
    }

    public Float getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Float similarity) {
        this.similarity = similarity;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public Boolean getIsCorrectAnswer() {
        return isCorrectAnswer;
    }

    public void setIsCorrectAnswer(Boolean isCorrectAnswer) {
        this.isCorrectAnswer = isCorrectAnswer;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getQuestionType() {
        return questionType;
    }

    public void setQuestionType(String questionType) {
        this.questionType = questionType;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Boolean getFlagged() {
        return flagged;
    }

    public void setFlagged(Boolean flagged) {
        this.flagged = flagged;
    }

    public Long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(Long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    // Utility methods
    public boolean isCorrect() {
        return Boolean.TRUE.equals(this.isCorrectAnswer);
    }

    public void evaluateCorrectness(float threshold) {
        if (this.similarity != null) {
            this.isCorrectAnswer = this.similarity >= threshold;
            // Calculate score based on similarity
            this.score = Math.max(0, this.similarity * 100);
        }
    }

    public void markAsFlagged(String reason) {
        this.flagged = true;
        if (this.metadata == null) {
            this.metadata = "flagged_reason: " + reason;
        } else {
            this.metadata += "; flagged_reason: " + reason;
        }
    }
} 