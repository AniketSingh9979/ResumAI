# Simple 2-Endpoint Resume Parser Test
# Tests the streamlined API with just 2 main endpoints

$baseUrl = "http://localhost:8081"

Write-Host "🚀 SIMPLIFIED 2-ENDPOINT API TEST" -ForegroundColor Yellow
Write-Host "==================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "This application now has just 2 main endpoints:" -ForegroundColor White
Write-Host "1. /api/uploadResume - Complete resume processing" -ForegroundColor Green  
Write-Host "2. /api/matching/uploadJD - Job description upload" -ForegroundColor Green
Write-Host ""

# Test Application Health
Write-Host "⏳ Testing Application Health..." -ForegroundColor Cyan
try {
    $health = Invoke-RestMethod -Uri "$baseUrl/actuator/health"
    if ($health.status -eq "UP") {
        Write-Host "✅ Application is running and healthy" -ForegroundColor Green
    }
} catch {
    Write-Host "❌ Cannot connect to application. Please start it first:" -ForegroundColor Red
    Write-Host "   mvn spring-boot:run" -ForegroundColor Yellow
    exit 1
}

Write-Host ""

# Test Endpoint 1: Upload Job Description
Write-Host "📄 ENDPOINT 1: Upload Job Description" -ForegroundColor Cyan
Write-Host "====================================" -ForegroundColor White

