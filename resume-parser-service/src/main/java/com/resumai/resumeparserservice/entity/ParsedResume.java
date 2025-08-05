package com.resumai.resumeparserservice.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "parsed_resumes", indexes = {
    @Index(name = "idx_content_hash", columnList = "contentHash"),
    @Index(name = "idx_email", columnList = "email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalFileName;

    private String email;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String skills;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String experience;

    private Double score;

    @Column(nullable = false)
    private LocalDateTime uploadTime;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String parsedText;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String rawText;

    // Additional fields for metadata
    private Long fileSize;
    private String contentType;
    
    // File storage path
    private String filePath;
    
    // Content hash for duplicate detection
    @Column(unique = true, length = 64)
    private String contentHash;

    @PrePersist
    protected void onCreate() {
        if (uploadTime == null) {
            uploadTime = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // uploadTime should not change on update
    }
} 