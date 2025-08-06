#!/usr/bin/env pwsh

Write-Host "Testing Requirements and Responsibilities Parsing..." -ForegroundColor Cyan

$baseUrl = "http://localhost:8081"

# Test job description with clear requirements and responsibilities sections
$testJobDescription = @"
Senior Java Developer

Company: TechCorp Solutions
Location: Mumbai, India
Experience: 5+ years

Job Description:
We are seeking a talented Senior Java Developer to join our growing team.

Requirements:
- Bachelor's degree in Computer Science or related field
- 5+ years of experience in Java development
- Strong knowledge of Spring Boot and Hibernate
- Experience with microservices architecture
- Proficiency in SQL and database design
- Knowledge of REST API development
- Experience with Git version control

Responsibilities:
- Design and develop high-quality Java applications
- Implement microservices using Spring Boot
- Write clean, maintainable, and efficient code
- Participate in code reviews and technical discussions
- Collaborate with cross-functional teams
- Troubleshoot and debug applications
- Mentor junior developers

About the Company:
TechCorp Solutions is a leading technology company...
"@

Write-Host "Uploading job description with clear requirements and responsibilities..." -ForegroundColor Yellow

try {
    # Create multipart form data manually
    $boundary = [System.Guid]::NewGuid().ToString()
    $LF = "`r`n"
    
    $bodyLines = @()
    $bodyLines += "--$boundary"
    $bodyLines += 'Content-Disposition: form-data; name="description"'
    $bodyLines += ""
    $bodyLines += $testJobDescription
    $bodyLines += "--$boundary"
    $bodyLines += 'Content-Disposition: form-data; name="panelMemberId"'
    $bodyLines += ""
    $bodyLines += "1"
    $bodyLines += "--$boundary--"
    
    $body = $bodyLines -join $LF
    $headers = @{
        'Content-Type' = "multipart/form-data; boundary=$boundary"
    }
    
    $uploadResponse = Invoke-RestMethod -Uri "$baseUrl/api/matching/uploadJD" -Method POST -Body $body -Headers $headers
    
    if ($uploadResponse.success) {
        Write-Host "✓ Upload successful! Job ID: $($uploadResponse.jobId)" -ForegroundColor Green
        
        # Wait a moment for processing
        Start-Sleep -Seconds 2
        
        # Retrieve the uploaded job
        Write-Host "`nRetrieving uploaded job to check parsing..." -ForegroundColor Yellow
        
        $jobsResponse = Invoke-RestMethod -Uri "$baseUrl/api/matching/jobs" -Method GET
        
        if ($jobsResponse.success -and $jobsResponse.jobs.Count -gt 0) {
            $latestJob = $jobsResponse.jobs[0]
            
            Write-Host "`nParsing Results:" -ForegroundColor Magenta
            Write-Host "Title: '$($latestJob.title)'" -ForegroundColor White
            Write-Host "Company: '$($latestJob.company)'" -ForegroundColor White
            Write-Host "Location: '$($latestJob.location)'" -ForegroundColor White
            Write-Host "Experience: '$($latestJob.experienceLevel)'" -ForegroundColor White
            
            # Focus on requirements and responsibilities
            Write-Host "`nRequirements:" -ForegroundColor Yellow
            if ($latestJob.requirements -and $latestJob.requirements.Trim() -ne "") {
                Write-Host "✓ FOUND ($($latestJob.requirements.Length) chars)" -ForegroundColor Green
                Write-Host $latestJob.requirements.Substring(0, [Math]::Min(200, $latestJob.requirements.Length)) -ForegroundColor Gray
                if ($latestJob.requirements.Length -gt 200) {
                    Write-Host "..." -ForegroundColor Gray
                }
            } else {
                Write-Host "✗ NULL or EMPTY" -ForegroundColor Red
            }
            
            Write-Host "`nResponsibilities:" -ForegroundColor Yellow
            if ($latestJob.responsibilities -and $latestJob.responsibilities.Trim() -ne "") {
                Write-Host "✓ FOUND ($($latestJob.responsibilities.Length) chars)" -ForegroundColor Green
                Write-Host $latestJob.responsibilities.Substring(0, [Math]::Min(200, $latestJob.responsibilities.Length)) -ForegroundColor Gray
                if ($latestJob.responsibilities.Length -gt 200) {
                    Write-Host "..." -ForegroundColor Gray
                }
            } else {
                Write-Host "✗ NULL or EMPTY" -ForegroundColor Red
            }
            
            # Summary
            $successCount = 0
            if ($latestJob.requirements -and $latestJob.requirements.Trim() -ne "") { $successCount++ }
            if ($latestJob.responsibilities -and $latestJob.responsibilities.Trim() -ne "") { $successCount++ }
            
            Write-Host "`nSummary:" -ForegroundColor Cyan
            Write-Host "Successfully parsed: $successCount/2 sections" -ForegroundColor White
            
            if ($successCount -eq 2) {
                Write-Host "✓ Requirements and Responsibilities parsing is working!" -ForegroundColor Green
            } elseif ($successCount -eq 1) {
                Write-Host "⚠ Partial success - one section parsed" -ForegroundColor Yellow
            } else {
                Write-Host "✗ Parsing failed - check the parsing patterns" -ForegroundColor Red
            }
            
        } else {
            Write-Host "✗ Could not retrieve uploaded job" -ForegroundColor Red
        }
        
    } else {
        Write-Host "✗ Upload failed: $($uploadResponse.message)" -ForegroundColor Red
    }
    
} catch {
    Write-Host "✗ Error: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    }
}

Write-Host "`nTest completed!" -ForegroundColor Green 