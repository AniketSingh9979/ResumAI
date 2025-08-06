package com.interview;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Main Spring Boot Application for Interview Backend
 * 
 * This application provides REST APIs for:
 * - Technical interview questions (Coding, MCQ, Subjective)
 * - OpenAI-powered Q&A evaluation using cosine similarity
 * - Interview result storage and management
 * - Experience-based question filtering
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class InterviewBackendApplication {

    private static final Logger logger = LoggerFactory.getLogger(InterviewBackendApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Interview Backend Application...");
        SpringApplication.run(InterviewBackendApplication.class, args);
        logger.info("Interview Backend Application started successfully!");
    }
} 