if (Test-Path "sample_job_description.txt") {
    Write-Host "Uploading job description..." -ForegroundColor Yellow
    try {
        $jobResult = cmd /c "curl -s -X POST $baseUrl/api/matching/uploadJD -F `"file=@sample_job_description.txt`" -F `"title=Data Scientist`" -F `"company=AI Corp`""
        if ($jobResult -like "*success*true*") {
            Write-Host "✅ Job description uploaded successfully" -ForegroundColor Green
            $jobData = $jobResult | ConvertFrom-Json
            Write-Host "   Job ID: $($jobData.jobId)" -ForegroundColor Gray
            Write-Host "   Title: $($jobData.job.title)" -ForegroundColor Gray
            Write-Host "   Company: $($jobData.job.company)" -ForegroundColor Gray
        } else {
            Write-Host "⚠️ Job upload result: $jobResult" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ Error uploading job: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "⚠️ sample_job_description.txt not found" -ForegroundColor Yellow
}

Write-Host ""

# Test Endpoint 2: Upload Resume (Complete Processing)
Write-Host "👤 ENDPOINT 2: Upload Resume (Complete Processing)" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor White

# Check for valid resume files
$resumeFile = $null
if (Test-Path "professional_resume.pdf") {
    $resumeFile = "professional_resume.pdf"
} elseif (Test-Path "professional_resume.doc") {
    $resumeFile = "professional_resume.doc"
} elseif (Test-Path "test_resume_valid.pdf") {
    $resumeFile = "test_resume_valid.pdf"
} elseif (Test-Path "test_resume_valid.doc") {
    $resumeFile = "test_resume_valid.doc"
}

if ($resumeFile) {
    Write-Host "Uploading resume: $resumeFile" -ForegroundColor Yellow
    Write-Host "This will: extract text, parse data, score against jobs, save to database" -ForegroundColor Gray
    
    try {
        $resumeResult = cmd /c "curl -s -X POST $baseUrl/api/uploadResume -F `"file=@$resumeFile`""
        
        if ($resumeResult -like "*success*true*") {
            Write-Host "✅ Resume processed successfully!" -ForegroundColor Green
            
            $resumeData = $resumeResult | ConvertFrom-Json
            Write-Host ""
            Write-Host "📊 PROCESSING RESULTS:" -ForegroundColor White
            Write-Host "  Resume ID: $($resumeData.resumeId)" -ForegroundColor Green
            Write-Host "  File: $($resumeData.originalFileName)" -ForegroundColor Gray
            Write-Host "  Size: $($resumeData.fileSize) bytes" -ForegroundColor Gray
            Write-Host "  Type: $($resumeData.detectedType)" -ForegroundColor Gray
            Write-Host "  Upload Time: $($resumeData.uploadTime)" -ForegroundColor Gray
            Write-Host ""
            Write-Host "📝 EXTRACTED DATA:" -ForegroundColor White
            Write-Host "  Email: $($resumeData.email)" -ForegroundColor Gray
            Write-Host "  Skills Count: $($resumeData.skillsCount)" -ForegroundColor Gray
            Write-Host "  Experience: $($resumeData.experience)" -ForegroundColor Gray
            Write-Host "  Text Length: $($resumeData.textLength) characters" -ForegroundColor Gray
            Write-Host ""
            Write-Host "🎯 SCORING RESULTS:" -ForegroundColor White
            Write-Host "  Best Match Score: $($resumeData.bestMatchScore)%" -ForegroundColor Green
            Write-Host "  Total Jobs Matched: $($resumeData.totalJobsMatched)" -ForegroundColor Gray
            
            if ($resumeData.bestMatchJob) {
                Write-Host "  Best Match Job: $($resumeData.bestMatchJob.jobTitle) at $($resumeData.bestMatchJob.company)" -ForegroundColor Gray
                Write-Host "  Match Category: $($resumeData.bestMatchJob.matchCategory)" -ForegroundColor Gray
            }
            
                         Write-Host ""
             Write-Host "💾 DATABASE STORAGE:" -ForegroundColor White
             Write-Host "  Saved to Database: $($resumeData.savedToDatabase)" -ForegroundColor Green
             Write-Host "  Database ID: $($resumeData.databaseId)" -ForegroundColor Gray
             
             Write-Host ""
             Write-Host "📁 FILE STORAGE:" -ForegroundColor White
             Write-Host "  File Saved to Disk: $($resumeData.fileSaved)" -ForegroundColor Green
             Write-Host "  File Path: $($resumeData.filePath)" -ForegroundColor Gray
             Write-Host "  Download URL: $($resumeData.downloadUrl)" -ForegroundColor Gray
            
        } else {
            Write-Host "❌ Resume processing failed" -ForegroundColor Red
            Write-Host "Response: $resumeResult" -ForegroundColor Gray
        }
    } catch {
        Write-Host "❌ Error processing resume: $($_.Exception.Message)" -ForegroundColor Red
    }
} else {
    Write-Host "❌ No valid resume files found!" -ForegroundColor Red
    Write-Host "Create a proper resume file first:" -ForegroundColor Yellow
    Write-Host "  powershell -ExecutionPolicy Bypass -File create_proper_resume.ps1" -ForegroundColor Gray
}

Write-Host ""

# Test Database Content
Write-Host "🗄️ DATABASE VERIFICATION" -ForegroundColor Cyan
Write-Host "========================" -ForegroundColor White

try {
    $resumes = Invoke-RestMethod -Uri "$baseUrl/api/resumes"
    if ($resumes.success) {
        Write-Host "✅ Parsed Resumes in Database: $($resumes.totalCount)" -ForegroundColor Green
    }
    
    $jobs = Invoke-RestMethod -Uri "$baseUrl/api/matching/jobs"
    if ($jobs.success) {
        Write-Host "✅ Job Descriptions in Database: $($jobs.totalJobs)" -ForegroundColor Green
    }
    
    $stats = Invoke-RestMethod -Uri "$baseUrl/api/resumes/statistics"
    if ($stats.success) {
        Write-Host "✅ Average Resume Score: $($stats.statistics.averageScore)%" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️ Could not verify database content" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🎉 SIMPLIFIED API TEST COMPLETE!" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green
Write-Host ""
Write-Host "✅ YOUR CLEAN 2-ENDPOINT API:" -ForegroundColor Yellow
Write-Host "  1. POST /api/uploadResume - Complete resume processing" -ForegroundColor Green
Write-Host "     (validate + extract + parse + score + save to database)" -ForegroundColor Gray
Write-Host "  2. POST /api/matching/uploadJD - Job description upload" -ForegroundColor Green
Write-Host "     (validate + extract + parse + save to database)" -ForegroundColor Gray
Write-Host ""
Write-Host "🌐 Useful URLs:" -ForegroundColor White
Write-Host "  - Application Health: http://localhost:8081/actuator/health" -ForegroundColor Gray
Write-Host "  - All Resumes: http://localhost:8081/api/resumes" -ForegroundColor Gray
Write-Host "  - All Jobs: http://localhost:8081/api/matching/jobs" -ForegroundColor Gray
Write-Host "  - Statistics: http://localhost:8081/api/resumes/statistics" -ForegroundColor Gray
Write-Host ""
Write-Host "🧹 CLEANUP COMPLETED:" -ForegroundColor Cyan
Write-Host "  - Removed redundant matching endpoints" -ForegroundColor Green
Write-Host "  - Removed unused controllers and services" -ForegroundColor Green  
Write-Host "  - Removed duplicate health endpoints" -ForegroundColor Green
Write-Host "  - Updated API documentation" -ForegroundColor Green 