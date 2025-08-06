package com.resumai.resumeparserservice.controller;

import com.resumai.resumeparserservice.entity.JobDescription;
import com.resumai.resumeparserservice.repository.JobDescriptionRepository;
import com.resumai.resumeparserservice.service.TextExtractionService;
import com.resumai.resumeparserservice.service.JobDescriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/matching")
@RequiredArgsConstructor
@Slf4j
public class JobDescriptionController {

    private final TextExtractionService textExtractionService;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final JobDescriptionService jobDescriptionService;

    /**
     * Upload job description file or text and store it in the database
     * This is one of the 2 main endpoints for the streamlined API
     */
    @PostMapping("/uploadJD")
    public ResponseEntity<Map<String, Object>> uploadJobDescription(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "company", required = false) String company,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "requirements", required = false) String requirements,
            @RequestParam(value = "responsibilities", required = false) String responsibilities,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "experienceLevel", required = false) String experienceLevel,
            @RequestParam(value = "panelistName", required = true) String panelistName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            String extractedText = null;
            String detectedContentType = null;
            
            // If file is provided, extract text from it
            if (file != null && !file.isEmpty()) {
                // Validate file type for job descriptions
                detectedContentType = textExtractionService.detectFileType(file);
                if (!textExtractionService.isSupportedJobDescriptionFileType(detectedContentType)) {
                    response.put("success", false);
                    response.put("message", "Unsupported file type for job descriptions");
                    response.put("detectedType", detectedContentType);
                    response.put("supportedTypes", "PDF, DOC, DOCX, RTF, TXT");
                    return ResponseEntity.badRequest().body(response);
                }
                
                extractedText = textExtractionService.extractTextFromFile(file);
                log.info("Extracted {} characters from job description file: {}", 
                        extractedText.length(), file.getOriginalFilename());
            }
            
            // Process and save job description using service
            JobDescription savedJob = jobDescriptionService.processAndSaveJobDescription(
                extractedText, title, company, description, requirements, responsibilities, location, experienceLevel, panelistName);
            
            // Prepare response
            response.put("success", true);
            response.put("message", "Job description uploaded and saved successfully");
            response.put("jobId", savedJob.getId());
            response.put("job", savedJob);
            
            if (file != null) {
                response.put("originalFileName", file.getOriginalFilename());
                response.put("fileSize", file.getSize());
                response.put("detectedContentType", detectedContentType);
                response.put("extractedTextLength", extractedText != null ? extractedText.length() : 0);
            }
            
            return ResponseEntity.ok(response);
            
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
            log.error("Error saving job description: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to save job description: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get all job descriptions
     */
    @GetMapping("/jobs")
    public ResponseEntity<Map<String, Object>> getAllJobs() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<JobDescription> jobs = jobDescriptionRepository.findAllOrderByCreatedAtDesc();
            
            response.put("success", true);
            response.put("message", "Job descriptions retrieved successfully");
            response.put("totalJobs", jobs.size());
            response.put("jobs", jobs);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving job descriptions: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve job descriptions: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get paginated job descriptions with sorting and filtering
     */
    @GetMapping("/jobs/paginated")
    public ResponseEntity<Map<String, Object>> getJobsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) String panelistName) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Create Sort object
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            // Create Pageable
            Pageable pageable = PageRequest.of(page, size, sort);
            
            // Get jobs with filtering
            Page<JobDescription> jobsPage = jobDescriptionService.getJobsPagedAndFiltered(
                pageable, title, company, location, experienceLevel, panelistName);
            
            response.put("success", true);
            response.put("message", "Job descriptions retrieved successfully");
            response.put("jobs", jobsPage.getContent());
            response.put("currentPage", jobsPage.getNumber());
            response.put("totalPages", jobsPage.getTotalPages());
            response.put("totalElements", jobsPage.getTotalElements());
            response.put("pageSize", jobsPage.getSize());
            response.put("hasNext", jobsPage.hasNext());
            response.put("hasPrevious", jobsPage.hasPrevious());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving paginated job descriptions: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve job descriptions: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Get a specific job description by ID
     */
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobById(@PathVariable Long jobId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Optional<JobDescription> jobOpt = jobDescriptionRepository.findById(jobId);
            
            if (jobOpt.isPresent()) {
            response.put("success", true);
            response.put("message", "Job description retrieved successfully");
                response.put("job", jobOpt.get());
            } else {
                response.put("success", false);
                response.put("message", "Job description not found with ID: " + jobId);
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving job description: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve job description: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 