package com.resumai.resumeparserservice.controller;

import com.resumai.resumeparserservice.entity.ParsedResume;
import com.resumai.resumeparserservice.service.ParsedResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
@Slf4j
public class ParsedResumeController {

    private final ParsedResumeService parsedResumeService;

    /**
     * Get all parsed resumes
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllResumes() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ParsedResume> resumes = parsedResumeService.findAllOrderByUploadTime();
            
            response.put("success", true);
            response.put("message", "Resumes retrieved successfully");
            response.put("totalCount", resumes.size());
            response.put("resumes", resumes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving resumes: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve resumes: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get a specific resume by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getResumeById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<ParsedResume> resumeOpt = parsedResumeService.findById(id);
            
            if (resumeOpt.isPresent()) {
                response.put("success", true);
                response.put("message", "Resume retrieved successfully");
                response.put("resume", resumeOpt.get());
            } else {
                response.put("success", false);
                response.put("message", "Resume not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving resume: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve resume: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Search resumes by email
     */
    @GetMapping("/search/email/{email}")
    public ResponseEntity<Map<String, Object>> searchByEmail(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ParsedResume> resumes = parsedResumeService.findByEmail(email);
            
            response.put("success", true);
            response.put("message", "Resumes found by email");
            response.put("email", email);
            response.put("count", resumes.size());
            response.put("resumes", resumes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching by email: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to search by email: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Search resumes by skill
     */
    @GetMapping("/search/skill/{skill}")
    public ResponseEntity<Map<String, Object>> searchBySkill(@PathVariable String skill) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ParsedResume> resumes = parsedResumeService.findBySkill(skill);
            
            response.put("success", true);
            response.put("message", "Resumes found by skill");
            response.put("skill", skill);
            response.put("count", resumes.size());
            response.put("resumes", resumes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching by skill: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to search by skill: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Search resumes by score range
     */
    @GetMapping("/search/score")
    public ResponseEntity<Map<String, Object>> searchByScore(
            @RequestParam(required = false, defaultValue = "0.0") Double minScore,
            @RequestParam(required = false, defaultValue = "100.0") Double maxScore) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ParsedResume> resumes = parsedResumeService.findByScoreRange(minScore, maxScore);
            
            response.put("success", true);
            response.put("message", "Resumes found by score range");
            response.put("minScore", minScore);
            response.put("maxScore", maxScore);
            response.put("count", resumes.size());
            response.put("resumes", resumes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching by score: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to search by score: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Search resumes by text content
     */
    @GetMapping("/search/text/{keyword}")
    public ResponseEntity<Map<String, Object>> searchByText(@PathVariable String keyword) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ParsedResume> resumes = parsedResumeService.searchByText(keyword);
            
            response.put("success", true);
            response.put("message", "Resumes found by text search");
            response.put("keyword", keyword);
            response.put("count", resumes.size());
            response.put("resumes", resumes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching by text: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to search by text: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get resumes within a date range
     */
    @GetMapping("/search/date-range")
    public ResponseEntity<Map<String, Object>> searchByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<ParsedResume> resumes = parsedResumeService.findByUploadTimeRange(startDate, endDate);
            
            response.put("success", true);
            response.put("message", "Resumes found by date range");
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("count", resumes.size());
            response.put("resumes", resumes);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error searching by date range: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to search by date range: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get resume statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            ParsedResumeService.ResumeStatistics stats = parsedResumeService.getStatistics();
            
            response.put("success", true);
            response.put("message", "Statistics retrieved successfully");
            response.put("statistics", Map.of(
                "totalResumes", stats.totalResumes,
                "averageScore", Math.round(stats.averageScore * 100.0) / 100.0,
                "resumesWithEmail", stats.resumesWithEmail,
                "resumesWithSkills", stats.resumesWithSkills,
                "completionRate", stats.totalResumes > 0 ? 
                    Math.round((double) stats.resumesWithEmail / stats.totalResumes * 100.0) : 0
            ));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving statistics: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve statistics: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Update score for a resume
     */
    @PutMapping("/{id}/score")
    public ResponseEntity<Map<String, Object>> updateScore(
            @PathVariable Long id, 
            @RequestBody Map<String, Double> request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Double score = request.get("score");
            if (score == null) {
                response.put("success", false);
                response.put("message", "Score value is required");
                return ResponseEntity.badRequest().body(response);
            }
            
            ParsedResume updatedResume = parsedResumeService.updateScore(id, score);
            
            response.put("success", true);
            response.put("message", "Score updated successfully");
            response.put("resume", updatedResume);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error updating score: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to update score: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete a resume
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteResume(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            parsedResumeService.deleteById(id);
            
            response.put("success", true);
            response.put("message", "Resume deleted successfully");
            response.put("deletedId", id);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error deleting resume: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to delete resume: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Download original resume file
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long id) {
        try {
            Optional<ParsedResume> resumeOpt = parsedResumeService.findById(id);
            
            if (resumeOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            ParsedResume resume = resumeOpt.get();
            String filePath = resume.getFilePath();
            
            if (filePath == null || filePath.trim().isEmpty()) {
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
            log.error("Error downloading resume with ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

} 