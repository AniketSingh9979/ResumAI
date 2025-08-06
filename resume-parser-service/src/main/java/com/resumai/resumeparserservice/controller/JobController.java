package com.resumai.resumeparserservice.controller;

import com.resumai.resumeparserservice.dto.ApiResponse;
import com.resumai.resumeparserservice.entity.JobDescription;
import com.resumai.resumeparserservice.service.JobDescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
public class JobController {

    @Autowired
    private JobDescriptionService jobDescriptionService;

    // Note: For uploading job descriptions, use JobDescriptionController at /api/matching/uploadJD
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<JobDescription>>> getAllJobDescriptions() {
        try {
            List<JobDescription> jobDescriptions = jobDescriptionService.getAllJobDescriptions();
            return ResponseEntity.ok(
                ApiResponse.success("Job descriptions retrieved successfully", jobDescriptions));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to retrieve job descriptions: " + e.getMessage()));
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteJobDescription(@PathVariable Long id) {
        try {
            jobDescriptionService.deleteJobDescription(id);
            return ResponseEntity.ok(
                ApiResponse.success("Job description deleted successfully", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to delete job description: " + e.getMessage()));
        }
    }
} 