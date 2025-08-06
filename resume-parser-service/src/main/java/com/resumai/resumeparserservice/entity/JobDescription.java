package com.resumai.resumeparserservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_descriptions")
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name cannot exceed 255 characters")
    @Column(name = "file_name", nullable = false)
    private String fileName;

    @NotBlank(message = "Original file name is required")
    @Size(max = 255, message = "Original file name cannot exceed 255 characters")
    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @NotBlank(message = "File path is required")
    @Size(max = 500, message = "File path cannot exceed 500 characters")
    @Column(name = "file_path", nullable = false)
    private String filePath;

    @NotNull(message = "File size is required")
    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @NotBlank(message = "Content type is required")
    @Size(max = 100, message = "Content type cannot exceed 100 characters")
    @Column(name = "content_type", nullable = false)
    private String contentType;

    // Relationship to PanelMember entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "panel_member_id", nullable = true, foreignKey = @ForeignKey(name = "fk_job_description_panel_member"))
    private PanelMember panelMember;

    // Keep legacy fields for backward compatibility and denormalized access
    @Column(name = "panel_member_name", nullable = true)
    private String panelMemberName;

    @Column(name = "panel_member_email", nullable = true)
    private String panelMemberEmail;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Job-specific fields with proper database columns
    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "company", length = 150)
    private String company;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "experience_level", length = 50)
    private String experienceLevel;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date", nullable = false)
    private LocalDateTime updatedDate;

    // Default constructor
    public JobDescription() {}

    // Constructor with required fields
    public JobDescription(String fileName, String originalFileName, String filePath, 
                         Long fileSize, String contentType, Long panelMemberId, 
                         String panelMemberName, String panelMemberEmail) {
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
        this.contentType = contentType;
        // Note: panelMemberId parameter is deprecated, use setPanelMember() instead
        this.panelMemberName = panelMemberName;
        this.panelMemberEmail = panelMemberEmail;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getPanelMemberId() {
        return panelMember != null ? panelMember.getId() : null;
    }

    public void setPanelMemberId(Long panelMemberId) {
        // This method is kept for backward compatibility but now it's a no-op
        // Use setPanelMember() instead to set the actual relationship
    }

    public String getPanelMemberName() {
        return panelMemberName;
    }

    public void setPanelMemberName(String panelMemberName) {
        this.panelMemberName = panelMemberName;
    }

    public String getPanelMemberEmail() {
        return panelMemberEmail;
    }

    public void setPanelMemberEmail(String panelMemberEmail) {
        this.panelMemberEmail = panelMemberEmail;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    // Additional getter methods for extracted metadata from description
    /**
     * Extract title from description field
     */
    public String getTitle() {
        // Return the direct field value if available, otherwise extract from description
        if (title != null && !title.trim().isEmpty()) {
            return title;
        }
        
        if (description == null) return null;
        String[] lines = description.split("\n");
        for (String line : lines) {
            if (line.startsWith("Title: ")) {
                return line.substring(7).trim();
            }
        }
        return null;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Extract company from description field
     */
    public String getCompany() {
        // Return the direct field value if available, otherwise extract from description
        if (company != null && !company.trim().isEmpty()) {
            return company;
        }
        
        if (description == null) return null;
        String[] lines = description.split("\n");
        for (String line : lines) {
            if (line.startsWith("Company: ")) {
                return line.substring(9).trim();
            }
        }
        return null;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * Extract location from description field
     */
    public String getLocation() {
        // Return the direct field value if available, otherwise extract from description
        if (location != null && !location.trim().isEmpty()) {
            return location;
        }
        
        if (description == null) return null;
        String[] lines = description.split("\n");
        for (String line : lines) {
            if (line.startsWith("Location: ")) {
                return line.substring(10).trim();
            }
        }
        return null;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Extract experience level from description field
     */
    public String getExperienceLevel() {
        // Return the direct field value if available, otherwise extract from description
        if (experienceLevel != null && !experienceLevel.trim().isEmpty()) {
            return experienceLevel;
        }
        
        if (description == null) return null;
        String[] lines = description.split("\n");
        for (String line : lines) {
            if (line.startsWith("Experience Level: ")) {
                return line.substring(18).trim();
            }
        }
        return null;
    }

    public void setExperienceLevel(String experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    /**
     * Extract requirements from description field
     */
    public String getRequirements() {
        // Return the direct field value if available, otherwise extract from description
        if (requirements != null && !requirements.trim().isEmpty()) {
            return requirements;
        }
        
        if (description == null) return null;
        int requirementsStart = description.indexOf("Requirements:\n");
        if (requirementsStart == -1) return null;
        
        int nextSectionStart = description.indexOf("\n\n", requirementsStart + 13);
        if (nextSectionStart == -1) {
            return description.substring(requirementsStart + 13).trim();
        } else {
            return description.substring(requirementsStart + 13, nextSectionStart).trim();
        }
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    /**
     * Extract responsibilities from description field
     */
    public String getResponsibilities() {
        // Return the direct field value if available, otherwise extract from description
        if (responsibilities != null && !responsibilities.trim().isEmpty()) {
            return responsibilities;
        }
        
        if (description == null) return null;
        int responsibilitiesStart = description.indexOf("Responsibilities:\n");
        if (responsibilitiesStart == -1) return null;
        
        int nextSectionStart = description.indexOf("\n\n", responsibilitiesStart + 17);
        if (nextSectionStart == -1) {
            return description.substring(responsibilitiesStart + 17).trim();
        } else {
            return description.substring(responsibilitiesStart + 17, nextSectionStart).trim();
        }
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    /**
     * Get combined text for similarity calculations
     */
    public String getCombinedText() {
        StringBuilder combined = new StringBuilder();
        
        String title = getTitle();
        if (title != null && !title.trim().isEmpty()) {
            combined.append(title).append(" ");
        }
        
        String company = getCompany();
        if (company != null && !company.trim().isEmpty()) {
            combined.append(company).append(" ");
        }
        
        if (description != null && !description.trim().isEmpty()) {
            combined.append(description).append(" ");
        }
        
        String requirements = getRequirements();
        if (requirements != null && !requirements.trim().isEmpty()) {
            combined.append(requirements).append(" ");
        }
        
        String responsibilities = getResponsibilities();
        if (responsibilities != null && !responsibilities.trim().isEmpty()) {
            combined.append(responsibilities).append(" ");
        }
        
        return combined.toString().trim();
    }

    // New getter and setter for PanelMember relationship
    public PanelMember getPanelMember() {
        return panelMember;
    }

    public void setPanelMember(PanelMember panelMember) {
        this.panelMember = panelMember;
        // Automatically sync denormalized fields
        if (panelMember != null) {
            this.panelMemberName = panelMember.getName();
            this.panelMemberEmail = panelMember.getEmail();
        } else {
            this.panelMemberName = null;
            this.panelMemberEmail = null;
        }
    }

    @Override
    public String toString() {
        return "JobDescription{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", originalFileName='" + originalFileName + '\'' +
                ", fileSize=" + fileSize +
                ", contentType='" + contentType + '\'' +
                ", panelMemberId=" + getPanelMemberId() +
                ", panelMemberName='" + panelMemberName + '\'' +
                ", panelMemberEmail='" + panelMemberEmail + '\'' +
                ", isActive=" + isActive +
                ", createdDate=" + createdDate +
                '}';
    }
} 