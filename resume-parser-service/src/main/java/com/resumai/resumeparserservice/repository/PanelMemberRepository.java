package com.resumai.resumeparserservice.repository;

import com.resumai.resumeparserservice.entity.PanelMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PanelMemberRepository extends JpaRepository<PanelMember, Long> {
    
    /**
     * Find panel member by email
     */
    Optional<PanelMember> findByEmail(String email);
    
    /**
     * Find panel member by employee ID
     */
    Optional<PanelMember> findByEmployeeId(String employeeId);
    
    /**
     * Find all active panel members
     */
    List<PanelMember> findByIsActiveTrue();
    
    /**
     * Find panel members by availability status
     */
    List<PanelMember> findByAvailabilityStatusAndIsActiveTrue(PanelMember.AvailabilityStatus availabilityStatus);
    
    /**
     * Find panel members by department
     */
    List<PanelMember> findByDepartmentAndIsActiveTrue(String department);
    
    /**
     * Find panel members by location
     */
    List<PanelMember> findByLocationAndIsActiveTrue(String location);
    
    /**
     * Find panel members by expertise (case-insensitive partial match)
     */
    @Query("SELECT p FROM PanelMember p WHERE LOWER(p.expertise) LIKE LOWER(CONCAT('%', :expertise, '%')) AND p.isActive = true")
    List<PanelMember> findByExpertiseContainingIgnoreCaseAndIsActiveTrue(@Param("expertise") String expertise);
    
    /**
     * Find panel members by name (case-insensitive partial match)
     */
    @Query("SELECT p FROM PanelMember p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isActive = true")
    List<PanelMember> findByNameContainingIgnoreCaseAndIsActiveTrue(@Param("name") String name);
    
    /**
     * Check if email already exists (excluding current record)
     */
    @Query("SELECT COUNT(p) > 0 FROM PanelMember p WHERE p.email = :email AND p.id != :excludeId")
    boolean existsByEmailAndIdNot(@Param("email") String email, @Param("excludeId") Long excludeId);
    
    /**
     * Check if employee ID already exists (excluding current record)
     */
    @Query("SELECT COUNT(p) > 0 FROM PanelMember p WHERE p.employeeId = :employeeId AND p.id != :excludeId")
    boolean existsByEmployeeIdAndIdNot(@Param("employeeId") String employeeId, @Param("excludeId") Long excludeId);
    
    /**
     * Get count of panel members by department
     */
    @Query("SELECT p.department, COUNT(p) FROM PanelMember p WHERE p.isActive = true GROUP BY p.department")
    List<Object[]> countByDepartment();
    
    /**
     * Get count of panel members by availability status
     */
    @Query("SELECT p.availabilityStatus, COUNT(p) FROM PanelMember p WHERE p.isActive = true GROUP BY p.availabilityStatus")
    List<Object[]> countByAvailabilityStatus();
} 