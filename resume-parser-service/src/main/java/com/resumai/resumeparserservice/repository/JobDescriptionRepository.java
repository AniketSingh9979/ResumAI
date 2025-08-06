package com.resumai.resumeparserservice.repository;

import com.resumai.resumeparserservice.entity.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobDescriptionRepository extends JpaRepository<JobDescription, Long>, JpaSpecificationExecutor<JobDescription> {

    /**
     * Find all active job descriptions
     */
    List<JobDescription> findByIsActiveTrue();

    /**
     * Find all active job descriptions ordered by creation date descending
     */
    @Query("SELECT jd FROM JobDescription jd LEFT JOIN FETCH jd.panelMember WHERE jd.isActive = true ORDER BY jd.createdDate DESC")
    List<JobDescription> findAllOrderByCreatedAtDesc();

    /**
     * Find job descriptions by panel member ID
     */
    @Query("SELECT jd FROM JobDescription jd WHERE jd.panelMember.id = :panelMemberId AND jd.isActive = true")
    List<JobDescription> findByPanelMemberIdAndIsActiveTrue(@Param("panelMemberId") Long panelMemberId);

    /**
     * Find job descriptions by panel member name
     */
    List<JobDescription> findByPanelMemberNameContainingIgnoreCaseAndIsActiveTrue(String panelMemberName);

    /**
     * Find job descriptions by file name
     */
    List<JobDescription> findByOriginalFileNameContainingIgnoreCaseAndIsActiveTrue(String fileName);

    /**
     * Find job descriptions created between dates
     */
    List<JobDescription> findByCreatedDateBetweenAndIsActiveTrue(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find job descriptions by content type
     */
    List<JobDescription> findByContentTypeAndIsActiveTrue(String contentType);

    /**
     * Custom query to search job descriptions by multiple criteria
     */
    @Query("SELECT jd FROM JobDescription jd WHERE jd.isActive = true AND " +
           "(LOWER(jd.originalFileName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(jd.panelMemberName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(jd.panelMemberEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(jd.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<JobDescription> searchJobDescriptions(@Param("searchTerm") String searchTerm);

    /**
     * Count active job descriptions
     */
    long countByIsActiveTrue();

    /**
     * Count job descriptions by panel member
     */
    @Query("SELECT COUNT(jd) FROM JobDescription jd WHERE jd.panelMember.id = :panelMemberId AND jd.isActive = true")
    long countByPanelMemberAndIsActiveTrue(@Param("panelMemberId") Long panelMemberId);

    /**
     * Find job description by file path (for duplicate prevention)
     */
    Optional<JobDescription> findByFilePathAndIsActiveTrue(String filePath);

    /**
     * Find recent job descriptions (last N days)
     */
    @Query("SELECT jd FROM JobDescription jd WHERE jd.isActive = true AND " +
           "jd.createdDate >= :fromDate ORDER BY jd.createdDate DESC")
    List<JobDescription> findRecentJobDescriptions(@Param("fromDate") LocalDateTime fromDate);

    /**
     * Find top uploaders (panel members with most job descriptions)
     */
    @Query("SELECT jd.panelMemberName, jd.panelMemberEmail, COUNT(jd) as uploadCount " +
           "FROM JobDescription jd WHERE jd.isActive = true AND jd.panelMember IS NOT NULL " +
           "GROUP BY jd.panelMember.id, jd.panelMemberName, jd.panelMemberEmail " +
           "ORDER BY COUNT(jd) DESC")
    List<Object[]> findTopUploaders();
} 