package com.resumai.resumeparserservice.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
@Slf4j
public class TextExtractionService {

    private final Tika tika;

    public TextExtractionService() {
        this.tika = new Tika();
        // Set maximum string length for extraction (10MB)
        this.tika.setMaxStringLength(10 * 1024 * 1024);
    }

    /**
     * Extract text content from a MultipartFile using Apache Tika
     * @param file The uploaded file
     * @return Extracted text content
     * @throws IOException If file reading fails
     * @throws TikaException If text extraction fails
     */
    public String extractTextFromFile(MultipartFile file) throws IOException, TikaException {
        log.info("Starting text extraction from file: {}", file.getOriginalFilename());
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String extractedText;
        try (InputStream inputStream = file.getInputStream()) {
            extractedText = tika.parseToString(inputStream);
        }

        log.info("Successfully extracted {} characters from file: {}", 
                extractedText.length(), file.getOriginalFilename());
        
        return extractedText.trim();
    }



    /**
     * Get detected MIME type of the file
     * @param file The uploaded file
     * @return Detected MIME type
     * @throws IOException If file reading fails
     */
    public String detectFileType(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            return tika.detect(inputStream);
        }
    }

    /**
     * Validate if the file type is supported for resume uploads (PDF, DOC, DOCX, RTF, TXT)
     * @param contentType The MIME type of the file
     * @return true if supported, false otherwise
     */
    public boolean isSupportedResumeFileType(String contentType) {
        return contentType != null && (
            contentType.equals("application/pdf") ||
            contentType.equals("application/msword") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
            contentType.equals("application/rtf") ||
            contentType.equals("text/plain") ||
            contentType.startsWith("text/")
        );
    }

    /**
     * Validate if the file type is supported for job description uploads (all document types)
     * @param contentType The MIME type of the file
     * @return true if supported, false otherwise
     */
    public boolean isSupportedJobDescriptionFileType(String contentType) {
        return contentType != null && (
            contentType.equals("application/pdf") ||
            contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
            contentType.equals("application/msword") ||
            contentType.equals("text/plain") ||
            contentType.equals("application/rtf") ||
            contentType.startsWith("text/")
        );
    }

} 