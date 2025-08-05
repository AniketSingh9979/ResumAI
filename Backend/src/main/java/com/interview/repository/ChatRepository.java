package com.interview.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.interview.entity.ChatMessage;

/**
 * Repository interface for ChatMessage entity
 * Provides methods for managing Q&A chat interactions
 */
@Repository
public interface ChatRepository extends JpaRepository<ChatMessage, UUID> {

    // Find chat messages by resume ID
    List<ChatMessage> findByResumeIdOrderByTimestampAsc(String resumeId);

    // Find chat messages by session ID
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId);

    // Find chat messages by domain
    List<ChatMessage> findByDomainOrderByTimestampDesc(String domain);

    // Find correct answers by resume ID
    List<ChatMessage> findByResumeIdAndIsCorrectAnswerTrueOrderByTimestampAsc(String resumeId);

    // Find chat messages within time range
    List<ChatMessage> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startTime, LocalDateTime endTime);

    // Count correct answers by resume ID
    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.resumeId = :resumeId AND c.isCorrectAnswer = true")
    long countCorrectAnswersByResumeId(@Param("resumeId") String resumeId);

    // Count total questions by resume ID
    long countByResumeId(String resumeId);

    // Get average similarity score by resume ID
    @Query("SELECT AVG(c.similarity) FROM ChatMessage c WHERE c.resumeId = :resumeId AND c.similarity IS NOT NULL")
    Double getAverageSimilarityByResumeId(@Param("resumeId") String resumeId);

    // Get chat history by resume ID with pagination
    @Query("SELECT c FROM ChatMessage c WHERE c.resumeId = :resumeId ORDER BY c.timestamp ASC")
    List<ChatMessage> getChatHistoryByResumeId(@Param("resumeId") String resumeId);

    // Find flagged messages
    List<ChatMessage> findByFlaggedTrueOrderByTimestampDesc();

    // Find messages with low similarity scores
    @Query("SELECT c FROM ChatMessage c WHERE c.similarity < :threshold AND c.similarity IS NOT NULL ORDER BY c.similarity ASC")
    List<ChatMessage> findMessagesWithLowSimilarity(@Param("threshold") float threshold);

    // Find messages by domain and time range
    @Query("SELECT c FROM ChatMessage c WHERE c.domain = :domain AND c.timestamp BETWEEN :startTime AND :endTime ORDER BY c.timestamp DESC")
    List<ChatMessage> findByDomainAndTimestampBetween(
            @Param("domain") String domain,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    // Get response time statistics
    @Query("SELECT AVG(c.responseTimeMs), MIN(c.responseTimeMs), MAX(c.responseTimeMs) FROM ChatMessage c WHERE c.responseTimeMs IS NOT NULL")
    Object[] getResponseTimeStatistics();

    // Delete old chat messages (cleanup)
    void deleteByTimestampBefore(LocalDateTime cutoffTime);

    // Find recent evaluation for duplicate prevention
    @Query("SELECT c FROM ChatMessage c WHERE c.resumeId = :resumeId AND c.userQuestion LIKE %:questionText% AND c.timestamp > :since ORDER BY c.timestamp DESC")
    Optional<ChatMessage> findRecentEvaluationByResumeAndQuestion(
            @Param("resumeId") String resumeId,
            @Param("questionText") String questionText,
            @Param("since") LocalDateTime since);
} 