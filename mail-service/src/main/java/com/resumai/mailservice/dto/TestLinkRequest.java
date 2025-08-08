package com.resumai.mailservice.dto;

import com.resumai.mailservice.model.Candidate;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO class for test link request containing candidate information and test link
 */
public class TestLinkRequest {
    
    @NotNull(message = "Candidate is required")
    @Valid
    private Candidate candidate;
    
    @NotBlank(message = "Test link is required")
    private String testLink;
    
    public TestLinkRequest() {
    }
    
    public TestLinkRequest(Candidate candidate, String testLink) {
        this.candidate = candidate;
        this.testLink = testLink;
    }
    
    public Candidate getCandidate() {
        return candidate;
    }
    
    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }
    
    public String getTestLink() {
        return testLink;
    }
    
    public void setTestLink(String testLink) {
        this.testLink = testLink;
    }
    
    @Override
    public String toString() {
        return "TestLinkRequest{" +
                "candidate=" + candidate +
                ", testLink='" + testLink + '\'' +
                '}';
    }
} 