package com.resumai.resumeparserservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "panel_members", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "email"),
           @UniqueConstraint(columnNames = "employeeId")
       })
public class PanelMember {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    
    @NotBlank(message = "Employee ID is required")
    @Size(min = 3, max = 50, message = "Employee ID must be between 3 and 50 characters")
    @Column(name = "employee_id", nullable = false, unique = true, length = 50)
    private String employeeId;
    
    @NotBlank(message = "Designation is required")
    @Size(max = 100, message = "Designation must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String designation;
    
    @NotBlank(message = "Department is required")
    @Size(max = 100, message = "Department must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String department;
    
    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    @Column(nullable = false, length = 100)
    private String location;
    
    @NotBlank(message = "Expertise is required")
    @Size(max = 200, message = "Expertise must not exceed 200 characters")
    @Column(nullable = false, length = 200)
    private String expertise;
    
    @NotBlank(message = "Mobile number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid mobile number")
    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;
    
    @NotNull(message = "Availability status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", nullable = false)
    private AvailabilityStatus availabilityStatus;
    
    @CreationTimestamp
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @UpdateTimestamp
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Enum for availability status
    public enum AvailabilityStatus {
        AVAILABLE("Available"),
        BUSY("Busy"),
        ON_LEAVE("On Leave"),
        UNAVAILABLE("Unavailable");
        
        private final String displayName;
        
        AvailabilityStatus(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Default constructor
    public PanelMember() {}
    
    // Constructor with required fields
    public PanelMember(String name, String email, String employeeId, String designation, 
                      String department, String location, String expertise, String mobileNumber, 
                      AvailabilityStatus availabilityStatus) {
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
    
    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }
    
    public void setAvailabilityStatus(AvailabilityStatus availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
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
        return "PanelMember{" +
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