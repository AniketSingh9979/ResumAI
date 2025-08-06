package com.resumai.resumeparserservice.dto;

import com.resumai.resumeparserservice.entity.PanelMember;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public class PanelMemberResponse {
    
    private Long id;
    private String name;
    private String email;
    private String employeeId;
    private String designation;
    private String department;
    private String location;
    private String expertise;
    private String mobileNumber;
    private PanelMember.AvailabilityStatus availabilityStatus;
    private String availabilityStatusDisplay;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedDate;
    
    private Boolean isActive;
    
    // Default constructor
    public PanelMemberResponse() {}
    
    // Constructor from Entity
    public PanelMemberResponse(PanelMember panelMember) {
        this.id = panelMember.getId();
        this.name = panelMember.getName();
        this.email = panelMember.getEmail();
        this.employeeId = panelMember.getEmployeeId();
        this.designation = panelMember.getDesignation();
        this.department = panelMember.getDepartment();
        this.location = panelMember.getLocation();
        this.expertise = panelMember.getExpertise();
        this.mobileNumber = panelMember.getMobileNumber();
        this.availabilityStatus = panelMember.getAvailabilityStatus();
        this.availabilityStatusDisplay = panelMember.getAvailabilityStatus().getDisplayName();
        this.createdDate = panelMember.getCreatedDate();
        this.updatedDate = panelMember.getUpdatedDate();
        this.isActive = panelMember.getIsActive();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getDesignation() {
        return designation;
    }
    
    public void setDesignation(String designation) {
        this.designation = designation;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getExpertise() {
        return expertise;
    }
    
    public void setExpertise(String expertise) {
        this.expertise = expertise;
    }
    
    public String getMobileNumber() {
        return mobileNumber;
    }
    
    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }
    
    public PanelMember.AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }
    
    public void setAvailabilityStatus(PanelMember.AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
        if (availabilityStatus != null) {
            this.availabilityStatusDisplay = availabilityStatus.getDisplayName();
        }
    }
    
    public String getAvailabilityStatusDisplay() {
        return availabilityStatusDisplay;
    }
    
    public void setAvailabilityStatusDisplay(String availabilityStatusDisplay) {
        this.availabilityStatusDisplay = availabilityStatusDisplay;
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
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    @Override
    public String toString() {
        return "PanelMemberResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", designation='" + designation + '\'' +
                ", department='" + department + '\'' +
                ", location='" + location + '\'' +
                ", expertise='" + expertise + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", availabilityStatus=" + availabilityStatus +
                ", createdDate=" + createdDate +
                ", isActive=" + isActive +
                '}';
    }
} 