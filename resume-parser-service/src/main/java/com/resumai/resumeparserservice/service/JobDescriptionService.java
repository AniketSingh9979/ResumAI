package com.resumai.resumeparserservice.service;

import com.resumai.resumeparserservice.entity.JobDescription;
import com.resumai.resumeparserservice.entity.PanelMember;
import com.resumai.resumeparserservice.repository.JobDescriptionRepository;
import com.resumai.resumeparserservice.repository.PanelMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobDescriptionService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final PanelMemberRepository panelMemberRepository;
    private final JobDescriptionParsingService parsingService;

    @Value("${app.upload.dir:uploaded-job-descriptions}")
    private String uploadDir;

    /**
     * Save job description with file upload
     */
    public JobDescription saveJobDescription(MultipartFile file, Long panelMemberId, 
                                           String panelMemberName, String panelMemberEmail) throws IOException {
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique file name
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String uniqueFileName = UUID.randomUUID().toString() + "_" + 
                              System.currentTimeMillis() + fileExtension;
        
        // Save file to disk
        Path filePath = uploadPath.resolve(uniqueFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Create job description entity
        JobDescription jobDescription = new JobDescription();
        jobDescription.setFileName(uniqueFileName);
        jobDescription.setOriginalFileName(originalFileName);
        jobDescription.setFilePath(filePath.toString());
        jobDescription.setFileSize(file.getSize());
        jobDescription.setContentType(file.getContentType());
        
        // Handle panel member association
        if (panelMemberId != null) {
            Optional<PanelMember> panelMemberOpt = panelMemberRepository.findById(panelMemberId);
            if (panelMemberOpt.isPresent()) {
                jobDescription.setPanelMember(panelMemberOpt.get());
                log.info("Associated job description with panel member: {} (ID: {})", 
                        panelMemberOpt.get().getName(), panelMemberId);
            } else {
                // Set legacy fields if panel member not found but name/email provided
                jobDescription.setPanelMemberName(panelMemberName);
                jobDescription.setPanelMemberEmail(panelMemberEmail);
                log.warn("Panel member not found with ID: {}, using provided name and email", panelMemberId);
            }
        } else {
            // Set legacy fields when no ID provided
            jobDescription.setPanelMemberName(panelMemberName);
            jobDescription.setPanelMemberEmail(panelMemberEmail);
        }
        
        jobDescription.setIsActive(true);

        // Try to extract text content for description (basic implementation)
        try {
            String description = extractTextFromFile(file, file.getContentType());
            jobDescription.setDescription(description);
        } catch (Exception e) {
            // If text extraction fails, just log and continue
            log.error("Could not extract text from file: {}", e.getMessage());
        }

        // Save to database
        return jobDescriptionRepository.save(jobDescription);
    }

    /**
     * Process and save job description with extracted text and metadata
     */
    public JobDescription processAndSaveJobDescription(String extractedText, String title, String company, 
                                                     String description, String requirements, String responsibilities, 
                                                     String location, String experienceLevel, Long panelMemberId, String panelistName) {
        
        log.info("Processing job description with extracted text of {} characters", 
                extractedText != null ? extractedText.length() : 0);
        
        // Parse fields from extracted text if they're not provided manually
        Map<String, String> parsedFields = null;
        if (extractedText != null && !extractedText.trim().isEmpty()) {
            log.info("Parsing fields from extracted text...");
            parsedFields = parsingService.parseJobDescriptionFields(extractedText);
            log.info("Parsed fields from text: {}", parsedFields.keySet());
            
            // Debug: Show what was parsed for requirements and responsibilities
            if (parsedFields.containsKey("requirements")) {
                log.info("Parsed requirements: {}", parsedFields.get("requirements").substring(0, Math.min(100, parsedFields.get("requirements").length())) + "...");
            } else {
                log.warn("No requirements parsed from text");
            }
            
            if (parsedFields.containsKey("responsibilities")) {
                log.info("Parsed responsibilities: {}", parsedFields.get("responsibilities").substring(0, Math.min(100, parsedFields.get("responsibilities").length())) + "...");
            } else {
                log.warn("No responsibilities parsed from text");
            }
        } else {
            log.warn("No extracted text available for parsing");
        }
        
        // Create job description entity
        JobDescription jobDescription = new JobDescription();
        
        // Handle panel member association
        if (panelMemberId != null) {
            Optional<PanelMember> panelMemberOpt = panelMemberRepository.findById(panelMemberId);
            if (panelMemberOpt.isPresent()) {
                jobDescription.setPanelMember(panelMemberOpt.get());
                log.info("Associated job description with panel member: {} (ID: {})", 
                        panelMemberOpt.get().getName(), panelMemberId);
            } else {
                throw new IllegalArgumentException("Panel member not found with ID: " + panelMemberId);
            }
        } else if (panelistName != null && !panelistName.trim().isEmpty()) {
            // Fallback: set only the name if no ID provided
            jobDescription.setPanelMemberName(panelistName);
        }
        
        jobDescription.setIsActive(true);
        jobDescription.setCreatedDate(LocalDateTime.now());
        
        // Set individual fields with intelligent fallback to parsed values
        jobDescription.setTitle(getFieldValue(title, parsedFields, "title", "Untitled Position"));
        jobDescription.setCompany(getFieldValue(company, parsedFields, "company", "Unknown Company"));
        jobDescription.setLocation(getFieldValue(location, parsedFields, "location", "Location TBD"));
        jobDescription.setExperienceLevel(getFieldValue(experienceLevel, parsedFields, "experienceLevel", "Experience level not specified"));
        jobDescription.setRequirements(getFieldValue(requirements, parsedFields, "requirements", null));
        jobDescription.setResponsibilities(getFieldValue(responsibilities, parsedFields, "responsibilities", null));
        
        // Process description field - include all metadata in description for backward compatibility
        StringBuilder fullDescription = new StringBuilder();
        
        // Use extracted text if available
        if (extractedText != null && !extractedText.trim().isEmpty()) {
            fullDescription.append("Extracted Content:\n").append(extractedText).append("\n\n");
        }
        
        // Add provided description
        if (description != null && !description.trim().isEmpty()) {
            fullDescription.append(description);
        } else {
            // If no explicit description provided, create a summary from the fields
            fullDescription.append("Job Title: ").append(jobDescription.getTitle()).append("\n");
            fullDescription.append("Company: ").append(jobDescription.getCompany()).append("\n");
            fullDescription.append("Location: ").append(jobDescription.getLocation()).append("\n");
            fullDescription.append("Experience: ").append(jobDescription.getExperienceLevel()).append("\n");
            
            if (jobDescription.getRequirements() != null) {
                fullDescription.append("\nRequirements:\n").append(jobDescription.getRequirements()).append("\n");
            }
            
            if (jobDescription.getResponsibilities() != null) {
                fullDescription.append("\nResponsibilities:\n").append(jobDescription.getResponsibilities());
            }
        }
        
        jobDescription.setDescription(fullDescription.toString().trim());
        
        // Set default values for required fields that aren't provided
        jobDescription.setFileName("job_description_" + System.currentTimeMillis());
        jobDescription.setOriginalFileName(jobDescription.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".txt");
        jobDescription.setFilePath("/virtual/job_descriptions/" + jobDescription.getFileName());
        jobDescription.setFileSize(extractedText != null ? (long) extractedText.length() : 0L);
        jobDescription.setContentType("text/plain");
        
        // Save to database
        JobDescription saved = jobDescriptionRepository.save(jobDescription);
        log.info("Saved job description with ID: {} and title: '{}'", saved.getId(), saved.getTitle());
        
        return saved;
    }
    
    /**
     * Helper method to get field value with fallback logic
     */
    private String getFieldValue(String providedValue, Map<String, String> parsedFields, String fieldKey, String defaultValue) {
        // Priority: 1. Provided value, 2. Parsed value, 3. Default value
        if (providedValue != null && !providedValue.trim().isEmpty()) {
            log.debug("Using provided value for {}: {}", fieldKey, providedValue.trim());
            return providedValue.trim();
        }
        
        if (parsedFields != null && parsedFields.containsKey(fieldKey)) {
            String parsedValue = parsedFields.get(fieldKey);
            if (parsedValue != null && !parsedValue.trim().isEmpty()) {
                log.info("Using parsed value for {}: {}", fieldKey, parsedValue.trim().substring(0, Math.min(50, parsedValue.trim().length())) + "...");
                return parsedValue.trim();
            }
        }
        
        if (defaultValue != null) {
            log.debug("Using default value for {}: {}", fieldKey, defaultValue);
        } else {
            log.debug("No value available for {}, setting to null", fieldKey);
        }
        
        return defaultValue;
    }

    /**
     * Get all active job descriptions
     */
    public List<JobDescription> getAllJobDescriptions() {
        return jobDescriptionRepository.findByIsActiveTrue();
    }

    /**
     * Get job description by ID
     */
    public Optional<JobDescription> getJobDescriptionById(Long id) {
        return jobDescriptionRepository.findById(id);
    }

    /**
     * Get job descriptions by panel member ID
     */
    public List<JobDescription> getJobDescriptionsByPanelMember(Long panelMemberId) {
        return jobDescriptionRepository.findByPanelMemberIdAndIsActiveTrue(panelMemberId);
    }

    /**
     * Search job descriptions
     */
    public List<JobDescription> searchJobDescriptions(String searchTerm) {
        return jobDescriptionRepository.searchJobDescriptions(searchTerm);
    }

    /**
     * Delete job description (soft delete)
     */
    public void deleteJobDescription(Long id) {
        Optional<JobDescription> jobDescriptionOpt = jobDescriptionRepository.findById(id);
        if (jobDescriptionOpt.isPresent()) {
            JobDescription jobDescription = jobDescriptionOpt.get();
            jobDescription.setIsActive(false);
            jobDescriptionRepository.save(jobDescription);
            
            // Optionally delete the physical file
            try {
                Path filePath = Paths.get(jobDescription.getFilePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            } catch (IOException e) {
                log.error("Could not delete file: {}", e.getMessage());
            }
        } else {
            throw new RuntimeException("Job description not found with id: " + id);
        }
    }

    /**
     * Get recent job descriptions (last N days)
     */
    public List<JobDescription> getRecentJobDescriptions(int days) {
        LocalDateTime fromDate = LocalDateTime.now().minusDays(days);
        return jobDescriptionRepository.findRecentJobDescriptions(fromDate);
    }

    /**
     * Get job descriptions count
     */
    public long getJobDescriptionsCount() {
        return jobDescriptionRepository.countByIsActiveTrue();
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    /**
     * Basic text extraction from file (can be enhanced with Apache Tika)
     */
    private String extractTextFromFile(MultipartFile file, String contentType) throws IOException {
        // For now, just return basic info
        // In a real implementation, you would use Apache Tika or other libraries
        // to extract text from PDF, DOC, DOCX files
        
        if (contentType != null && contentType.equals("text/plain")) {
            return new String(file.getBytes());
        }
        
        // For other file types, return basic metadata
        return String.format("File: %s, Size: %d bytes, Type: %s", 
                           file.getOriginalFilename(), file.getSize(), contentType);
    }

    /**
     * Check if file already exists (duplicate prevention)
     */
    public boolean isFileAlreadyUploaded(String filePath) {
        return jobDescriptionRepository.findByFilePathAndIsActiveTrue(filePath).isPresent();
    }

    /**
     * Update job description
     */
    public JobDescription updateJobDescription(Long id, JobDescription updatedJobDescription) {
        Optional<JobDescription> existingOpt = jobDescriptionRepository.findById(id);
        if (existingOpt.isPresent()) {
            JobDescription existing = existingOpt.get();
            
            // Update allowed fields
            if (updatedJobDescription.getDescription() != null) {
                existing.setDescription(updatedJobDescription.getDescription());
            }
            if (updatedJobDescription.getPanelMemberName() != null) {
                existing.setPanelMemberName(updatedJobDescription.getPanelMemberName());
            }
            if (updatedJobDescription.getPanelMemberEmail() != null) {
                existing.setPanelMemberEmail(updatedJobDescription.getPanelMemberEmail());
            }
            
            return jobDescriptionRepository.save(existing);
        } else {
            throw new RuntimeException("Job description not found with id: " + id);
        }
    }

    /**
     * Get job descriptions with pagination and filtering
     */
    public Page<JobDescription> getJobsPagedAndFiltered(Pageable pageable, String title, String company, 
                                                       String location, String experienceLevel, String panelistName) {
        
        Specification<JobDescription> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Only active job descriptions
            predicates.add(criteriaBuilder.equal(root.get("isActive"), true));
            
            // Filter by title using the proper title column
            if (title != null && !title.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("title")), 
                    "%" + title.toLowerCase() + "%"
                ));
            }
            
            // Filter by company using the proper company column
            if (company != null && !company.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("company")), 
                    "%" + company.toLowerCase() + "%"
                ));
            }
            
            // Filter by location using the proper location column
            if (location != null && !location.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("location")), 
                    "%" + location.toLowerCase() + "%"
                ));
            }
            
            // Filter by experience level using the proper experienceLevel column
            if (experienceLevel != null && !experienceLevel.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("experienceLevel")), 
                    "%" + experienceLevel.toLowerCase() + "%"
                ));
            }
            
            // Filter by panelist name
            if (panelistName != null && !panelistName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("panelMemberName")), 
                    "%" + panelistName.toLowerCase() + "%"
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        return jobDescriptionRepository.findAll(spec, pageable);
    }
} 