package com.resumai.resumeparserservice.dto;

import com.resumai.resumeparserservice.entity.PanelMember;
import jakarta.validation.constraints.*;

public class PanelMemberRequest {
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @NotBlank(message = "Employee ID is required")
    @Size(min = 3, max = 50, message = "Employee ID must be between 3 and 50 characters")
    private String employeeId;
    
    @NotBlank(message = "Designation is required")
    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;
    
    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
    
    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
    
    @NotBlank(message = "Expertise is required")
    @Size(max = 200, message = "Expertise must not exceed 200 characters")
    private String expertise;
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid mobile number")
    private String mobileNumber;
    
    @NotNull(message = "Availability status is required")
    private PanelMember.AvailabilityStatus availabilityStatus;
    
    // Default constructor
    public PanelMemberRequest() {}
    
    // Constructor with all fields
    public PanelMemberRequest(String name, String email, String employeeId, String designation,
                             String department, String location, String expertise, String mobileNumber,
                             PanelMember.AvailabilityStatus availabilityStatus) {
        this.name = name;
        this.email = email;
        this.employeeId = employeeId;
        this.designation = designation;
        this.department = department;
        this.location = location;
        this.expertise = expertise;
        this.mobileNumber = mobileNumber;
        this.availabilityStatus = availabilityStatus;
    }
    
    // Getters and Setters
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
    }
    
    /**
     * Convert DTO to Entity
     */
    public PanelMember toEntity() {
        PanelMember panelMember = new PanelMember();
        panelMember.setName(this.name);
        panelMember.setEmail(this.email);
        panelMember.setEmployeeId(this.employeeId);
        panelMember.setDesignation(this.designation);
        panelMember.setDepartment(this.department);
        panelMember.setLocation(this.location);
        panelMember.setExpertise(this.expertise);
        panelMember.setMobileNumber(this.mobileNumber);
        panelMember.setAvailabilityStatus(this.availabilityStatus);
        return panelMember;
    }
    
    @Override
    public String toString() {
        return "PanelMemberRequest{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", designation='" + designation + '\'' +
                ", department='" + department + '\'' +
                ", location='" + location + '\'' +
                ", expertise='" + expertise + '\'' +
                ", mobileNumber='" + mobileNumber + '\'' +
                ", availabilityStatus=" + availabilityStatus +
                '}';
    }
} 