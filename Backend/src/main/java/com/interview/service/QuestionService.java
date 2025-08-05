package com.interview.service;

import com.interview.entity.Question;
import com.interview.entity.Question.ExperienceLevel;
import com.interview.entity.Question.QuestionType;
import com.interview.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing interview questions
 * Handles question fetching, filtering, and interview configuration
 */
@Service
public class QuestionService {

    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);

    private final QuestionRepository questionRepository;

    @Value("${interview.max.questions.coding:3}")
    private int maxCodingQuestions;

    @Value("${interview.max.questions.mcq:10}")
    private int maxMcqQuestions;

    @Value("${interview.max.questions.subjective:5}")
    private int maxSubjectiveQuestions;

    @Value("${interview.timer.minutes:45}")
    private int interviewTimerMinutes;

    @Autowired
    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    // ===== Core Question Fetching Methods =====

    /**
     * Get complete question set for interview based on domain and experience
     */
    public Map<String, List<Question>> getCompleteQuestionSet(String domain, int experienceYears) {
        ExperienceLevel experienceLevel = ExperienceLevel.fromYears(experienceYears);
        
        Map<String, List<Question>> questionSet = new HashMap<>();
        
        // Get coding questions
        List<Question> codingQuestions = getQuestionsByType(domain, experienceLevel, QuestionType.CODING, maxCodingQuestions);
        questionSet.put("coding", codingQuestions);
        
        // Get MCQ questions
        List<Question> mcqQuestions = getQuestionsByType(domain, experienceLevel, QuestionType.MCQ, maxMcqQuestions);
        questionSet.put("mcq", mcqQuestions);
        
        // Get subjective questions
        List<Question> subjectiveQuestions = getQuestionsByType(domain, experienceLevel, QuestionType.SUBJECTIVE, maxSubjectiveQuestions);
        questionSet.put("subjective", subjectiveQuestions);
        
        logger.info("Generated question set for domain: {}, experience: {} years. " +
                   "Coding: {}, MCQ: {}, Subjective: {}", 
                   domain, experienceYears, codingQuestions.size(), mcqQuestions.size(), subjectiveQuestions.size());
        
        return questionSet;
    }

    /**
     * Get questions by specific type with limit
     */
    public List<Question> getQuestionsByType(String domain, ExperienceLevel experienceLevel, 
                                           QuestionType questionType, int limit) {
        try {
            // First try to get exact match questions
            List<Question> exactQuestions = questionRepository.findByDomainAndExperienceLevelAndQuestionTypeAndActiveTrue(
                domain, experienceLevel, questionType);
            
            if (exactQuestions.size() >= limit) {
                Collections.shuffle(exactQuestions);
                return exactQuestions.subList(0, limit);
            }
            
            // If not enough exact matches, get suitable questions (adjacent experience levels)
            List<Question> suitableQuestions = questionRepository.findSuitableQuestionsForExperience(
                domain, questionType, experienceLevel);
            
            if (suitableQuestions.isEmpty()) {
                logger.warn("No questions found for domain: {}, type: {}, experience: {}", 
                           domain, questionType, experienceLevel);
                return new ArrayList<>();
            }
            
            Collections.shuffle(suitableQuestions);
            int actualLimit = Math.min(limit, suitableQuestions.size());
            
            return suitableQuestions.subList(0, actualLimit);
            
        } catch (Exception e) {
            logger.error("Error fetching questions: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Get coding questions for specific domain and experience
     */
    public List<Question> getCodingQuestions(String domain, int experienceYears) {
        ExperienceLevel experienceLevel = ExperienceLevel.fromYears(experienceYears);
        return getQuestionsByType(domain, experienceLevel, QuestionType.CODING, maxCodingQuestions);
    }

    /**
     * Get MCQ questions for specific domain and experience
     */
    public List<Question> getMcqQuestions(String domain, int experienceYears) {
        ExperienceLevel experienceLevel = ExperienceLevel.fromYears(experienceYears);
        return getQuestionsByType(domain, experienceLevel, QuestionType.MCQ, maxMcqQuestions);
    }

    /**
     * Get subjective questions for specific domain and experience
     */
    public List<Question> getSubjectiveQuestions(String domain, int experienceYears) {
        ExperienceLevel experienceLevel = ExperienceLevel.fromYears(experienceYears);
        return getQuestionsByType(domain, experienceLevel, QuestionType.SUBJECTIVE, maxSubjectiveQuestions);
    }

    // ===== Domain and Configuration Methods =====

    /**
     * Get all available domains
     */
    public List<String> getAvailableDomains() {
        try {
            return questionRepository.findDistinctDomains();
        } catch (Exception e) {
            logger.error("Error fetching domains: {}", e.getMessage(), e);
            return getDefaultDomains();
        }
    }

    /**
     * Check if interview is available for given domain and experience
     */
    public InterviewAvailability checkInterviewAvailability(String domain, int experienceYears) {
        ExperienceLevel experienceLevel = ExperienceLevel.fromYears(experienceYears);
        
        long codingCount = questionRepository.countByDomainAndExperienceLevelAndQuestionTypeAndActiveTrue(
            domain, experienceLevel, QuestionType.CODING);
        long mcqCount = questionRepository.countByDomainAndExperienceLevelAndQuestionTypeAndActiveTrue(
            domain, experienceLevel, QuestionType.MCQ);
        long subjectiveCount = questionRepository.countByDomainAndExperienceLevelAndQuestionTypeAndActiveTrue(
            domain, experienceLevel, QuestionType.SUBJECTIVE);
        
        boolean available = codingCount >= 1 && mcqCount >= 3 && subjectiveCount >= 1;
        
        return new InterviewAvailability(available, (int)codingCount, (int)mcqCount, (int)subjectiveCount);
    }

    /**
     * Get interview configuration for a domain
     */
    public InterviewConfiguration getInterviewConfiguration(String domain) {
        return new InterviewConfiguration(
            domain,
            maxCodingQuestions,
            maxMcqQuestions,
            maxSubjectiveQuestions,
            interviewTimerMinutes,
            "Complete all sections within the time limit. Each section tests different aspects of your technical knowledge."
        );
    }

    // ===== Utility Methods =====

    /**
     * Update question usage statistics
     */
    public void updateQuestionUsage(UUID questionId, Float score) {
        try {
            Optional<Question> questionOpt = questionRepository.findById(questionId);
            if (questionOpt.isPresent()) {
                Question question = questionOpt.get();
                question.incrementUsage();
                if (score != null) {
                    question.updateAverageScore(score);
                }
                questionRepository.save(question);
            }
        } catch (Exception e) {
            logger.error("Error updating question usage: {}", e.getMessage(), e);
        }
    }

    /**
     * Get default domains as fallback
     */
    private List<String> getDefaultDomains() {
        return Arrays.asList(
            "Java Development",
            "Python Development", 
            "Frontend Development",
            "Full Stack Development",
            "DevOps",
            "Data Science",
            "System Design"
        );
    }

    /**
     * Get questions by tags
     */
    public List<Question> getQuestionsByTag(String tag) {
        return questionRepository.findByTagsContaining(tag);
    }

    /**
     * Get popular questions (most used)
     */
    public List<Question> getPopularQuestions(int limit) {
        List<Question> popularQuestions = questionRepository.findMostUsedQuestions();
        return popularQuestions.stream().limit(limit).collect(Collectors.toList());
    }

    // ===== Inner Classes for DTOs =====

    /**
     * Interview availability information
     */
    public static class InterviewAvailability {
        private boolean available;
        private int codingQuestions;
        private int mcqQuestions;
        private int subjectiveQuestions;
        private String message;

        public InterviewAvailability(boolean available, int codingQuestions, int mcqQuestions, int subjectiveQuestions) {
            this.available = available;
            this.codingQuestions = codingQuestions;
            this.mcqQuestions = mcqQuestions;
            this.subjectiveQuestions = subjectiveQuestions;
            this.message = available ? "Interview available" : "Insufficient questions for this domain/experience level";
        }

        // Getters and Setters
        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public int getCodingQuestions() {
            return codingQuestions;
        }

        public void setCodingQuestions(int codingQuestions) {
            this.codingQuestions = codingQuestions;
        }

        public int getMcqQuestions() {
            return mcqQuestions;
        }

        public void setMcqQuestions(int mcqQuestions) {
            this.mcqQuestions = mcqQuestions;
        }

        public int getSubjectiveQuestions() {
            return subjectiveQuestions;
        }

        public void setSubjectiveQuestions(int subjectiveQuestions) {
            this.subjectiveQuestions = subjectiveQuestions;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

    /**
     * Interview configuration information
     */
    public static class InterviewConfiguration {
        private String domain;
        private int codingQuestions;
        private int mcqQuestions;
        private int subjectiveQuestions;
        private int timeLimit;
        private String instructions;

        public InterviewConfiguration(String domain, int codingQuestions, int mcqQuestions, 
                                    int subjectiveQuestions, int timeLimit, String instructions) {
            this.domain = domain;
            this.codingQuestions = codingQuestions;
            this.mcqQuestions = mcqQuestions;
            this.subjectiveQuestions = subjectiveQuestions;
            this.timeLimit = timeLimit;
            this.instructions = instructions;
        }

        // Getters and Setters
        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }

        public int getCodingQuestions() {
            return codingQuestions;
        }

        public void setCodingQuestions(int codingQuestions) {
            this.codingQuestions = codingQuestions;
        }

        public int getMcqQuestions() {
            return mcqQuestions;
        }

        public void setMcqQuestions(int mcqQuestions) {
            this.mcqQuestions = mcqQuestions;
        }

        public int getSubjectiveQuestions() {
            return subjectiveQuestions;
        }

        public void setSubjectiveQuestions(int subjectiveQuestions) {
            this.subjectiveQuestions = subjectiveQuestions;
        }

        public int getTimeLimit() {
            return timeLimit;
        }

        public void setTimeLimit(int timeLimit) {
            this.timeLimit = timeLimit;
        }

        public String getInstructions() {
            return instructions;
        }

        public void setInstructions(String instructions) {
            this.instructions = instructions;
        }

        public int getTotalQuestions() {
            return codingQuestions + mcqQuestions + subjectiveQuestions;
        }
    }
} 