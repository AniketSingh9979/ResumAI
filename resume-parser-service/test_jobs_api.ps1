#!/usr/bin/env pwsh

# Test script for Job Descriptions API
Write-Host "Testing Job Descriptions API..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8081"

# Test 1: Check if service is running
Write-Host "`n1. Testing server connectivity..." -ForegroundColor Yellow
try {
    $healthCheck = Invoke-RestMethod -Uri "$baseUrl/actuator/health" -Method GET -TimeoutSec 5
    Write-Host "Server is running!" -ForegroundColor Green
} catch {
    try {
        # Fallback test with any endpoint
        $response = Invoke-WebRequest -Uri $baseUrl -Method GET -TimeoutSec 5
        Write-Host "Server is accessible!" -ForegroundColor Green
    } catch {
        Write-Host "Server is not accessible. Please start the resume-parser-service." -ForegroundColor Red
        Write-Host "Command: cd ResumAI/resume-parser-service && mvn spring-boot:run" -ForegroundColor Yellow
        exit 1
    }
}

# Test 2: Get all job descriptions
Write-Host "`n2. Testing GET /api/matching/jobs..." -ForegroundColor Yellow
try {
    $jobsResponse = Invoke-RestMethod -Uri "$baseUrl/api/matching/jobs" -Method GET
    
    if ($jobsResponse.success) {
        Write-Host "Jobs API is working!" -ForegroundColor Green
        Write-Host "Total Jobs: $($jobsResponse.totalJobs)" -ForegroundColor Cyan
        Write-Host "Jobs Found: $($jobsResponse.jobs.Count)" -ForegroundColor Cyan
        
        if ($jobsResponse.jobs.Count -gt 0) {
            Write-Host "`nFirst Job Details:" -ForegroundColor Magenta
            $firstJob = $jobsResponse.jobs[0]
            Write-Host "   ID: $($firstJob.id)" -ForegroundColor Gray
            Write-Host "   Title: $($firstJob.title)" -ForegroundColor Gray
            Write-Host "   Company: $($firstJob.company)" -ForegroundColor Gray
            Write-Host "   Location: $($firstJob.location)" -ForegroundColor Gray
            Write-Host "   Experience: $($firstJob.experienceLevel)" -ForegroundColor Gray
            Write-Host "   Panel Member: $($firstJob.panelMemberName)" -ForegroundColor Gray
            Write-Host "   Created: $($firstJob.createdDate)" -ForegroundColor Gray
            Write-Host "   Description: $($firstJob.description.Substring(0, [Math]::Min(100, $firstJob.description.Length)))" -ForegroundColor Gray
            
            # Show the full JSON for debugging
            Write-Host "`nFull Job JSON:" -ForegroundColor Yellow
            $firstJob | ConvertTo-Json -Depth 3
        } else {
            Write-Host "No jobs found in database." -ForegroundColor Yellow
            Write-Host "Try uploading a job description first." -ForegroundColor Cyan
        }
    } else {
        Write-Host "API returned error: $($jobsResponse.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "Error calling jobs API: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Full error details:" -ForegroundColor Yellow
    Write-Host $_.Exception -ForegroundColor Gray
}

# Test 3: Get panel members (used for job associations)
Write-Host "`n3. Testing GET /api/matching/panel-members..." -ForegroundColor Yellow
try {
    $panelResponse = Invoke-RestMethod -Uri "$baseUrl/api/matching/panel-members" -Method GET
    
    if ($panelResponse.success) {
        Write-Host "Panel Members API is working!" -ForegroundColor Green
        Write-Host "Total Panel Members: $($panelResponse.totalPanelMembers)" -ForegroundColor Cyan
        
        if ($panelResponse.panelMembers.Count -gt 0) {
            Write-Host "`nFirst Panel Member:" -ForegroundColor Magenta
            $firstPanel = $panelResponse.panelMembers[0]
            Write-Host "   ID: $($firstPanel.id)" -ForegroundColor Gray
            Write-Host "   Name: $($firstPanel.name)" -ForegroundColor Gray
            Write-Host "   Email: $($firstPanel.email)" -ForegroundColor Gray
            Write-Host "   Designation: $($firstPanel.designation)" -ForegroundColor Gray
        }
    } else {
        Write-Host "Panel API returned error: $($panelResponse.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "Error calling panel members API: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nAPI Testing Complete!" -ForegroundColor Green
Write-Host "`nSummary:" -ForegroundColor Cyan
Write-Host "   - Use GET /api/matching/jobs to fetch all job descriptions" -ForegroundColor White
Write-Host "   - Use GET /api/matching/panel-members to get panel members" -ForegroundColor White
Write-Host "   - Use POST /api/matching/uploadJD to upload new job descriptions" -ForegroundColor White
Write-Host "`nDashboard URL: http://localhost:4200" -ForegroundColor Yellow
Write-Host "Backend URL: http://localhost:8081" -ForegroundColor Yellow 