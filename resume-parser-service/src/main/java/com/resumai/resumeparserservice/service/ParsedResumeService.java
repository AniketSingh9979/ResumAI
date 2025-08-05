package com.resumai.resumeparserservice.service;

import com.resumai.resumeparserservice.entity.ParsedResume;
import com.resumai.resumeparserservice.repository.ParsedResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.io.IOException;
import org.apache.tika.exception.TikaException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParsedResumeService {

    private final ParsedResumeRepository parsedResumeRepository;
    private final SimilarityService similarityService;
    private final TextExtractionService textExtractionService;
    private final ResumeMatchingService resumeMatchingService;
    private final HashingService hashingService;
    
    private static final String UPLOAD_DIR = "uploaded-resumes/";

    /**
     * Save a parsed resume with all extracted information
     */
    public ParsedResume saveParsedResume(MultipartFile file, String extractedText, 
                                       String email, Set<String> skills, 
                                       String experience, Double score, String filePath) {
        ParsedResume parsedResume = new ParsedResume();
        
        // Basic file information
        parsedResume.setOriginalFileName(file.getOriginalFilename());
        parsedResume.setFileSize(file.getSize());
        parsedResume.setContentType(file.getContentType());
        
        // Extracted content
        parsedResume.setRawText(extractedText);
        parsedResume.setParsedText(extractedText); // Can be processed differently if needed
        parsedResume.setEmail(email);
        parsedResume.setSkills(String.join(", ", skills));
        parsedResume.setExperience(experience);
        parsedResume.setScore(score);
        
        // File storage path
        parsedResume.setFilePath(filePath);
        
        // Generate and set content hash
        try {
            String contentHash = hashingService.generateFileHash(file);
            parsedResume.setContentHash(contentHash);
        } catch (IOException e) {
            log.error("Failed to generate content hash for file: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("Failed to generate content hash", e);
        }
        
        // Timestamp
        parsedResume.setUploadTime(LocalDateTime.now());
        
        ParsedResume saved = parsedResumeRepository.save(parsedResume);
        log.info("Saved parsed resume with ID: {} for file: {}", saved.getId(), saved.getOriginalFileName());
        
        return saved;
    }



    /**
     * Update the score for a parsed resume
     */
    public ParsedResume updateScore(Long resumeId, Double score) {
        Optional<ParsedResume> resumeOpt = parsedResumeRepository.findById(resumeId);
        if (resumeOpt.isPresent()) {
            ParsedResume resume = resumeOpt.get();
            resume.setScore(score);
            return parsedResumeRepository.save(resume);
        }
        throw new IllegalArgumentException("ParsedResume not found with ID: " + resumeId);
    }

    /**
     * Find resume by ID
     */
    public Optional<ParsedResume> findById(Long id) {
        return parsedResumeRepository.findById(id);
    }

    /**
     * Find resumes by email
     */
    public List<ParsedResume> findByEmail(String email) {
        return parsedResumeRepository.findByEmail(email);
    }

    /**
     * Find resumes by skill
     */
    public List<ParsedResume> findBySkill(String skill) {
        return parsedResumeRepository.findBySkillsContaining(skill);
    }

    /**
     * Find resumes by score range
     */
    public List<ParsedResume> findByScoreRange(Double minScore, Double maxScore) {
        return parsedResumeRepository.findByScoreBetween(minScore, maxScore);
    }

    /**
     * Get all resumes ordered by upload time (newest first)
     */
    public List<ParsedResume> findAllOrderByUploadTime() {
        return parsedResumeRepository.findAllOrderByUploadTimeDesc();
    }



    /**
     * Get resumes uploaded within a date range
     */
    public List<ParsedResume> findByUploadTimeRange(LocalDateTime startDate, LocalDateTime endDate) {
        return parsedResumeRepository.findByUploadTimeBetween(startDate, endDate);
    }

    /**
     * Search resumes by text content
     */
    public List<ParsedResume> searchByText(String keyword) {
        return parsedResumeRepository.findByTextContaining(keyword);
    }

    /**
     * Get statistics about parsed resumes
     */
    public ResumeStatistics getStatistics() {
        List<ParsedResume> allResumes = parsedResumeRepository.findAll();
        long totalCount = allResumes.size();
        
        double averageScore = allResumes.stream()
                .filter(r -> r.getScore() != null)
                .mapToDouble(ParsedResume::getScore)
                .average()
                .orElse(0.0);
        
        long resumesWithEmail = allResumes.stream()
                .filter(r -> r.getEmail() != null && !r.getEmail().trim().isEmpty())
                .count();
        
        long resumesWithSkills = allResumes.stream()
                .filter(r -> r.getSkills() != null && !r.getSkills().trim().isEmpty())
                .count();
        
        return new ResumeStatistics(totalCount, averageScore, resumesWithEmail, resumesWithSkills);
    }

    /**
     * Delete a parsed resume
     */
    public void deleteById(Long id) {
        parsedResumeRepository.deleteById(id);
        log.info("Deleted ParsedResume with ID: {}", id);
    }

    /**
     * Extract email from text using simple regex
     */
    private String extractEmail(String text) {
        if (text == null) return null;
        
        java.util.regex.Pattern emailPattern = java.util.regex.Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"
        );
        java.util.regex.Matcher matcher = emailPattern.matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    /**
     * Extract experience information from text
     */
    private String extractExperience(String text) {
        if (text == null) return null;
        
        // Simple experience extraction - look for patterns with "years" or "experience"
        java.util.regex.Pattern expPattern = java.util.regex.Pattern.compile(
            "(?i)(\\d+)\\s*(?:years?|yrs?)\\s*(?:of\\s*)?(?:experience|exp)", 
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        java.util.regex.Matcher matcher = expPattern.matcher(text);
        
        if (matcher.find()) {
            return matcher.group() + " years of experience";
        }
        
        // Look for experience sections
        String[] lines = text.split("\n");
        for (String line : lines) {
            if (line.toLowerCase().contains("experience") && line.length() < 200) {
                return line.trim();
            }
        }
        
        return null;
    }

    /**
     * Statistics container class
     */
    public static class ResumeStatistics {
        public final long totalResumes;
        public final double averageScore;
        public final long resumesWithEmail;
        public final long resumesWithSkills;

        public ResumeStatistics(long totalResumes, double averageScore, 
                              long resumesWithEmail, long resumesWithSkills) {
            this.totalResumes = totalResumes;
            this.averageScore = averageScore;
            this.resumesWithEmail = resumesWithEmail;
            this.resumesWithSkills = resumesWithSkills;
        }
    }

    // ============================================================================
    // RESUME PROCESSING HELPER METHODS
    // ============================================================================

    /**
     * Validate the uploaded file and check for duplicates
     */
    public void validateUploadedFile(MultipartFile file) throws IOException, TikaException {
        // Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Please select a file to upload");
        }

        // Validate file type (Resume uploads support PDF, DOC, DOCX, RTF, and TXT)
        String detectedType = textExtractionService.detectFileType(file);
        if (!textExtractionService.isSupportedResumeFileType(detectedType)) {
            throw new IllegalArgumentException("Invalid file type. Resume uploads support PDF, DOC, DOCX, RTF, and TXT files. Detected: " + detectedType);
        }
        
        // Check for duplicate content
        String contentHash = hashingService.generateFileHash(file);
        Optional<ParsedResume> existingResume = parsedResumeRepository.findByContentHash(contentHash);
        
        if (existingResume.isPresent()) {
            ParsedResume duplicate = existingResume.get();
            String message = String.format("This resume has already been uploaded. " +
                "Original file: '%s' was uploaded on %s (Resume ID: %d). " +
                "Duplicate uploads are not allowed.",
                duplicate.getOriginalFileName(),
                duplicate.getUploadTime().toString(),
                duplicate.getId());
            log.warn("Duplicate resume upload attempt. File: {}, Original: {}", 
                    file.getOriginalFilename(), duplicate.getOriginalFileName());
            throw new IllegalArgumentException(message);
        }
        
        log.info("Resume validation passed for file: {}", file.getOriginalFilename());
    }

    /**
     * Save uploaded file to disk with unique filename
     */
    public Path saveFileToDisk(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename and save file to disk
        String originalFilename = file.getOriginalFilename();
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File saved to: {} -> {}", originalFilename, filePath.toString());
        
        return filePath;
    }

    /**
     * Extract text and parse resume data (email, skills, experience)
     */
    public ResumeProcessingData extractAndParseResumeData(MultipartFile file) throws IOException, TikaException {
        // Extract text from resume
        String extractedText = textExtractionService.extractTextFromFile(file);
        log.info("Extracted {} characters from resume: {}", extractedText.length(), file.getOriginalFilename());

        // Parse resume data (extract email, skills, experience)
        String email = extractEmail(extractedText);
        Set<String> skills = similarityService.extractSkills(extractedText);
        String experience = extractExperience(extractedText);

        return new ResumeProcessingData(extractedText, email, skills, experience);
    }

    /**
     * Score resume against all job descriptions
     */
    public MatchingResults scoreResumeAgainstJobs(String extractedText) {
        // Match resume against ALL job descriptions and get best score
        List<Map<String, Object>> allMatches = resumeMatchingService.matchResumeWithAllJobs(extractedText);
        Double bestScore = 0.0;
        Map<String, Object> bestMatch = null;
        
        if (!allMatches.isEmpty()) {
            bestMatch = allMatches.get(0); // First one is highest score
            bestScore = (Double) bestMatch.get("overallScore");
        }

        return new MatchingResults(allMatches, bestScore, bestMatch);
    }

    /**
     * Save parsed resume data to database - enhanced version with file path
     */
    public ParsedResume saveProcessedResumeToDatabase(MultipartFile file, ResumeProcessingData resumeData, 
                                                     MatchingResults matchingResults, Path filePath) {
        ParsedResume savedResume = saveParsedResume(
            file, resumeData.extractedText, resumeData.email, resumeData.skills, 
            resumeData.experience, matchingResults.bestScore, filePath.toString()
        );

        log.info("Resume processed and saved with ID: {} for file: {}", 
                savedResume.getId(), file.getOriginalFilename());
        
        return savedResume;
    }

    // ============================================================================
    // DATA TRANSFER OBJECTS FOR RESUME PROCESSING
    // ============================================================================

    /**
     * Data class to hold extracted resume information
     */
    public static class ResumeProcessingData {
        public final String extractedText;
        public final String email;
        public final Set<String> skills;
        public final String experience;

        public ResumeProcessingData(String extractedText, String email, Set<String> skills, String experience) {
            this.extractedText = extractedText;
            this.email = email;
            this.skills = skills;
            this.experience = experience;
        }
    }

    /**
     * Data class to hold job matching results
     */
    public static class MatchingResults {
        public final List<Map<String, Object>> allMatches;
        public final Double bestScore;
        public final Map<String, Object> bestMatch;

        public MatchingResults(List<Map<String, Object>> allMatches, Double bestScore, Map<String, Object> bestMatch) {
            this.allMatches = allMatches;
            this.bestScore = bestScore;
            this.bestMatch = bestMatch;
        }
    }
} 