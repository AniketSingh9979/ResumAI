# Test script for uploadJD endpoint
Write-Host "Testing uploadJD endpoint..." -ForegroundColor Green

# Test 1: Basic server connectivity
Write-Host "`n1. Testing server connectivity..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/actuator/health" -Method GET -TimeoutSec 10
    Write-Host "✅ Server is responding: $($response.status)" -ForegroundColor Green
} catch {
    Write-Host "❌ Server connectivity failed: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Please ensure the application is running on port 8081" -ForegroundColor Red
    exit 1
}

# Test 2: Test uploadJD with text-only (no file)
Write-Host "`n2. Testing uploadJD with text data..." -ForegroundColor Yellow
try {
    $body = @{
        title = "Senior Java Developer"
        company = "TechCorp"
        description = "We are looking for a Senior Java Developer with Spring Boot experience"
        requirements = "Java 8+, Spring Boot, Microservices, REST APIs"
        responsibilities = "Design and develop microservices, Code reviews, Mentoring"
        location = "Remote"
        experienceLevel = "5+ years"
        panelistName = "John Smith"
    }
    
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/matching/uploadJD" -Method POST -Body $body -TimeoutSec 30
    Write-Host "✅ Text upload successful!" -ForegroundColor Green
    Write-Host "Job ID: $($response.jobId)" -ForegroundColor Cyan
    Write-Host "Response: $($response.message)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Text upload failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = [System.IO.StreamReader]::new($_.Exception.Response.GetResponseStream())
        $errorResponse = $reader.ReadToEnd()
        Write-Host "Error details: $errorResponse" -ForegroundColor Red
    }
}

# Test 3: Test uploadJD with file (if test file exists)
Write-Host "`n3. Testing uploadJD with file upload..." -ForegroundColor Yellow
$testFile = "test_job_description.txt"
if (Test-Path $testFile) {
    try {
        $form = @{
            file = Get-Item $testFile
            title = "Backend Developer"
            company = "StartupXYZ"
            panelistName = "Jane Doe"
        }
        
        $response = Invoke-RestMethod -Uri "http://localhost:8081/api/matching/uploadJD" -Method POST -Form $form -TimeoutSec 30
        Write-Host "✅ File upload successful!" -ForegroundColor Green
        Write-Host "Job ID: $($response.jobId)" -ForegroundColor Cyan
    } catch {
        Write-Host "❌ File upload failed: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "⚠️  Test file '$testFile' not found, skipping file upload test" -ForegroundColor Yellow
    Write-Host "Create a text file named '$testFile' to test file uploads" -ForegroundColor Yellow
}

# Test 4: Get panel members
Write-Host "`n4. Testing panel members endpoint..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:8081/api/matching/panel-members" -Method GET -TimeoutSec 10
    Write-Host "✅ Panel members retrieved: $($response.totalPanelMembers) members" -ForegroundColor Green
} catch {
    Write-Host "❌ Panel members fetch failed: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n✅ Test completed!" -ForegroundColor Green
Write-Host "If any tests failed, check the console logs in your Spring Boot application" -ForegroundColor Cyan 