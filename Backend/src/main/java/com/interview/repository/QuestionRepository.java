package com.interview.repository;

import com.interview.entity.Question;
import com.interview.entity.Question.ExperienceLevel;
import com.interview.entity.Question.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository interface for Question entity
 * Provides methods for fetching questions based on various criteria
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    // Find questions by domain and experience level
    List<Question> findByDomainAndExperienceLevelAndActiveTrue(String domain, ExperienceLevel experienceLevel);

    // Find questions by type
    List<Question> findByQuestionTypeAndActiveTrue(QuestionType questionType);

    // Find questions by domain and type
    List<Question> findByDomainAndQuestionTypeAndActiveTrue(String domain, QuestionType questionType);

    // Find questions by domain, experience level, and type
    List<Question> findByDomainAndExperienceLevelAndQuestionTypeAndActiveTrue(
            String domain, ExperienceLevel experienceLevel, QuestionType questionType);

    // Get random questions by type and experience level
    @Query("SELECT q FROM Question q WHERE q.domain = :domain AND q.experienceLevel = :experienceLevel " +
           "AND q.questionType = :questionType AND q.active = true ORDER BY RANDOM()")
    List<Question> findRandomQuestionsByDomainAndExperienceAndType(
            @Param("domain") String domain,
            @Param("experienceLevel") ExperienceLevel experienceLevel,
            @Param("questionType") QuestionType questionType);

    // Get limited random questions
    @Query(value = "SELECT * FROM questions WHERE domain = :domain AND experience_level = :experienceLevel " +
                   "AND question_type = :questionType AND active = true ORDER BY RANDOM() LIMIT :limit", 
           nativeQuery = true)
    List<Question> findRandomQuestionsWithLimit(
            @Param("domain") String domain,
            @Param("experienceLevel") String experienceLevel,
            @Param("questionType") String questionType,
            @Param("limit") int limit);

    // Find all active questions by domain
    List<Question> findByDomainAndActiveTrue(String domain);

    // Find distinct domains
    @Query("SELECT DISTINCT q.domain FROM Question q WHERE q.active = true")
    List<String> findDistinctDomains();

    // Find questions suitable for experience level (including adjacent levels)
    @Query("SELECT q FROM Question q WHERE q.domain = :domain AND q.questionType = :questionType " +
           "AND q.active = true AND (q.experienceLevel = :experienceLevel OR " +
           "ABS(CAST(q.experienceLevel AS integer) - CAST(:experienceLevel AS integer)) <= 1)")
    List<Question> findSuitableQuestionsForExperience(
            @Param("domain") String domain,
            @Param("questionType") QuestionType questionType,
            @Param("experienceLevel") ExperienceLevel experienceLevel);

    // Count questions by criteria
    long countByDomainAndExperienceLevelAndQuestionTypeAndActiveTrue(
            String domain, ExperienceLevel experienceLevel, QuestionType questionType);

    // Find questions by tags
    @Query("SELECT q FROM Question q WHERE q.tags LIKE %:tag% AND q.active = true")
    List<Question> findByTagsContaining(@Param("tag") String tag);

    // Get most used questions
    @Query("SELECT q FROM Question q WHERE q.active = true ORDER BY q.usageCount DESC")
    List<Question> findMostUsedQuestions();

    // Get questions with highest average scores
    @Query("SELECT q FROM Question q WHERE q.active = true AND q.usageCount > 0 ORDER BY q.averageScore DESC")
    List<Question> findHighestScoredQuestions();
} 