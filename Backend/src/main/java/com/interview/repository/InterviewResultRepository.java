package com.interview.repository;

import com.interview.entity.InterviewResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for InterviewResult entity
 * Provides methods for managing interview results and analytics
 */
@Repository
public interface InterviewResultRepository extends JpaRepository<InterviewResult, UUID> {

    // Find results by resume ID
    List<InterviewResult> findByResumeIdOrderByInterviewTimeDesc(String resumeId);

    // Find results by candidate name
    List<InterviewResult> findByCandidateNameContainingIgnoreCaseOrderByInterviewTimeDesc(String candidateName);

    // Find results by domain
    List<InterviewResult> findByDomainOrderByInterviewTimeDesc(String domain);

    // Find results by experience level
    List<InterviewResult> findByExperienceLevelOrderByInterviewTimeDesc(String experienceLevel);

    // Find results by domain and experience level
    List<InterviewResult> findByDomainAndExperienceLevelOrderByInterviewTimeDesc(String domain, String experienceLevel);

    // Find results within time range
    List<InterviewResult> findByInterviewTimeBetweenOrderByInterviewTimeDesc(LocalDateTime startTime, LocalDateTime endTime);

    // Find results by status
    List<InterviewResult> findByStatusOrderByInterviewTimeDesc(String status);

    // Find latest result by resume ID
    Optional<InterviewResult> findTopByResumeIdOrderByInterviewTimeDesc(String resumeId);

    // Get average score by domain
    @Query("SELECT AVG(ir.scorePercentage) FROM InterviewResult ir WHERE ir.domain = :domain AND ir.scorePercentage IS NOT NULL")
    Double getAverageScoreByDomain(@Param("domain") String domain);

    // Get average score by experience level
    @Query("SELECT AVG(ir.scorePercentage) FROM InterviewResult ir WHERE ir.experienceLevel = :experienceLevel AND ir.scorePercentage IS NOT NULL")
    Double getAverageScoreByExperienceLevel(@Param("experienceLevel") String experienceLevel);

    // Count results by domain
    long countByDomain(String domain);

    // Count results by experience level
    long countByExperienceLevel(String experienceLevel);

    // Find top performers
    @Query("SELECT ir FROM InterviewResult ir WHERE ir.scorePercentage >= :minScore ORDER BY ir.scorePercentage DESC")
    List<InterviewResult> findTopPerformers(@Param("minScore") float minScore);

    // Find results with high scores in specific domain
    @Query("SELECT ir FROM InterviewResult ir WHERE ir.domain = :domain AND ir.scorePercentage >= :minScore ORDER BY ir.scorePercentage DESC")
    List<InterviewResult> findTopPerformersByDomain(@Param("domain") String domain, @Param("minScore") float minScore);

    // Get pass rate by domain (assuming passing score is 60%)
    @Query("SELECT (COUNT(CASE WHEN ir.scorePercentage >= 60 THEN 1 END) * 100.0 / COUNT(*)) " +
           "FROM InterviewResult ir WHERE ir.domain = :domain AND ir.scorePercentage IS NOT NULL")
    Double getPassRateByDomain(@Param("domain") String domain);

    // Get domain statistics
    @Query("SELECT ir.domain, COUNT(*), AVG(ir.scorePercentage), MIN(ir.scorePercentage), MAX(ir.scorePercentage) " +
           "FROM InterviewResult ir WHERE ir.scorePercentage IS NOT NULL GROUP BY ir.domain")
    List<Object[]> getDomainStatistics();

    // Find recent interviews (last 30 days)
    @Query("SELECT ir FROM InterviewResult ir WHERE ir.interviewTime >= :cutoffDate ORDER BY ir.interviewTime DESC")
    List<InterviewResult> findRecentInterviews(@Param("cutoffDate") LocalDateTime cutoffDate);

    // Get experience level distribution
    @Query("SELECT ir.experienceLevel, COUNT(*) FROM InterviewResult ir GROUP BY ir.experienceLevel")
    List<Object[]> getExperienceLevelDistribution();

    // Find incomplete interviews
    @Query("SELECT ir FROM InterviewResult ir WHERE ir.status != 'COMPLETED' ORDER BY ir.interviewTime DESC")
    List<InterviewResult> findIncompleteInterviews();

    // Get average interview duration by domain
    @Query("SELECT AVG(ir.interviewDuration) FROM InterviewResult ir WHERE ir.domain = :domain AND ir.interviewDuration IS NOT NULL")
    Double getAverageInterviewDurationByDomain(@Param("domain") String domain);

    // Find interviews that exceeded time limit
    @Query("SELECT ir FROM InterviewResult ir WHERE ir.completedOnTime = false ORDER BY ir.interviewTime DESC")
    List<InterviewResult> findInterviewsExceedingTimeLimit();
} 