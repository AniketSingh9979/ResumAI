package com.resumai.resumeparserservice.repository;

import com.resumai.resumeparserservice.entity.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long> {

    Optional<JobDescription> findByTitleAndCompany(String title, String company);

    List<JobDescription> findByCompany(String company);

    List<JobDescription> findByExperienceLevel(String experienceLevel);

    List<JobDescription> findByLocation(String location);

    @Query("SELECT jd FROM JobDescription jd WHERE jd.title LIKE %:keyword% OR jd.description LIKE %:keyword%")
    List<JobDescription> findByTitleOrDescriptionContaining(@Param("keyword") String keyword);

    List<JobDescription> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(jd) FROM JobDescription jd WHERE jd.createdAt >= :date")
    Long countByCreatedAtAfter(LocalDateTime date);

    @Query("SELECT jd FROM JobDescription jd ORDER BY jd.createdAt DESC")
    List<JobDescription> findAllOrderByCreatedAtDesc();
    
    // Duplicate detection methods
    Optional<JobDescription> findByContentHash(String contentHash);
    
    @Query("SELECT jd FROM JobDescription jd WHERE jd.contentHash = :contentHash")
    Optional<JobDescription> findDuplicateByContentHash(@Param("contentHash") String contentHash);
    
    boolean existsByContentHash(String contentHash);
} 