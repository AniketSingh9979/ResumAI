package com.resumai.resumeparserservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class HashingService {

    /**
     * Generate SHA-256 hash for uploaded file content
     */
    public String generateFileHash(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            
            return bytesToHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Unable to generate file hash", e);
        }
    }

    /**
     * Generate SHA-256 hash for text content
     */
    public String generateTextHash(String content) {
        if (content == null) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Unable to generate text hash", e);
        }
    }

    /**
     * Generate hash for job description content by combining key fields
     */
    public String generateJobDescriptionHash(String title, String company, String description, 
                                           String requirements, String responsibilities) {
        StringBuilder combined = new StringBuilder();
        
        // Normalize and combine key fields for consistent hashing
        if (title != null) {
            combined.append(normalizeText(title)).append("|");
        }
        if (company != null) {
            combined.append(normalizeText(company)).append("|");
        }
        if (description != null) {
            combined.append(normalizeText(description)).append("|");
        }
        if (requirements != null) {
            combined.append(normalizeText(requirements)).append("|");
        }
        if (responsibilities != null) {
            combined.append(normalizeText(responsibilities));
        }
        
        return generateTextHash(combined.toString());
    }

    /**
     * Normalize text for consistent hashing by removing extra whitespace and converting to lowercase
     */
    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /**
     * Convert byte array to hexadecimal string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
} 