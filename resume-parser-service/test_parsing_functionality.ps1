#!/usr/bin/env pwsh

Write-Host "Testing Job Description Parsing Functionality..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8081"

# Test 1: Upload a text-based job description with structured content
Write-Host "`n1. Testing job description upload with text content..." -ForegroundColor Yellow

$jobDescriptionText = @"
Senior Software Engineer

Company: TechCorp Solutions Ltd
Location: Mumbai, Maharashtra
Experience: 5-8 years

Job Description:
We are looking for a Senior Software Engineer to join our dynamic team.

Requirements:
- Bachelor's degree in Computer Science or related field
- 5+ years of experience in Java development
- Strong knowledge of Spring Boot, Microservices
- Experience with React.js and Angular
- Knowledge of SQL and NoSQL databases
- Excellent communication skills

Responsibilities:
- Design and develop scalable web applications
- Lead technical discussions and code reviews
- Mentor junior developers
- Collaborate with cross-functional teams
- Ensure code quality and best practices
"@

try {
    # Upload via form data
    $boundary = [System.Guid]::NewGuid().ToString()
    $headers = @{ "Content-Type" = "multipart/form-data; boundary=$boundary" }
    
    $bodyLines = @()
    $bodyLines += "--$boundary"
    $bodyLines += 'Content-Disposition: form-data; name="description"'
    $bodyLines += ""
    $bodyLines += $jobDescriptionText
    $bodyLines += "--$boundary"
    $bodyLines += 'Content-Disposition: form-data; name="panelMemberId"'
    $bodyLines += ""
    $bodyLines += "1"
    $bodyLines += "--$boundary--"
    
    $body = $bodyLines -join "`r`n"
    
    $uploadResponse = Invoke-RestMethod -Uri "$baseUrl/api/matching/uploadJD" -Method POST -Body $body -Headers $headers
    
    if ($uploadResponse.success) {
        Write-Host "✓ Upload successful!" -ForegroundColor Green
        Write-Host "Job ID: $($uploadResponse.jobId)" -ForegroundColor Cyan
        
        # Get the uploaded job to verify parsing
        Write-Host "`n2. Retrieving uploaded job to verify field extraction..." -ForegroundColor Yellow
        
        $jobsResponse = Invoke-RestMethod -Uri "$baseUrl/api/matching/jobs" -Method GET
        
        if ($jobsResponse.success -and $jobsResponse.jobs.Count -gt 0) {
            $latestJob = $jobsResponse.jobs[0]  # Get the most recent job
            
            Write-Host "✓ Job retrieved successfully!" -ForegroundColor Green
            Write-Host "`nExtracted Fields:" -ForegroundColor Magenta
            Write-Host "  Title: $($latestJob.title)" -ForegroundColor White
            Write-Host "  Company: $($latestJob.company)" -ForegroundColor White
            Write-Host "  Location: $($latestJob.location)" -ForegroundColor White
            Write-Host "  Experience: $($latestJob.experienceLevel)" -ForegroundColor White
            Write-Host "  Requirements Length: $($latestJob.requirements.Length) chars" -ForegroundColor White
            Write-Host "  Responsibilities Length: $($latestJob.responsibilities.Length) chars" -ForegroundColor White
            
            # Check if parsing worked
            $fieldsExtracted = @()
            if ($latestJob.title -and $latestJob.title -ne "Untitled Position") { $fieldsExtracted += "Title" }
            if ($latestJob.company -and $latestJob.company -ne "Unknown Company") { $fieldsExtracted += "Company" }
            if ($latestJob.location -and $latestJob.location -ne "Location TBD") { $fieldsExtracted += "Location" }
            if ($latestJob.experienceLevel -and $latestJob.experienceLevel -ne "Experience level not specified") { $fieldsExtracted += "Experience" }
            if ($latestJob.requirements) { $fieldsExtracted += "Requirements" }
            if ($latestJob.responsibilities) { $fieldsExtracted += "Responsibilities" }
            
            Write-Host "`nParsing Results:" -ForegroundColor Yellow
            Write-Host "Successfully extracted: $($fieldsExtracted -join ', ')" -ForegroundColor Green
            Write-Host "Total fields extracted: $($fieldsExtracted.Count)/6" -ForegroundColor Cyan
            
            if ($fieldsExtracted.Count -ge 4) {
                Write-Host "✓ Parsing is working well!" -ForegroundColor Green
            } else {
                Write-Host "⚠ Parsing could be improved" -ForegroundColor Yellow
            }
        } else {
            Write-Host "✗ Could not retrieve jobs" -ForegroundColor Red
        }
    } else {
        Write-Host "✗ Upload failed: $($uploadResponse.message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "✗ Error during upload: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`nParsing functionality test completed!" -ForegroundColor Green 