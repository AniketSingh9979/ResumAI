package com.interview.controller;

import com.interview.entity.Question;
import com.interview.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for interview questions
 * Handles fetching questions based on domain and experience
 */
@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "http://localhost:4200")
public class QuestionController {

    private static final Logger logger = LoggerFactory.getLogger(QuestionController.class);
    
    private final QuestionService questionService;

    @Autowired
    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * GET /api/questions/domains - Get all available domains
     */
    @GetMapping("/domains")
    public ResponseEntity<List<String>> getAvailableDomains() {
        try {
            List<String> domains = questionService.getAvailableDomains();
            logger.info("Retrieved {} available domains", domains.size());
            return ResponseEntity.ok(domains);
        } catch (Exception e) {
            logger.error("Error fetching domains: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET /api/questions/complete-set - Get complete question set for interview
     */
    @GetMapping("/complete-set")
    public ResponseEntity<Map<String, List<Question>>> getCompleteQuestionSet(
            @RequestParam String domain,
            @RequestParam int experienceYears) {
        try {
            Map<String, List<Question>> questionSet = 
                questionService.getCompleteQuestionSet(domain, experienceYears);
            
            int totalQuestions = questionSet.values().stream()
                .mapToInt(List::size)
                .sum();
            
            logger.info("Generated complete question set for domain: {}, experience: {} years. Total: {} questions", 
                       domain, experienceYears, totalQuestions);
            
            return ResponseEntity.ok(questionSet);
        } catch (Exception e) {
            logger.error("Error generating question set: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
} 