package com.resumai.resumeparserservice.controller;

import com.resumai.resumeparserservice.service.ParsedResumeService;
import com.resumai.resumeparserservice.service.ParsedResumeService.ResumeProcessingData;
import com.resumai.resumeparserservice.service.ParsedResumeService.MatchingResults;
import com.resumai.resumeparserservice.entity.ParsedResume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*")
@RequiredArgsConstructor
@Slf4j
public class FileUploadController {

    private final ParsedResumeService parsedResumeService;

    /**
     * Simple health check endpoint to verify all dependencies are working
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        try {
            log.info("🏥 Health check requested");
            
            // Test if service is available
            if (parsedResumeService != null) {
                response.put("status", "UP");
                response.put("service", "resume-parser-service");
                response.put("timestamp", java.time.LocalDateTime.now());
                response.put("message", "Service is running properly");
                
                log.info("✅ Health check passed");
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "DOWN");
                response.put("message", "ParsedResumeService is not available");
                return ResponseEntity.internalServerError().body(response);
            }
        } catch (Exception e) {
            log.error("❌ Health check failed: {}", e.getMessage(), e);
            response.put("status", "ERROR");
            response.put("message", "Health check failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Simple test endpoint to check if the controller is accessible
     */
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "FileUploadController is working!");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        log.info("🧪 Test endpoint accessed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/uploadResume")
    public ResponseEntity<Map<String, Object>> uploadResume(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("📄 Starting resume upload process for file: {} (size: {} bytes)", 
                    file.getOriginalFilename(), file.getSize());
            
            // Step 1: Validate file
            log.debug("Step 1: Validating uploaded file...");
            parsedResumeService.validateUploadedFile(file);
            log.info("✅ File validation successful");

            // Step 2: Save file to disk
            log.debug("Step 2: Saving file to disk...");
            Path filePath = parsedResumeService.saveFileToDisk(file);
            log.info("✅ File saved to: {}", filePath);

            // Step 3: Extract and parse resume data
            log.debug("Step 3: Extracting and parsing resume data...");
            ResumeProcessingData resumeData = parsedResumeService.extractAndParseResumeData(file);
            log.info("✅ Resume data extracted successfully");

            // Step 4: Score resume against jobs
            log.debug("Step 4: Scoring resume against job descriptions...");
            MatchingResults matchingResults = parsedResumeService.scoreResumeAgainstJobs(resumeData.extractedText);
            log.info("✅ Resume scoring completed");

            // Step 5: Save to database
            log.debug("Step 5: Saving processed resume to database...");
            ParsedResume savedResume = parsedResumeService.saveProcessedResumeToDatabase(file, resumeData, matchingResults, filePath);
            log.info("✅ Resume saved to database with ID: {}", savedResume.getId());

            // Step 6: Prepare response
            log.debug("Step 6: Building success response...");
            return buildSuccessResponse(response, savedResume, resumeData, matchingResults, filePath);

        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (IOException e) {
            log.error("Error processing file: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to process file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } catch (TikaException e) {
            log.error("Error extracting text: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to extract text from file: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        } catch (Exception e) {
            log.error("Error processing resume: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to process resume: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Build comprehensive success response
     */
    private ResponseEntity<Map<String, Object>> buildSuccessResponse(Map<String, Object> response, 
                                                                    ParsedResume savedResume, 
                                                                    ResumeProcessingData resumeData, 
                                                                    MatchingResults matchingResults, 
                                                                    Path filePath) {
        // Use the content type from the saved resume entity
        String detectedType = savedResume.getContentType();

        // Prepare comprehensive response
        response.put("success", true);
        response.put("message", "Resume uploaded, parsed, scored, and saved successfully");
        
        // File information
        response.put("resumeId", savedResume.getId());
        response.put("originalFileName", savedResume.getOriginalFileName());
        response.put("fileSize", savedResume.getFileSize());
        response.put("contentType", savedResume.getContentType());
        response.put("detectedType", detectedType);
        response.put("uploadTime", savedResume.getUploadTime());
        
        // Extracted data
        response.put("extractedText", resumeData.extractedText);
        response.put("textLength", resumeData.extractedText.length());
        response.put("email", resumeData.email);
        response.put("skills", resumeData.skills);
        response.put("skillsCount", resumeData.skills.size());
        response.put("experience", resumeData.experience);
        
        // Scoring results
        response.put("bestMatchScore", matchingResults.bestScore);
        response.put("totalJobsMatched", matchingResults.allMatches.size());
        
        if (matchingResults.bestMatch != null) {
            response.put("bestMatchJob", Map.of(
                "jobId", matchingResults.bestMatch.get("jobId"),
                "jobTitle", matchingResults.bestMatch.get("jobTitle"),
                "company", matchingResults.bestMatch.get("company"),
                "matchCategory", matchingResults.bestMatch.get("matchCategory")
            ));
        }
        
        // Database confirmation
        response.put("savedToDatabase", true);
        response.put("databaseId", savedResume.getId());
        
        // File storage confirmation
        response.put("fileSaved", true);
        response.put("filePath", filePath.toString());

        return ResponseEntity.ok(response);
    }

    /**
     * Get all resumes available for download
     */
    @GetMapping("/resumesList")
    public ResponseEntity<Map<String, Object>> getAllResumes() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ParsedResume> resumes = parsedResumeService.findAllOrderByUploadTime();
            
            // Create a simplified list for download purposes
            List<Map<String, Object>> resumeList = resumes.stream()
                    .map(resume -> {
                        Map<String, Object> resumeInfo = new HashMap<>();
                        resumeInfo.put("resumeId", resume.getId());
                        resumeInfo.put("email", resume.getEmail());
                        resumeInfo.put("originalFileName", resume.getOriginalFileName());
                        resumeInfo.put("uploadTime", resume.getUploadTime());
                        resumeInfo.put("fileSize", resume.getFileSize());
                        resumeInfo.put("contentType", resume.getContentType());
                        resumeInfo.put("score", resume.getScore());
                        resumeInfo.put("skills", resume.getSkills());
                        resumeInfo.put("experience", resume.getExperience());
                        resumeInfo.put("hasFile", resume.getFilePath() != null && !resume.getFilePath().trim().isEmpty());
                        
                        // Add download URL for each resume
                        if (resume.getEmail() != null && !resume.getEmail().trim().isEmpty()) {
                            resumeInfo.put("downloadUrl", "/api/downloadResume/" + resume.getEmail());
                        } else {
                            resumeInfo.put("downloadUrl", null);
                        }
                        
                        return resumeInfo;
                    })
                    .toList();
            
            response.put("success", true);
            response.put("message", "Resumes retrieved successfully");
            response.put("totalCount", resumeList.size());
            response.put("resumes", resumeList);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving resumes: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve resumes: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Download original resume file by email
     */
    @GetMapping("/downloadResume/{email}")
    public ResponseEntity<Resource> downloadResume(@PathVariable String email) {
        try {
            List<ParsedResume> resumes = parsedResumeService.findByEmail(email);
            
            if (resumes.isEmpty()) {
                log.error("No resume found for email: {}", email);
                return ResponseEntity.notFound().build();
            }
            
            // If multiple resumes exist for the same email, get the most recent one
            ParsedResume resume = resumes.stream()
                    .max((r1, r2) -> r1.getUploadTime().compareTo(r2.getUploadTime()))
                    .orElse(resumes.get(0));
            
            String filePath = resume.getFilePath();
            
            if (filePath == null || filePath.trim().isEmpty()) {
                log.error("File path is null or empty for email: {}", email);
                return ResponseEntity.notFound().build();
            }
            
            Path file = Paths.get(filePath);
            
            if (!Files.exists(file)) {
                log.error("File not found on disk: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new UrlResource(file.toUri());
            
            if (!resource.exists() || !resource.isReadable()) {
                log.error("File is not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }
            
            // Determine content type
            String contentType = resume.getContentType();
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            
            log.info("Downloading resume: {} ({})", resume.getOriginalFileName(), filePath);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + resume.getOriginalFileName() + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            log.error("Error downloading resume for email {}: {}", email, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

} 