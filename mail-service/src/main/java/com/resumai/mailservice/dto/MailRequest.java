package com.resumai.mailservice.dto;

import com.resumai.mailservice.model.Candidate;
import com.resumai.mailservice.model.Recruiter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * DTO class for mail request containing candidate and recruiter information
 */
public class MailRequest {
    
    @NotNull(message = "Candidate is required")
    @Valid
    private Candidate candidate;
    
    @NotNull(message = "Recruiter is required")
    @Valid
    private Recruiter recruiter;
    
    public MailRequest() {
    }
    
    public MailRequest(Candidate candidate, Recruiter recruiter) {
        this.candidate = candidate;
        this.recruiter = recruiter;
    }
    
    public Candidate getCandidate() {
        return candidate;
    }
    
    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }
    
    public Recruiter getRecruiter() {
        return recruiter;
    }
    
    public void setRecruiter(Recruiter recruiter) {
        this.recruiter = recruiter;
    }
    
    @Override
    public String toString() {
        return "MailRequest{" +
                "candidate=" + candidate +
                ", recruiter=" + recruiter +
                '}';
    }
} 