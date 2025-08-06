package com.resumai.resumeparserservice.controller;

import com.resumai.resumeparserservice.entity.JobDescription;
import com.resumai.resumeparserservice.entity.PanelMember;
import com.resumai.resumeparserservice.repository.JobDescriptionRepository;
import com.resumai.resumeparserservice.repository.PanelMemberRepository;
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
@CrossOrigin(origins = "*")
public class JobDescriptionController {

    private final TextExtractionService textExtractionService;
    private final JobDescriptionRepository jobDescriptionRepository;
    private final JobDescriptionService jobDescriptionService;
    private final PanelMemberRepository panelMemberRepository;

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
            @RequestParam(value = "panelMemberId", required = false) Long panelMemberId,
            @RequestParam(value = "panelistName", required = false) String panelistName) {
        
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
            } else if (description != null && !description.trim().isEmpty()) {
                // If no file but description is provided, use description as extracted text for parsing
                extractedText = description;
                log.info("Using provided description as text for parsing: {} characters", description.length());
            }
            
            // Process and save job description using service
            JobDescription savedJob = jobDescriptionService.processAndSaveJobDescription(
                extractedText, title, company, description, requirements, responsibilities, location, experienceLevel, panelMemberId, panelistName);
            
            // Prepare response
            response.put("success", true);
            response.put("message", "Job description uploaded and saved successfully");
            response.put("jobId", savedJob.getId());
            response.put("job", savedJob);
            
            // Include panel member details in response if available
            if (savedJob.getPanelMember() != null) {
                Map<String, Object> panelMemberDetails = new HashMap<>();
                panelMemberDetails.put("id", savedJob.getPanelMember().getId());
                panelMemberDetails.put("name", savedJob.getPanelMember().getName());
                panelMemberDetails.put("email", savedJob.getPanelMember().getEmail());
                panelMemberDetails.put("designation", savedJob.getPanelMember().getDesignation());
                panelMemberDetails.put("department", savedJob.getPanelMember().getDepartment());
                panelMemberDetails.put("expertise", savedJob.getPanelMember().getExpertise());
                panelMemberDetails.put("availabilityStatus", savedJob.getPanelMember().getAvailabilityStatus());
                response.put("panelMember", panelMemberDetails);
            } else if (savedJob.getPanelMemberName() != null) {
                // Include basic panel member info if only name was provided
                Map<String, Object> panelMemberDetails = new HashMap<>();
                panelMemberDetails.put("name", savedJob.getPanelMemberName());
                panelMemberDetails.put("email", savedJob.getPanelMemberEmail());
                response.put("panelMember", panelMemberDetails);
            }
            
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
            log.info("Fetching all job descriptions...");
            List<JobDescription> jobs = jobDescriptionRepository.findAllOrderByCreatedAtDesc();
            log.info("Found {} job descriptions", jobs.size());
            
            // Convert to a safe format to avoid lazy loading issues
            List<Map<String, Object>> jobsList = jobs.stream().map(job -> {
                Map<String, Object> jobMap = new HashMap<>();
                jobMap.put("id", job.getId());
                jobMap.put("fileName", job.getFileName());
                jobMap.put("originalFileName", job.getOriginalFileName());
                jobMap.put("fileSize", job.getFileSize());
                jobMap.put("contentType", job.getContentType());
                jobMap.put("description", job.getDescription());
                jobMap.put("title", job.getTitle());
                jobMap.put("company", job.getCompany());
                jobMap.put("location", job.getLocation());
                jobMap.put("experienceLevel", job.getExperienceLevel());
                jobMap.put("requirements", job.getRequirements());
                jobMap.put("responsibilities", job.getResponsibilities());
                jobMap.put("createdDate", job.getCreatedDate());
                jobMap.put("updatedDate", job.getUpdatedDate());
                jobMap.put("isActive", job.getIsActive());
                
                // Handle panel member safely
                if (job.getPanelMember() != null) {
                    Map<String, Object> panelMemberMap = new HashMap<>();
                    panelMemberMap.put("id", job.getPanelMember().getId());
                    panelMemberMap.put("name", job.getPanelMember().getName());
                    panelMemberMap.put("email", job.getPanelMember().getEmail());
                    panelMemberMap.put("designation", job.getPanelMember().getDesignation());
                    panelMemberMap.put("department", job.getPanelMember().getDepartment());
                    panelMemberMap.put("expertise", job.getPanelMember().getExpertise());
                    jobMap.put("panelMember", panelMemberMap);
                } else {
                    jobMap.put("panelMemberName", job.getPanelMemberName());
                    jobMap.put("panelMemberEmail", job.getPanelMemberEmail());
                }
                
                return jobMap;
            }).toList();
            
            response.put("success", true);
            response.put("message", "Job descriptions retrieved successfully");
            response.put("totalJobs", jobsList.size());
            response.put("jobs", jobsList);
            
            log.info("Successfully returning {} job descriptions", jobsList.size());
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

    /**
     * Get all active panel members for dropdown selection
     */
    @GetMapping("/panel-members")
    public ResponseEntity<Map<String, Object>> getAllPanelMembers() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<PanelMember> panelMembers = panelMemberRepository.findByIsActiveTrue();
            
            response.put("success", true);
            response.put("message", "Panel members retrieved successfully");
            response.put("totalPanelMembers", panelMembers.size());
            response.put("panelMembers", panelMembers);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error retrieving panel members: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to retrieve panel members: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
} 