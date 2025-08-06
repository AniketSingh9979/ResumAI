package com.interview.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.interview.entity.ChatMessage;
import com.interview.entity.InterviewResult;
import com.interview.repository.ChatRepository;
import com.interview.repository.InterviewResultRepository;
import com.interview.service.InterviewOpenAiService;
import com.interview.service.NotificationService;

/**
 * REST Controller for Q&A chat interactions and interview submissions
 * Handles OpenAI-powered subjective question evaluation
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    
    private final ChatRepository chatRepository;
    private final InterviewResultRepository interviewResultRepository;
    private final InterviewOpenAiService openAiService;
    private final NotificationService notificationService;

    @Autowired
    public ChatController(ChatRepository chatRepository, 
                         InterviewResultRepository interviewResultRepository,
                         InterviewOpenAiService openAiService,
                         @Autowired(required = false) NotificationService notificationService) {
        this.chatRepository = chatRepository;
        this.interviewResultRepository = interviewResultRepository;
        this.openAiService = openAiService;
        this.notificationService = notificationService;
    }

    /**
     * POST /api/chat/respond - Handle Q&A responses with OpenAI evaluation
     */
    @PostMapping("/respond")
    public ResponseEntity<ChatResponse> handleChatResponse(@RequestBody ChatRequest request) {
        logger.info("Processing chat response for resumeId: {}, domain: {}", 
                   request.getResumeId(), request.getDomain());
        
        try {
            long startTime = System.currentTimeMillis();
            
            // Check for recent similar evaluations to prevent duplicates
            Optional<ChatMessage> recentEvaluation = chatRepository.findRecentEvaluationByResumeAndQuestion(
                request.getResumeId(), 
                request.getQuestionText(),
                java.time.LocalDateTime.now().minusMinutes(5) // Within last 5 minutes
            );
            
            if (recentEvaluation.isPresent()) {
                ChatMessage recent = recentEvaluation.get();
                // Check if user answer is very similar to recently evaluated answer
                double textSimilarity = calculateSimpleTextSimilarity(
                    request.getUserAnswer(), 
                    recent.getUserQuestion()  // Correct method name
                );
                
                if (textSimilarity > 0.9) { // 90% similar
                    logger.info("Skipping evaluation - very similar answer recently evaluated");
                    
                    // Return cached result
                    ChatResponse response = new ChatResponse();
                    response.setBotResponse(recent.getBotResponse());
                    response.setSimilarity(recent.getSimilarity());
                    response.setCorrect(recent.getIsCorrectAnswer());
                    response.setFeedback(recent.getFeedback());
                    response.setScore(recent.getScore());
                    response.setResponseTimeMs(0L); // Cached response
                    response.setMessageId(recent.getId().toString());
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            // Step 1: Generate expected answer using AI
            String expectedAnswer = openAiService.generateExpectedAnswer(
                request.getQuestionText(),
                request.getDomain()
            );
            
            // Step 2: Generate comprehensive response for reference
            String botResponse = openAiService.getInterviewCompletion(
                request.getQuestionText(),    // Actual interview question
                request.getDomain(),
                expectedAnswer                // AI-generated expected answer
            );
            
            // Step 3: Calculate similarity between user answer and expected answer
            double similarity = openAiService.compareAnswers(
                expectedAnswer,               // AI-generated expected answer
                request.getUserAnswer()       // User's actual answer
            );
            
            // Step 4: Determine if answer is correct (threshold: 0.6)
            boolean isCorrect = similarity >= 0.6;
            
            // Step 5: Generate AI-powered contextual feedback
            String feedback = openAiService.generateAIFeedback(
                request.getQuestionText(),    // Original question
                request.getUserAnswer(),      // User's answer
                expectedAnswer,               // AI-generated expected answer
                similarity                    // Similarity score
            );
            
            // Calculate response time
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Save to database
            ChatMessage chatMessage = new ChatMessage(
                request.getResumeId(),
                request.getUserAnswer(),        // User's actual answer
                expectedAnswer,                 // AI-generated expected answer
                request.getDomain()
            );
            chatMessage.setBotResponse(botResponse);
            chatMessage.setSimilarity((float) similarity);
            chatMessage.setIsCorrectAnswer(isCorrect);
            chatMessage.setFeedback(feedback);
            chatMessage.setResponseTimeMs(responseTime);
            chatMessage.setQuestionType("SUBJECTIVE");
            
            // Calculate score based on similarity
            float score = Math.max(0, (float) (similarity * 100));
            chatMessage.setScore(score);
            
            ChatMessage savedMessage = chatRepository.save(chatMessage);
            
            // Prepare response
            ChatResponse response = new ChatResponse();
            response.setBotResponse(botResponse);
            response.setSimilarity(similarity);
            response.setCorrect(isCorrect);
            response.setFeedback(feedback);
            response.setScore(score);
            response.setResponseTimeMs(responseTime);
            response.setMessageId(savedMessage.getId().toString());
            
            logger.info("Chat response processed successfully. Similarity: {}, Correct: {}", 
                       similarity, isCorrect);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing chat response: {}", e.getMessage(), e);
            
            ChatResponse errorResponse = new ChatResponse();
            errorResponse.setBotResponse("Error processing your response. Please try again.");
            errorResponse.setSimilarity(0.0);
            errorResponse.setCorrect(false);
            errorResponse.setFeedback("Unable to evaluate response due to technical error.");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }



    /**
     * POST /api/chat/submitInterview - Submit complete interview results
     */
    @PostMapping("/submitInterview")
    public ResponseEntity<Map<String, Object>> submitInterview(@RequestBody InterviewSubmissionRequest request) {
        logger.info("Submitting interview for candidate: {}, domain: {}", 
                   request.getCandidateName(), request.getDomain());
        
        try {
            // Calculate subjective scores from chat messages
            Map<String, Object> subjectiveScores = calculateSubjectiveScores(request.getResumeId());
            
            // Create interview result
            InterviewResult result = new InterviewResult(
                request.getResumeId(),
                request.getCandidateName(),
                request.getDomain(),
                request.getExperienceLevel()
            );
            
            result.setCandidateEmail(request.getCandidateEmail());
            result.setTotalQuestions(request.getTotalQuestions());
            result.setCorrectAnswers(request.getCorrectAnswers());
            result.setScorePercentage(request.getScorePercentage());
            result.setFeedbackSummary(request.getFeedbackSummary());
            
            // Set section-specific data
            result.setCodingQuestions(request.getCodingQuestions());
            result.setMcqQuestions(request.getMcqQuestions());
            result.setSubjectiveQuestions(request.getSubjectiveQuestions());
            result.setCodingScore(request.getCodingScore());
            result.setMcqScore(request.getMcqScore());
            result.setSubjectiveScore((Float) subjectiveScores.get("averageScore"));
            
            result.setInterviewDuration(request.getInterviewDuration());
            result.setCompletedOnTime(request.getCompletedOnTime());
            result.setStatus("COMPLETED");
            
            // Save result
            InterviewResult savedResult = interviewResultRepository.save(result);
            
            // Send email notification asynchronously (non-blocking)
            if (notificationService != null) {
                try {
                    notificationService.sendInterviewResultNotification(savedResult);
                } catch (Exception e) {
                    // Log error but don't fail the main flow
                    logger.warn("Email notification failed for candidate: {} - Error: {}", 
                               savedResult.getCandidateName(), e.getMessage());
                }
            } else {
                logger.info("Email notification service not available for candidate: {}", 
                           savedResult.getCandidateName());
            }
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Interview submitted successfully");
            response.put("resultId", savedResult.getResultId().toString());
            response.put("overallScore", result.calculateOverallScore());
            response.put("performanceRating", result.getPerformanceRating());
            response.put("subjectiveAnalysis", subjectiveScores);
            
            logger.info("Interview submitted successfully for candidate: {}", request.getCandidateName());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error submitting interview: {}", e.getMessage(), e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error submitting interview: " + e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Calculate subjective scores from chat messages
     */
    private Map<String, Object> calculateSubjectiveScores(String resumeId) {
        Map<String, Object> scores = new HashMap<>();
        
        try {
            List<ChatMessage> chatMessages = chatRepository.findByResumeIdOrderByTimestampAsc(resumeId);
            
            if (chatMessages.isEmpty()) {
                scores.put("averageScore", 0.0f);
                scores.put("correctAnswers", 0);
                scores.put("totalQuestions", 0);
                return scores;
            }
            
            int correctAnswers = 0;
            float totalScore = 0.0f;
            
            for (ChatMessage message : chatMessages) {
                if (Boolean.TRUE.equals(message.getIsCorrectAnswer())) {
                    correctAnswers++;
                }
                if (message.getScore() != null) {
                    totalScore += message.getScore();
                }
            }
            
            float averageScore = chatMessages.size() > 0 ? totalScore / chatMessages.size() : 0.0f;
            
            scores.put("averageScore", averageScore);
            scores.put("correctAnswers", correctAnswers);
            scores.put("totalQuestions", chatMessages.size());
            scores.put("accuracy", chatMessages.size() > 0 ? (float) correctAnswers / chatMessages.size() : 0.0f);
            
        } catch (Exception e) {
            logger.error("Error calculating subjective scores: {}", e.getMessage(), e);
            scores.put("averageScore", 0.0f);
            scores.put("correctAnswers", 0);
            scores.put("totalQuestions", 0);
        }
        
        return scores;
    }

    // ===== DTO Classes =====

    /**
     * Request class for chat interactions
     */
    public static class ChatRequest {
        private String questionText;
        private String userAnswer;
        private String resumeId;
        private String domain;

        // Constructors
        public ChatRequest() {}

        public ChatRequest(String questionText, String userAnswer, String resumeId, String domain) {
            this.questionText = questionText;
            this.userAnswer = userAnswer;
            this.resumeId = resumeId;
            this.domain = domain;
        }

        // Getters and Setters
        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public String getUserAnswer() {
            return userAnswer;
        }

        public void setUserAnswer(String userAnswer) {
            this.userAnswer = userAnswer;
        }

        public String getResumeId() {
            return resumeId;
        }

        public void setResumeId(String resumeId) {
            this.resumeId = resumeId;
        }

        public String getDomain() {
            return domain;
        }

        public void setDomain(String domain) {
            this.domain = domain;
        }
    }

    /**
     * Response class for chat interactions
     */
    public static class ChatResponse {
        private String botResponse;
        private double similarity;
        private boolean correct;
        private String feedback;
        private float score;
        private long responseTimeMs;
        private String messageId;

        // Constructors
        public ChatResponse() {}

        // Getters and Setters
        public String getBotResponse() {
            return botResponse;
        }

        public void setBotResponse(String botResponse) {
            this.botResponse = botResponse;
        }

        public double getSimilarity() {
            return similarity;
        }

        public void setSimilarity(double similarity) {
            this.similarity = similarity;
        }

        public boolean isCorrect() {
            return correct;
        }

        public void setCorrect(boolean correct) {
            this.correct = correct;
        }

        public String getFeedback() {
            return feedback;
        }

        public void setFeedback(String feedback) {
            this.feedback = feedback;
        }

        public float getScore() {
            return score;
        }

        public void setScore(float score) {
            this.score = score;
        }

        public long getResponseTimeMs() {
            return responseTimeMs;
        }

        public void setResponseTimeMs(long responseTimeMs) {
            this.responseTimeMs = responseTimeMs;
        }

        public String getMessageId() {
            return messageId;
        }

        public void setMessageId(String messageId) {
            this.messageId = messageId;
        }
    }

    /**
     * Simple text similarity calculation for duplicate detection
     */
    private double calculateSimpleTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }

        String s1 = text1.toLowerCase().trim();
        String s2 = text2.toLowerCase().trim();

        if (s1.equals(s2)) {
            return 1.0;
        }

        // Simple word-based similarity
        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");
        
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.size() > 0 ? (double) intersection.size() / union.size() : 0.0;
    }

    /**
     * Request class for interview submission
     */
    public static class InterviewSubmissionRequest {
        private String resumeId;
        private String candidateName;
        private String candidateEmail;
        private String domain;
        private String experienceLevel;
        private Integer totalQuestions;
        private Integer correctAnswers;
        private Float scorePercentage;
        private String feedbackSummary;
        private Integer codingQuestions;
        private Integer mcqQuestions;
        private Integer subjectiveQuestions;
        private Float codingScore;
        private Float mcqScore;
        private Long interviewDuration;
        private Boolean completedOnTime;

        // Constructors
        public InterviewSubmissionRequest() {}

        // Getters and Setters
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

        public String getFeedbackSummary() {
            return feedbackSummary;
        }

        public void setFeedbackSummary(String feedbackSummary) {
            this.feedbackSummary = feedbackSummary;
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
    }
} 