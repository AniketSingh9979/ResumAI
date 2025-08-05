package com.interview.service;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;

/**
 * Service for OpenAI API integration
 * Handles GPT completions and text similarity comparisons using embeddings
 */
@Service
public class InterviewOpenAiService {

    private static final Logger logger = LoggerFactory.getLogger(InterviewOpenAiService.class);
    
    private final OpenAiService openAiService;
    private final boolean isConfigured;

    // Constructor with OpenAI API key injection
    public InterviewOpenAiService(@Value("${openai.api.key:}") String apiKey,
                                  @Value("${openai.timeout:60}") long timeoutSeconds) {
        if (apiKey == null || apiKey.trim().isEmpty() || "YOUR_OPENAI_API_KEY_HERE".equals(apiKey)) {
            logger.warn("OpenAI API key not configured. OpenAI features will be disabled.");
            this.openAiService = null;
            this.isConfigured = false;
        } else {
            this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(timeoutSeconds));
            this.isConfigured = true;
            logger.info("OpenAI service initialized successfully");
        }
    }

    /**
     * Get completion from GPT model
     */
    public String getCompletion(String prompt) {
        if (!isConfigured) {
            logger.warn("OpenAI not configured. Returning mock response.");
            return "Mock response: This is a simulated answer since OpenAI is not configured.";
        }

        try {
            ChatMessage message = new ChatMessage(ChatMessageRole.USER.value(), prompt);
            
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(Collections.singletonList(message))
                    .maxTokens(500)
                    .temperature(0.7)
                    .build();

            var completion = openAiService.createChatCompletion(request);
            
            if (completion.getChoices() != null && !completion.getChoices().isEmpty()) {
                return completion.getChoices().get(0).getMessage().getContent().trim();
            }
            
            return "No response generated";
            
        } catch (Exception e) {
            logger.error("Error calling OpenAI API: {}", e.getMessage(), e);
            return "Error generating response: " + e.getMessage();
        }
    }

    /**
     * Get interview-specific completion with custom prompt
     */
    public String getInterviewCompletion(String questionText, String domain, String expectedAnswer) {
        String prompt = buildInterviewPrompt(questionText, domain, expectedAnswer);
        return getCompletion(prompt);
    }

    /**
     * Create embeddings for text similarity comparison
     */
    public List<Double> createEmbedding(String text) {
        if (!isConfigured) {
            logger.warn("OpenAI not configured. Returning mock embedding.");
            // Return a mock embedding vector for testing
            return Arrays.asList(0.1, 0.2, 0.3, 0.4, 0.5);
        }

        try {
            EmbeddingRequest request = EmbeddingRequest.builder()
                    .model("text-embedding-ada-002")
                    .input(Collections.singletonList(text))
                    .build();

            var embedding = openAiService.createEmbeddings(request);
            
            if (embedding.getData() != null && !embedding.getData().isEmpty()) {
                return embedding.getData().get(0).getEmbedding();
            }
            
            return Collections.emptyList();
            
        } catch (Exception e) {
            logger.error("Error creating embedding: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Calculate cosine similarity between two text strings using embeddings
     */
    public double compareAnswers(String expected, String actual) {
        if (!isConfigured) {
            logger.warn("OpenAI not configured. Using simple text similarity.");
            return calculateSimpleTextSimilarity(expected, actual);
        }

        try {
            List<Double> expectedEmbedding = createEmbedding(expected);
            List<Double> actualEmbedding = createEmbedding(actual);
            
            if (expectedEmbedding.isEmpty() || actualEmbedding.isEmpty()) {
                logger.warn("Failed to create embeddings. Falling back to simple similarity.");
                return calculateSimpleTextSimilarity(expected, actual);
            }
            
            return calculateCosineSimilarity(expectedEmbedding, actualEmbedding);
            
        } catch (Exception e) {
            logger.error("Error comparing answers: {}", e.getMessage(), e);
            return calculateSimpleTextSimilarity(expected, actual);
        }
    }

    /**
     * Calculate cosine similarity between two embedding vectors
     */
    public double calculateCosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        if (vectorA.size() != vectorB.size()) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.size(); i++) {
            double a = vectorA.get(i);
            double b = vectorB.get(i);
            
            dotProduct += a * b;
            normA += a * a;
            normB += b * b;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Simple text similarity as fallback when OpenAI is not available
     */
    public double calculateSimpleTextSimilarity(String text1, String text2) {
        if (text1 == null || text2 == null) {
            return 0.0;
        }

        String s1 = text1.toLowerCase().trim();
        String s2 = text2.toLowerCase().trim();

        if (s1.equals(s2)) {
            return 1.0;
        }

        // Simple word overlap similarity
        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");

        int commonWords = 0;
        int totalUniqueWords = 0;

        for (String word1 : words1) {
            for (String word2 : words2) {
                if (word1.equals(word2)) {
                    commonWords++;
                    break;
                }
            }
        }

        totalUniqueWords = words1.length + words2.length - commonWords;
        
        return totalUniqueWords > 0 ? (double) commonWords / totalUniqueWords : 0.0;
    }

    /**
     * Build interview-specific prompt for GPT
     */
    private String buildInterviewPrompt(String questionText, String domain, String expectedAnswer) {
        return String.format(
            "You are conducting a technical interview in the %s domain. " +
            "Interview Question: %s\n\n" +
            "Expected Answer Reference: %s\n\n" +
            "Please provide a comprehensive and detailed answer to this interview question. " +
            "Focus on key technical concepts, practical applications, and real-world examples. " +
            "Structure your response clearly and make it suitable for technical assessment.",
            domain, questionText, expectedAnswer
        );
    }

    /**
     * Generate expected answer for a given interview question using AI
     */
    public String generateExpectedAnswer(String questionText, String domain) {
        if (!isConfigured) {
            logger.warn("OpenAI not configured. Returning generic expected answer.");
            return "This is a mock expected answer since OpenAI is not configured. " +
                   "Please provide a comprehensive answer covering key concepts and practical applications.";
        }

        String prompt = String.format(
            "You are a senior technical expert in %s. " +
            "Generate a comprehensive, accurate answer to this interview question: %s\n\n" +
            "Requirements:\n" +
            "- Cover all key technical concepts\n" +
            "- Include practical examples\n" +
            "- Structure the answer clearly\n" +
            "- Use professional language suitable for assessment\n" +
            "- Focus on depth and accuracy\n" +
            "- Length: 2-4 sentences with key points covered",
            domain, questionText
        );
        
        return getCompletion(prompt);
    }

    /**
     * Generate AI-powered contextual feedback based on question, answers, and similarity
     */
    public String generateAIFeedback(String questionText, String userAnswer, String expectedAnswer, double similarity) {
        if (!isConfigured) {
            logger.warn("OpenAI not configured. Using simple feedback.");
            return generateSimpleFeedback(similarity);
        }

        String prompt = String.format(
            "You are an experienced technical interviewer providing feedback on a candidate's answer.\n\n" +
            "Interview Question: %s\n\n" +
            "Expected Answer: %s\n\n" +
            "Candidate's Answer: %s\n\n" +
            "Similarity Score: %.2f (0.0 = completely different, 1.0 = perfect match)\n\n" +
            "Provide constructive feedback that:\n" +
            "- Highlights what the candidate did well\n" +
            "- Identifies key concepts they missed\n" +
            "- Suggests specific improvements\n" +
            "- Maintains an encouraging but honest tone\n" +
            "- Keeps feedback concise (2-3 sentences)\n" +
            "- Focuses on technical accuracy and completeness",
            questionText, expectedAnswer, userAnswer, similarity
        );
        
        return getCompletion(prompt);
    }

    /**
     * Simple fallback feedback when AI is not available
     */
    private String generateSimpleFeedback(double similarity) {
        if (similarity >= 0.8) {
            return "Excellent answer! Your response demonstrates strong understanding of the concepts.";
        } else if (similarity >= 0.6) {
            return "Good answer with room for improvement. Consider covering more key points.";
        } else if (similarity >= 0.4) {
            return "Partial understanding shown. Review the core concepts and try to be more comprehensive.";
        } else {
            return "Answer needs significant improvement. Please study the topic more thoroughly.";
        }
    }

    /**
     * Check if OpenAI service is properly configured
     */
    public boolean isConfigured() {
        return isConfigured;
    }

    /**
     * Get service status for health checks
     */
    public String getServiceStatus() {
        return isConfigured ? "OpenAI service is configured and ready" : "OpenAI service is not configured";
    }
} 