package com.resumai.resumeparserservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_descriptions", indexes = {
    @Index(name = "idx_job_content_hash", columnList = "contentHash"),
    @Index(name = "idx_title_company", columnList = "title, company")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String company;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String responsibilities;

    private String location;

    private String experienceLevel;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    // Content hash for duplicate detection
    @Column(unique = true, length = 64)
    private String contentHash;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Method to get combined text for similarity comparison
    public String getCombinedText() {
        StringBuilder combined = new StringBuilder();
        combined.append(title).append(" ");
        combined.append(description).append(" ");
        if (requirements != null) {
            combined.append(requirements).append(" ");
        }
        if (responsibilities != null) {
            combined.append(responsibilities);
        }
        return combined.toString();
    }
} 