package com.interview.config;

import feign.Retryer;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for Feign clients
 * Provides retry logic and error handling for service-to-service communication
 */
@Configuration
public class FeignConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(FeignConfig.class);
    
    /**
     * Custom retryer for Feign clients
     * Retries failed requests with exponential backoff
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(
            1000,    // Initial interval (1 second)
            3000,    // Max interval (3 seconds)
            3        // Max attempts
        );
    }
    
    /**
     * Custom error decoder for better error handling
     */
    @Bean
    public ErrorDecoder feignErrorDecoder() {
        return new ErrorDecoder() {
            @Override
            public Exception decode(String methodKey, feign.Response response) {
                String message = String.format("Service call failed: %s - Status: %d", 
                    methodKey, response.status());
                
                logger.warn("Feign client error: {}", message);
                
                switch (response.status()) {
                    case 404:
                        return new RuntimeException("Mail service endpoint not found: " + methodKey);
                    case 503:
                        return new RuntimeException("Mail service unavailable: " + methodKey);
                    case 500:
                        return new RuntimeException("Mail service internal error: " + methodKey);
                    default:
                        return new RuntimeException(message);
                }
            }
        };
    }
} 