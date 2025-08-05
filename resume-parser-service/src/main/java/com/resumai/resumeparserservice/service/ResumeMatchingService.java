package com.resumai.resumeparserservice.service;

import com.resumai.resumeparserservice.entity.JobDescription;
import com.resumai.resumeparserservice.repository.JobDescriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeMatchingService {

    private final JobDescriptionRepository jobDescriptionRepository;
    private final SimilarityService similarityService;



    /**
     * Match a resume against all job descriptions in the database
     */
    public List<Map<String, Object>> matchResumeWithAllJobs(String resumeText) {
        List<JobDescription> allJobs = jobDescriptionRepository.findAll();
        
        return allJobs.stream()
                .map(job -> calculateJobMatch(resumeText, job))
                .sorted((a, b) -> Double.compare((Double) b.get("overallScore"), (Double) a.get("overallScore")))
                .collect(Collectors.toList());
    }







    /**
     * Calculate comprehensive matching score for a job
     */
    private Map<String, Object> calculateJobMatch(String resumeText, JobDescription job) {
        Map<String, Object> result = new HashMap<>();
        
        // Basic job information
        result.put("jobId", job.getId());
        result.put("jobTitle", job.getTitle());
        result.put("company", job.getCompany());
        result.put("location", job.getLocation());
        result.put("experienceLevel", job.getExperienceLevel());
        
        // Calculate different similarity scores
        double overallSimilarity = similarityService.calculateSimilarity(resumeText, job.getCombinedText());
        double weightedSimilarity = similarityService.calculateWeightedSimilarity(
            resumeText, 
            job.getTitle(), 
            job.getDescription(), 
            job.getRequirements(), 
            job.getResponsibilities()
        );
        // Use direct skill matching for better JD to Resume comparison
        double skillMatch = similarityService.calculateDirectSkillMatch(resumeText, job.getCombinedText());
        
        // Calculate title-specific match
        double titleMatch = similarityService.calculateSimilarity(resumeText, job.getTitle());
        
        // Calculate requirements match if available - using direct skill matching
        double requirementsMatch = 0.0;
        if (job.getRequirements() != null && !job.getRequirements().trim().isEmpty()) {
            requirementsMatch = similarityService.calculateDirectSkillMatch(resumeText, job.getRequirements());
        }
        
        // Calculate final overall score (weighted combination)
        double overallScore = calculateOverallScore(overallSimilarity, weightedSimilarity, skillMatch, titleMatch, requirementsMatch);
        
        // Extract matching skills
        Set<String> resumeSkills = similarityService.extractSkills(resumeText);
        Set<String> jobSkills = similarityService.extractSkills(job.getCombinedText());
        Set<String> matchingSkills = new HashSet<>(resumeSkills);
        matchingSkills.retainAll(jobSkills);
        
        // Add scores to result
        result.put("overallScore", Math.round(overallScore * 10000.0) / 100.0); // Convert to percentage
        result.put("overallSimilarity", Math.round(overallSimilarity * 10000.0) / 100.0);
        result.put("weightedSimilarity", Math.round(weightedSimilarity * 10000.0) / 100.0);
        result.put("skillMatchPercentage", Math.round(skillMatch * 10000.0) / 100.0);
        result.put("titleMatch", Math.round(titleMatch * 10000.0) / 100.0);
        result.put("requirementsMatch", Math.round(requirementsMatch * 10000.0) / 100.0);
        
        // Add skill information
        result.put("resumeSkills", resumeSkills);
        result.put("jobSkills", jobSkills);
        result.put("matchingSkills", matchingSkills);
        result.put("skillsFound", resumeSkills.size());
        result.put("skillsRequired", jobSkills.size());
        result.put("skillsMatched", matchingSkills.size());
        
        // Add match category
        result.put("matchCategory", getMatchCategory(overallScore));
        
        log.info("Calculated match score {} for job '{}' at '{}'", 
                overallScore, job.getTitle(), job.getCompany());
        
        return result;
    }

    /**
     * Calculate overall score using weighted combination of different similarity measures
     */
    private double calculateOverallScore(double overallSimilarity, double weightedSimilarity, 
                                       double skillMatch, double titleMatch, double requirementsMatch) {
        // Weights for different components
        double overallWeight = 0.25;
        double weightedWeight = 0.25;
        double skillWeight = 0.30;
        double titleWeight = 0.10;
        double requirementsWeight = 0.10;
        
        return (overallSimilarity * overallWeight) +
               (weightedSimilarity * weightedWeight) +
               (skillMatch * skillWeight) +
               (titleMatch * titleWeight) +
               (requirementsMatch * requirementsWeight);
    }

    /**
     * Categorize match quality based on score
     */
    private String getMatchCategory(double score) {
        if (score >= 0.8) return "Excellent Match";
        if (score >= 0.6) return "Good Match";
        if (score >= 0.4) return "Fair Match";
        if (score >= 0.2) return "Poor Match";
        return "No Match";
    }


} 