package com.interview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing interview questions for all types:
 * - CODING: Programming questions with starter code
 * - MCQ: Multiple choice questions with options
 * - SUBJECTIVE: Open-ended questions for Q&A evaluation
 */
@Entity
@Table(name = "questions")
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private UUID id;
    
    @Column(name = "title", nullable = false, length = 500)
    private String title;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "question_type", nullable = false)
    private QuestionType questionType;
    
    @Column(name = "domain", nullable = false, length = 100)
    private String domain;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", nullable = false)
    private ExperienceLevel experienceLevel;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty")
    private Difficulty difficulty;
    
    @Column(name = "starter_code", columnDefinition = "TEXT")
    private String starterCode;
    
    @Column(name = "options", columnDefinition = "TEXT")
    private String options; // JSON string for MCQ options
    
    @Column(name = "correct_answer", columnDefinition = "TEXT")
    private String correctAnswer;
    
    @Column(name = "multiple_selection")
    private Boolean multipleSelection = false;
    
    @Column(name = "max_points")
    private Integer maxPoints = 10;
    
    @Column(name = "time_limit")
    private Integer timeLimit; // in minutes
    
    @Column(name = "hints", columnDefinition = "TEXT")
    private String hints;
    
    @Column(name = "tags", length = 500)
    private String tags;
    
    @Column(name = "active")
    private Boolean active = true;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "usage_count")
    private Integer usageCount = 0;
    
    @Column(name = "average_score", precision = 5)
    private Float averageScore = 0.0f;

    // Enums
    public enum QuestionType {
        CODING, MCQ, SUBJECTIVE
    }

    public enum ExperienceLevel {
        FRESHER, JUNIOR, MID, SENIOR, EXPERT;
        
        public static ExperienceLevel fromYears(int years) {
            if (years <= 1) {
                return FRESHER;
            } else if (years <= 3) {
                return JUNIOR;
            } else if (years <= 6) {
                return MID;
            } else if (years <= 10) {
                return SENIOR;
            } else {
                return EXPERT;
            }
        }
    }

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    // Constructors
    public Question() {
        this.createdAt = LocalDateTime.now();
    }

    public Question(String title, String description, QuestionType questionType, 
                   String domain, ExperienceLevel experienceLevel) {
        this();
        this.title = title;
        this.description = description;
        this.questionType = questionType;
        this.domain = domain;
        this.experienceLevel = experienceLevel;
    }

    // Getter and Setter methods
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public QuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(QuestionType questionType) {
        this.questionType = questionType;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public ExperienceLevel getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(ExperienceLevel experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public String getStarterCode() {
        return starterCode;
    }

    public void setStarterCode(String starterCode) {
        this.starterCode = starterCode;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public Boolean getMultipleSelection() {
        return multipleSelection;
    }

    public void setMultipleSelection(Boolean multipleSelection) {
        this.multipleSelection = multipleSelection;
    }

    public Integer getMaxPoints() {
        return maxPoints;
    }

    public void setMaxPoints(Integer maxPoints) {
        this.maxPoints = maxPoints;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public String getHints() {
        return hints;
    }

    public void setHints(String hints) {
        this.hints = hints;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public Float getAverageScore() {
        return averageScore;
    }

    public void setAverageScore(Float averageScore) {
        this.averageScore = averageScore;
    }

    // Utility methods
    public boolean isSuitableFor(ExperienceLevel targetLevel) {
        return this.experienceLevel == targetLevel || 
               (this.experienceLevel.ordinal() <= targetLevel.ordinal() + 1 && 
                this.experienceLevel.ordinal() >= targetLevel.ordinal() - 1);
    }

    public List<String> getOptionsAsList() {
        if (options == null || options.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            // Handle both JSON array format and simple comma-separated format
            String cleanOptions = options.trim();
            if (cleanOptions.startsWith("[") && cleanOptions.endsWith("]")) {
                cleanOptions = cleanOptions.substring(1, cleanOptions.length() - 1);
            }
            return Arrays.asList(cleanOptions.split("\\s*,\\s*"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void incrementUsage() {
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }

    public void updateAverageScore(Float newScore) {
        if (this.averageScore == null) {
            this.averageScore = 0.0f;
        }
        if (this.usageCount == null || this.usageCount == 0) {
            this.averageScore = newScore;
        } else {
            this.averageScore = ((this.averageScore * this.usageCount) + newScore) / (this.usageCount + 1);
        }
    }
} 