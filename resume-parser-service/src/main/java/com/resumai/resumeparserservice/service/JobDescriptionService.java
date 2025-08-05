package com.resumai.resumeparserservice.service;

import com.resumai.resumeparserservice.entity.JobDescription;
import com.resumai.resumeparserservice.repository.JobDescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobDescriptionService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final HashingService hashingService;

    /**
     * Parse job description from text and parameters, then save to database
     */
    public JobDescription processAndSaveJobDescription(String extractedText, String title, String company,
                                                      String description, String requirements, String responsibilities,
                                                      String location, String experienceLevel) {
        // Parse job description
        JobDescription jobDescription = parseJobDescription(extractedText, title, company, 
                description, requirements, responsibilities, location, experienceLevel);
        
        // Validate required fields
        if (jobDescription.getTitle() == null || jobDescription.getTitle().trim().isEmpty() ||
            jobDescription.getCompany() == null || jobDescription.getCompany().trim().isEmpty()) {
            throw new IllegalArgumentException("Job title and company are required");
        }
        
        // Check for duplicate job descriptions
        String contentHash = hashingService.generateJobDescriptionHash(
            jobDescription.getTitle(),
            jobDescription.getCompany(),
            jobDescription.getDescription(),
            jobDescription.getRequirements(),
            jobDescription.getResponsibilities()
        );
        
        Optional<JobDescription> existingJob = jobDescriptionRepository.findByContentHash(contentHash);
        if (existingJob.isPresent()) {
            JobDescription duplicate = existingJob.get();
            String message = String.format("This job description has already been uploaded. " +
                "Original job: '%s' at '%s' was created on %s (Job ID: %d). " +
                "Duplicate job descriptions are not allowed.",
                duplicate.getTitle(),
                duplicate.getCompany(),
                duplicate.getCreatedAt().toString(),
                duplicate.getId());
            log.warn("Duplicate job description upload attempt. Title: {}, Company: {}, Original ID: {}", 
                    jobDescription.getTitle(), jobDescription.getCompany(), duplicate.getId());
            throw new IllegalArgumentException(message);
        }
        
        // Set the content hash
        jobDescription.setContentHash(contentHash);
        
        // Save to database
        JobDescription savedJob = jobDescriptionRepository.save(jobDescription);
        
        log.info("Job description saved with ID: {} for company: {}", 
                savedJob.getId(), savedJob.getCompany());
        
        return savedJob;
    }

    /**
     * Helper method to parse job description from text and parameters
     */
    private JobDescription parseJobDescription(String extractedText, String title, String company,
                                             String description, String requirements, String responsibilities,
                                             String location, String experienceLevel) {
        JobDescription jobDescription = new JobDescription();
        
        // Use provided parameters first, fall back to parsing from text
        jobDescription.setTitle(title != null ? title : extractTitleFromText(extractedText));
        jobDescription.setCompany(company != null ? company : extractCompanyFromText(extractedText));
        jobDescription.setDescription(description != null ? description : extractedText);
        jobDescription.setRequirements(requirements != null ? requirements : extractRequirementsFromText(extractedText));
        jobDescription.setResponsibilities(responsibilities != null ? responsibilities : extractResponsibilitiesFromText(extractedText));
        jobDescription.setLocation(location != null ? location : extractLocationFromText(extractedText));
        jobDescription.setExperienceLevel(experienceLevel != null ? experienceLevel : extractExperienceLevel(extractedText));
        
        // Set timestamp
        jobDescription.setCreatedAt(LocalDateTime.now());
        
        return jobDescription;
    }

    /**
     * Extract job title from text
     */
    private String extractTitleFromText(String text) {
        if (text == null) return null;
        
        // Look for common job title patterns
        String[] lines = text.split("\n");
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.length() > 5 && trimmedLine.length() < 100) {
                // Simple heuristic: first substantial line might be the title
                return trimmedLine;
            }
        }
        return null;
    }

    /**
     * Extract company name from text
     */
    private String extractCompanyFromText(String text) {
        if (text == null) return null;
        
        // Look for company patterns
        Pattern companyPattern = Pattern.compile(
            "(?i)company:?\\s*([\\w\\s&.,'-]+?)(?:\\n|$|\\.|,)"
        );
        Matcher matcher = companyPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }

    /**
     * Extract requirements from text
     */
    private String extractRequirementsFromText(String text) {
        if (text == null) return null;
        
        // Look for requirements sections
        Pattern reqPattern = Pattern.compile(
            "(?i)(?:requirements?|qualifications?):?\\s*([\\s\\S]*?)(?=(?:responsibilities?|benefits?|about|$))",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = reqPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }

    /**
     * Extract responsibilities from text
     */
    private String extractResponsibilitiesFromText(String text) {
        if (text == null) return null;
        
        // Look for responsibilities sections
        Pattern respPattern = Pattern.compile(
            "(?i)(?:responsibilities?|duties?|role):?\\s*([\\s\\S]*?)(?=(?:requirements?|qualifications?|benefits?|about|$))",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = respPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }

    /**
     * Extract location from text
     */
    private String extractLocationFromText(String text) {
        if (text == null) return null;
        
        // Look for location patterns
        Pattern locationPattern = Pattern.compile(
            "(?i)(?:location|based in|office):?\\s*([\\w\\s,'-]+?)(?:\\n|$|\\.|;)"
        );
        Matcher matcher = locationPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        
        return null;
    }

    /**
     * Extract experience level from text
     */
    private String extractExperienceLevel(String text) {
        if (text == null) return null;
        
        // Look for experience patterns
        Pattern expPattern = Pattern.compile(
            "(?i)(\\d+)\\s*(?:[-+])?\\s*(?:years?|yrs?)\\s*(?:of\\s*)?(?:experience|exp)",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = expPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group() + " experience";
        }
        
        // Look for level indicators
        if (text.toLowerCase().contains("senior")) return "Senior";
        if (text.toLowerCase().contains("junior")) return "Junior";
        if (text.toLowerCase().contains("entry level")) return "Entry Level";
        if (text.toLowerCase().contains("mid level")) return "Mid Level";
        
        return null;
    }
} 