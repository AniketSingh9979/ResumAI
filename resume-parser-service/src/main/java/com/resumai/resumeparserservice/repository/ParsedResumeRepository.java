package com.resumai.resumeparserservice.repository;

import com.resumai.resumeparserservice.entity.ParsedResume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ParsedResumeRepository extends JpaRepository<ParsedResume, Long> {

    Optional<ParsedResume> findByOriginalFileName(String originalFileName);

    List<ParsedResume> findByEmail(String email);

    List<ParsedResume> findByUploadTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<ParsedResume> findByScoreGreaterThanEqual(Double minScore);

    List<ParsedResume> findByScoreBetween(Double minScore, Double maxScore);

    @Query("SELECT p FROM ParsedResume p WHERE p.skills LIKE %:skill%")
    List<ParsedResume> findBySkillsContaining(@Param("skill") String skill);

    @Query("SELECT p FROM ParsedResume p WHERE p.experience LIKE %:keyword%")
    List<ParsedResume> findByExperienceContaining(@Param("keyword") String keyword);

    @Query("SELECT COUNT(p) FROM ParsedResume p WHERE p.uploadTime >= :date")
    Long countByUploadTimeAfter(@Param("date") LocalDateTime date);

    @Query("SELECT p FROM ParsedResume p ORDER BY p.uploadTime DESC")
    List<ParsedResume> findAllOrderByUploadTimeDesc();

    @Query("SELECT p FROM ParsedResume p ORDER BY p.score DESC")
    List<ParsedResume> findAllOrderByScoreDesc();

    @Query("SELECT AVG(p.score) FROM ParsedResume p WHERE p.uploadTime >= :date")
    Double getAverageScoreAfter(@Param("date") LocalDateTime date);

    @Query("SELECT p FROM ParsedResume p WHERE p.parsedText LIKE %:keyword% OR p.rawText LIKE %:keyword%")
    List<ParsedResume> findByTextContaining(@Param("keyword") String keyword);
    
    // Duplicate detection methods
    Optional<ParsedResume> findByContentHash(String contentHash);
    
    @Query("SELECT p FROM ParsedResume p WHERE p.contentHash = :contentHash")
    Optional<ParsedResume> findDuplicateByContentHash(@Param("contentHash") String contentHash);
    
    boolean existsByContentHash(String contentHash);
} 