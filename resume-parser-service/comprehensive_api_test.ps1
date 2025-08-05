# ResumAI API - Comprehensive Testing Script
# Run this script to test all major API functionalities automatically

param(
    [string]$BaseUrl = "http://localhost:8081",
    [string]$TestDataDir = "src/main/resources/docs"
)

$ErrorActionPreference = "Continue"
Write-Host "🎯 ResumAI API Comprehensive Testing Script" -ForegroundColor Cyan
Write-Host "Testing API at: $BaseUrl" -ForegroundColor Yellow
Write-Host "Looking for test files in: $TestDataDir" -ForegroundColor Yellow
Write-Host ""

# Function to make HTTP requests and handle responses
function Test-Endpoint {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [string]$Body = $null,
        [string]$FormFile = $null,
        [hashtable]$FormData = @{}
    )
    
    Write-Host "🔍 Testing: $Name" -ForegroundColor Green
    Write-Host "   $Method $Url" -ForegroundColor Gray
    
    try {
        $params = @{
            Uri = $Url
            Method = $Method
            Headers = $Headers
        }
        
        if ($Body) {
            $params.Body = $Body
        }
        
        if ($FormFile -and (Test-Path $FormFile)) {
            $boundary = [System.Guid]::NewGuid().ToString()
            $fileName = Split-Path $FormFile -Leaf
            $fileContent = [System.IO.File]::ReadAllBytes($FormFile)
            
            $bodyLines = @()
            $bodyLines += "--$boundary"
            $bodyLines += "Content-Disposition: form-data; name=`"file`"; filename=`"$fileName`""
            $bodyLines += "Content-Type: application/octet-stream"
            $bodyLines += ""
            
            # Add form data fields
            foreach ($key in $FormData.Keys) {
                $bodyLines += "--$boundary"
                $bodyLines += "Content-Disposition: form-data; name=`"$key`""
                $bodyLines += ""
                $bodyLines += $FormData[$key]
            }
            
            $bodyLines += "--$boundary--"
            
            $bodyString = $bodyLines -join "`r`n"
            $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($bodyString)
            $fileBytes = $bodyBytes[0..($bodyBytes.Length - 1)] + $fileContent + [System.Text.Encoding]::UTF8.GetBytes("`r`n")
            
            $params.Body = $fileBytes
            $params.Headers["Content-Type"] = "multipart/form-data; boundary=$boundary"
        }
        
        $response = Invoke-RestMethod @params
        Write-Host "   ✅ SUCCESS: " -ForegroundColor Green -NoNewline
        
        if ($response.success -eq $true) {
            Write-Host "API returned success=true" -ForegroundColor Green
        } else {
            Write-Host "API returned success=false: $($response.message)" -ForegroundColor Yellow
        }
        
        return $response
    }
    catch {
        Write-Host "   ❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
    Write-Host ""
}

# Function to test file download
function Test-Download {
    param(
        [string]$Name,
        [string]$Url,
        [string]$OutputFile
    )
    
    Write-Host "📥 Testing Download: $Name" -ForegroundColor Green
    Write-Host "   GET $Url" -ForegroundColor Gray
    
    try {
        Invoke-WebRequest -Uri $Url -OutFile $OutputFile
        if (Test-Path $OutputFile) {
            $size = (Get-Item $OutputFile).Length
            Write-Host "   ✅ SUCCESS: Downloaded $size bytes to $OutputFile" -ForegroundColor Green
            Remove-Item $OutputFile -Force # Clean up
        } else {
            Write-Host "   ❌ FAILED: File not created" -ForegroundColor Red
        }
    }
    catch {
        Write-Host "   ❌ FAILED: $($_.Exception.Message)" -ForegroundColor Red
    }
    Write-Host ""
}

# Start testing
Write-Host "📋 COMPREHENSIVE API TESTING STARTED" -ForegroundColor Cyan
Write-Host "=" * 50

# 1. Test Application Health
Write-Host "`n🏥 1. HEALTH CHECK TESTS" -ForegroundColor Magenta
Test-Endpoint -Name "Application Health" -Method "GET" -Url "$BaseUrl/actuator/health"

# 2. Test Job Description Upload
Write-Host "`n💼 2. JOB DESCRIPTION TESTS" -ForegroundColor Magenta

# Test with text parameters
$jobResponse = Test-Endpoint -Name "Upload Job Description (Text)" -Method "POST" -Url "$BaseUrl/api/matching/uploadJD" `
    -Headers @{"Content-Type" = "multipart/form-data"} `
    -FormData @{
        "title" = "Senior Java Developer"
        "company" = "TechCorp Testing"
        "description" = "We need an experienced Java developer for our team"
        "requirements" = "Java, Spring Boot, SQL, 5+ years experience"
        "responsibilities" = "Develop microservices, code reviews, mentoring"
        "location" = "Remote"
        "experienceLevel" = "Senior"
    }

if ($jobResponse) {
    $jobId = $jobResponse.jobId
    Write-Host "   📝 Job ID created: $jobId" -ForegroundColor Blue
}

# Get all jobs
Test-Endpoint -Name "Get All Job Descriptions" -Method "GET" -Url "$BaseUrl/api/matching/jobs"

# Get specific job if we have an ID
if ($jobId) {
    Test-Endpoint -Name "Get Job by ID" -Method "GET" -Url "$BaseUrl/api/matching/jobs/$jobId"
}

# 3. Test Resume Upload
Write-Host "`n📄 3. RESUME UPLOAD TESTS" -ForegroundColor Magenta

# Look for test resume files
$testResume = $null
$possibleFiles = @(
    "$TestDataDir/test_resume.pdf",
    "$TestDataDir/Sarah Johnson - Resume.pdf",
    "test_resume.pdf",
    "resume.pdf"
)

foreach ($file in $possibleFiles) {
    if (Test-Path $file) {
        $testResume = $file
        break
    }
}

if ($testResume) {
    Write-Host "   📁 Using test resume: $testResume" -ForegroundColor Blue
    $resumeResponse = Test-Endpoint -Name "Upload Resume (PDF)" -Method "POST" -Url "$BaseUrl/api/uploadResume" `
        -FormFile $testResume
    
    if ($resumeResponse) {
        $resumeId = $resumeResponse.resumeId
        Write-Host "   📝 Resume ID created: $resumeId" -ForegroundColor Blue
        Write-Host "   📧 Extracted email: $($resumeResponse.email)" -ForegroundColor Blue
        Write-Host "   🎯 Best match score: $($resumeResponse.bestMatchScore)" -ForegroundColor Blue
        Write-Host "   🔧 Skills found: $($resumeResponse.skillsCount)" -ForegroundColor Blue
    }
} else {
    Write-Host "   ⚠️  No test resume file found. Skipping resume upload tests." -ForegroundColor Yellow
}

# 4. Test Resume Management
Write-Host "`n📊 4. RESUME MANAGEMENT TESTS" -ForegroundColor Magenta

# Get all resumes
$allResumes = Test-Endpoint -Name "Get All Resumes" -Method "GET" -Url "$BaseUrl/api/resumes"

# Get resume statistics
Test-Endpoint -Name "Get Resume Statistics" -Method "GET" -Url "$BaseUrl/api/resumes/statistics"

# Get resume list for downloads
Test-Endpoint -Name "Get Resume List (Downloads)" -Method "GET" -Url "$BaseUrl/api/resumesList"

# Test specific resume operations if we have a resume ID
if ($resumeId) {
    # Get specific resume
    Test-Endpoint -Name "Get Resume by ID" -Method "GET" -Url "$BaseUrl/api/resumes/$resumeId"
    
    # Test download
    Test-Download -Name "Download Resume by ID" -Url "$BaseUrl/api/resumes/$resumeId/download" -OutputFile "test_download.pdf"
    
    # Update score
    Test-Endpoint -Name "Update Resume Score" -Method "PUT" -Url "$BaseUrl/api/resumes/$resumeId/score" `
        -Headers @{"Content-Type" = "application/json"} `
        -Body '{"score": 95.5}'
}

# 5. Test Search Operations
Write-Host "`n🔍 5. SEARCH OPERATION TESTS" -ForegroundColor Magenta

# Test skill searches
$skillsToTest = @("Java", "Python", "JavaScript", "React", "SQL")
foreach ($skill in $skillsToTest) {
    Test-Endpoint -Name "Search by Skill: $skill" -Method "GET" -Url "$BaseUrl/api/resumes/search/skill/$skill"
}

# Test score range search
Test-Endpoint -Name "Search by Score Range (80-100)" -Method "GET" -Url "$BaseUrl/api/resumes/search/score?minScore=80.0&maxScore=100.0"

# Test text search
$keywords = @("developer", "engineer", "experience")
foreach ($keyword in $keywords) {
    Test-Endpoint -Name "Search by Text: $keyword" -Method "GET" -Url "$BaseUrl/api/resumes/search/text/$keyword"
}

# Test email search if we have an email
if ($resumeResponse -and $resumeResponse.email) {
    Test-Endpoint -Name "Search by Email" -Method "GET" -Url "$BaseUrl/api/resumes/search/email/$($resumeResponse.email)"
}

# 6. Test Error Handling
Write-Host "`n❌ 6. ERROR HANDLING TESTS" -ForegroundColor Magenta

# Test invalid endpoints
Test-Endpoint -Name "Invalid Resume ID" -Method "GET" -Url "$BaseUrl/api/resumes/999999"
Test-Endpoint -Name "Invalid Job ID" -Method "GET" -Url "$BaseUrl/api/matching/jobs/999999"

# Test invalid score update
if ($resumeId) {
    Test-Endpoint -Name "Invalid Score Update (Over 100)" -Method "PUT" -Url "$BaseUrl/api/resumes/$resumeId/score" `
        -Headers @{"Content-Type" = "application/json"} `
        -Body '{"score": 150.0}'
}

# Summary
Write-Host "`n📋 TESTING COMPLETED" -ForegroundColor Cyan
Write-Host "=" * 50

Write-Host "`n📊 SUMMARY:" -ForegroundColor Yellow
Write-Host "• Health check tested" -ForegroundColor White
Write-Host "• Job description upload and retrieval tested" -ForegroundColor White
if ($testResume) {
    Write-Host "• Resume upload and processing tested" -ForegroundColor White
    Write-Host "• File download tested" -ForegroundColor White
} else {
    Write-Host "• Resume upload skipped (no test file found)" -ForegroundColor Yellow
}
Write-Host "• Search operations tested" -ForegroundColor White
Write-Host "• Error handling tested" -ForegroundColor White

Write-Host "`n🎯 NEXT STEPS:" -ForegroundColor Cyan
Write-Host "1. Review any failed tests above" -ForegroundColor White
Write-Host "2. Check the comprehensive testing guide for detailed scenarios" -ForegroundColor White
Write-Host "3. Test with actual resume files in production environment" -ForegroundColor White
Write-Host "4. Monitor application logs for any errors" -ForegroundColor White

Write-Host "`n✅ Testing script completed!" -ForegroundColor Green 