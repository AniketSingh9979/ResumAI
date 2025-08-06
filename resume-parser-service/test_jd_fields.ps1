# Test script for proper job description field storage
Write-Host "Testing Job Description Field Structure..." -ForegroundColor Green

# Test with comprehensive job description data
Write-Host "`n1. Testing uploadJD with all fields..." -ForegroundColor Yellow
try {
    $body = @{
        title = "Senior Java Developer"
        company = "TechCorp Solutions"
        description = "We are seeking an experienced Senior Java Developer to join our dynamic team"
        requirements = "5+ years Java, Spring Boot, Microservices, REST APIs, SQL, Git"
        responsibilities = "Design microservices, Lead technical discussions, Code reviews, Mentor junior developers"
        location = "Mumbai, India"
        experienceLevel = "5-8 years"
        panelistName = "Rajesh Kumar"
    }
    
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/matching/uploadJD" -Method POST -Body $body -TimeoutSec 30
    Write-Host "✅ Job Description created successfully!" -ForegroundColor Green
    Write-Host "Job ID: $($response.jobId)" -ForegroundColor Cyan
    
    # Verify the response contains proper field data
    if ($response.job.title) {
        Write-Host "✅ Title field saved: $($response.job.title)" -ForegroundColor Green
    } else {
        Write-Host "❌ Title field not saved properly" -ForegroundColor Red
    }
    
    if ($response.job.company) {
        Write-Host "✅ Company field saved: $($response.job.company)" -ForegroundColor Green
    } else {
        Write-Host "❌ Company field not saved properly" -ForegroundColor Red
    }
    
    if ($response.job.location) {
        Write-Host "✅ Location field saved: $($response.job.location)" -ForegroundColor Green
    } else {
        Write-Host "❌ Location field not saved properly" -ForegroundColor Red
    }
    
    $jobId = $response.jobId
    
} catch {
    Write-Host "❌ Job creation failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errorResponse = $reader.ReadToEnd()
        Write-Host "Error details: $errorResponse" -ForegroundColor Red
    }
    exit 1
}

# Test 2: Retrieve the job and verify fields
if ($jobId) {
    Write-Host "`n2. Testing job retrieval..." -ForegroundColor Yellow
    try {
        $retrievedJob = Invoke-RestMethod -Uri "http://localhost:8081/api/matching/jobs/$jobId" -Method GET -TimeoutSec 10
        
        if ($retrievedJob.success) {
            Write-Host "✅ Job retrieved successfully!" -ForegroundColor Green
            $job = $retrievedJob.job
            
            Write-Host "Job Details:" -ForegroundColor Cyan
            Write-Host "  Title: $($job.title)" -ForegroundColor White
            Write-Host "  Company: $($job.company)" -ForegroundColor White
            Write-Host "  Location: $($job.location)" -ForegroundColor White
            Write-Host "  Experience: $($job.experienceLevel)" -ForegroundColor White
            Write-Host "  Requirements: $($job.requirements -replace '\n', ' | ')" -ForegroundColor White
            Write-Host "  Responsibilities: $($job.responsibilities -replace '\n', ' | ')" -ForegroundColor White
        } else {
            Write-Host "❌ Failed to retrieve job: $($retrievedJob.message)" -ForegroundColor Red
        }
        
    } catch {
        Write-Host "❌ Job retrieval failed: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Test 3: Test filtering by individual fields
Write-Host "`n3. Testing field-based filtering..." -ForegroundColor Yellow
try {
    # Test filtering by company
    $filterResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/matching/jobs/paginated?company=TechCorp" -Method GET -TimeoutSec 10
    
    if ($filterResponse.success -and $filterResponse.totalElements -gt 0) {
        Write-Host "✅ Company filtering works! Found $($filterResponse.totalElements) jobs" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Company filtering returned no results" -ForegroundColor Yellow
    }
    
    # Test filtering by location
    $locationFilter = Invoke-RestMethod -Uri "http://localhost:8081/api/matching/jobs/paginated?location=Mumbai" -Method GET -TimeoutSec 10
    
    if ($locationFilter.success -and $locationFilter.totalElements -gt 0) {
        Write-Host "✅ Location filtering works! Found $($locationFilter.totalElements) jobs" -ForegroundColor Green
    } else {
        Write-Host "⚠️  Location filtering returned no results" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "❌ Filtering test failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n✅ Field structure testing completed!" -ForegroundColor Green
Write-Host "Check your database to verify that individual columns are now populated correctly" -ForegroundColor Cyan 