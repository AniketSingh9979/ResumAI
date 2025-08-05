# ResumAI API - Comprehensive Testing Guide

## 🎯 Overview
This document provides a complete testing checklist for all ResumAI API functionalities. Use this guide to systematically test every endpoint and ensure full functionality.

## 🔧 Prerequisites
- Server running on `http://localhost:8081`
- Sample resume files (PDF, DOC)
- Sample job description files (PDF, DOC, DOCX, RTF, TXT)
- API testing tool (Postman, cURL, etc.)

---

## 📋 Complete API Testing Checklist

### ✅ **1. CORE FILE UPLOAD OPERATIONS**

#### 1.1 Resume Upload & Processing
**Endpoint:** `POST /api/uploadResume`

**Test Cases:**
```bash
# Test 1: Valid PDF resume
curl -X POST http://localhost:8081/api/uploadResume \
  -H "Content-Type: multipart/form-data" \
  -F "file=@test_resume.pdf"

# Test 2: Valid DOC resume  
curl -X POST http://localhost:8081/api/uploadResume \
  -H "Content-Type: multipart/form-data" \
  -F "file=@test_resume.doc"

# Test 3: Invalid file type (should fail)
curl -X POST http://localhost:8081/api/uploadResume \
  -H "Content-Type: multipart/form-data" \
  -F "file=@test_resume.txt"

# Test 4: Large file (should fail if >10MB)
curl -X POST http://localhost:8081/api/uploadResume \
  -H "Content-Type: multipart/form-data" \
  -F "file=@large_resume.pdf"

# Test 5: No file provided (should fail)
curl -X POST http://localhost:8081/api/uploadResume \
  -H "Content-Type: multipart/form-data"
```

**Expected Response Fields:**
- ✅ `success: true`
- ✅ `resumeId` (numeric)
- ✅ `originalFileName`
- ✅ `extractedText`
- ✅ `email`
- ✅ `skills` (array)
- ✅ `bestMatchScore`
- ✅ `bestMatchJob` (if jobs exist)
- ✅ `filePath`

#### 1.2 Job Description Upload
**Endpoint:** `POST /api/matching/uploadJD`

**Test Cases:**
```bash
# Test 1: Upload JD file
curl -X POST http://localhost:8081/api/matching/uploadJD \
  -H "Content-Type: multipart/form-data" \
  -F "file=@job_description.pdf" \
  -F "title=Senior Developer" \
  -F "company=TechCorp"

# Test 2: Upload with all fields
curl -X POST http://localhost:8081/api/matching/uploadJD \
  -H "Content-Type: multipart/form-data" \
  -F "file=@job_description.pdf" \
  -F "title=Senior Java Developer" \
  -F "company=TechCorp Inc" \
  -F "description=Looking for experienced Java developer" \
  -F "requirements=5+ years Java, Spring Boot" \
  -F "responsibilities=Develop microservices" \
  -F "location=New York, NY" \
  -F "experienceLevel=Senior"

# Test 3: Text-only (no file)
curl -X POST http://localhost:8081/api/matching/uploadJD \
  -H "Content-Type: multipart/form-data" \
  -F "title=Frontend Developer" \
  -F "company=WebCorp" \
  -F "description=React developer needed" \
  -F "requirements=React, JavaScript, CSS"

# Test 4: Various file types
curl -X POST http://localhost:8081/api/matching/uploadJD \
  -H "Content-Type: multipart/form-data" \
  -F "file=@job_description.docx"

curl -X POST http://localhost:8081/api/matching/uploadJD \
  -H "Content-Type: multipart/form-data" \
  -F "file=@job_description.txt"
```

---

### ✅ **2. RESUME MANAGEMENT & QUERIES**

#### 2.1 Get All Resumes
**Endpoint:** `GET /api/resumes`

```bash
# Test: Get all resumes
curl -X GET http://localhost:8081/api/resumes
```

**Expected Response:**
- ✅ List of all parsed resumes
- ✅ Total count
- ✅ Complete resume data for each entry

#### 2.2 Get Resume by ID
**Endpoint:** `GET /api/resumes/{id}`

```bash
# Test 1: Valid ID
curl -X GET http://localhost:8081/api/resumes/1

# Test 2: Invalid ID (should return 404)
curl -X GET http://localhost:8081/api/resumes/999999
```

#### 2.3 Search Resumes by Email
**Endpoint:** `GET /api/resumes/search/email/{email}`

```bash
# Test 1: Existing email
curl -X GET http://localhost:8081/api/resumes/search/email/john.doe@email.com

# Test 2: Non-existing email
curl -X GET http://localhost:8081/api/resumes/search/email/notfound@email.com

# Test 3: Partial email (test URL encoding)
curl -X GET "http://localhost:8081/api/resumes/search/email/test%40email.com"
```

#### 2.4 Search Resumes by Skill
**Endpoint:** `GET /api/resumes/search/skill/{skill}`

```bash
# Test various skills
curl -X GET http://localhost:8081/api/resumes/search/skill/Java
curl -X GET http://localhost:8081/api/resumes/search/skill/Python
curl -X GET http://localhost:8081/api/resumes/search/skill/JavaScript
curl -X GET http://localhost:8081/api/resumes/search/skill/React
curl -X GET http://localhost:8081/api/resumes/search/skill/NonExistentSkill
```

#### 2.5 Search Resumes by Score Range
**Endpoint:** `GET /api/resumes/search/score`

```bash
# Test 1: Default range
curl -X GET http://localhost:8081/api/resumes/search/score

# Test 2: Specific range
curl -X GET "http://localhost:8081/api/resumes/search/score?minScore=70.0&maxScore=90.0"

# Test 3: High scores only
curl -X GET "http://localhost:8081/api/resumes/search/score?minScore=80.0"

# Test 4: Low scores only  
curl -X GET "http://localhost:8081/api/resumes/search/score?maxScore=50.0"

# Test 5: Invalid range
curl -X GET "http://localhost:8081/api/resumes/search/score?minScore=90.0&maxScore=10.0"
```

#### 2.6 Search Resumes by Text Content
**Endpoint:** `GET /api/resumes/search/text/{keyword}`

```bash
# Test various keywords
curl -X GET http://localhost:8081/api/resumes/search/text/developer
curl -X GET http://localhost:8081/api/resumes/search/text/engineer  
curl -X GET http://localhost:8081/api/resumes/search/text/manager
curl -X GET http://localhost:8081/api/resumes/search/text/experience
curl -X GET http://localhost:8081/api/resumes/search/text/university
```

#### 2.7 Search Resumes by Date Range
**Endpoint:** `GET /api/resumes/search/date-range`

```bash
# Test 1: Last 24 hours
curl -X GET "http://localhost:8081/api/resumes/search/date-range?startDate=2024-01-15T00:00:00&endDate=2024-01-16T00:00:00"

# Test 2: Current week
curl -X GET "http://localhost:8081/api/resumes/search/date-range?startDate=2024-01-10T00:00:00&endDate=2024-01-17T00:00:00"

# Test 3: Invalid date format (should fail)
curl -X GET "http://localhost:8081/api/resumes/search/date-range?startDate=invalid&endDate=invalid"
```

#### 2.8 Get Resume Statistics
**Endpoint:** `GET /api/resumes/statistics`

```bash
# Test: Get statistics
curl -X GET http://localhost:8081/api/resumes/statistics
```

**Expected Response Fields:**
- ✅ `totalResumes`
- ✅ `averageScore`
- ✅ `resumesWithEmail`
- ✅ `resumesWithSkills`
- ✅ `completionRate`

---

### ✅ **3. RESUME MODIFICATION OPERATIONS**

#### 3.1 Update Resume Score
**Endpoint:** `PUT /api/resumes/{id}/score`

```bash
# Test 1: Valid score update
curl -X PUT http://localhost:8081/api/resumes/1/score \
  -H "Content-Type: application/json" \
  -d '{"score": 85.5}'

# Test 2: Invalid score (should fail)
curl -X PUT http://localhost:8081/api/resumes/1/score \
  -H "Content-Type: application/json" \
  -d '{"score": 150.0}'

# Test 3: Missing score field (should fail)
curl -X PUT http://localhost:8081/api/resumes/1/score \
  -H "Content-Type: application/json" \
  -d '{}'

# Test 4: Invalid ID (should fail)
curl -X PUT http://localhost:8081/api/resumes/999999/score \
  -H "Content-Type: application/json" \
  -d '{"score": 75.0}'
```

#### 3.2 Delete Resume
**Endpoint:** `DELETE /api/resumes/{id}`

```bash
# Test 1: Valid deletion
curl -X DELETE http://localhost:8081/api/resumes/1

# Test 2: Invalid ID (should fail)
curl -X DELETE http://localhost:8081/api/resumes/999999

# Test 3: Verify deletion
curl -X GET http://localhost:8081/api/resumes/1
```

---

### ✅ **4. FILE DOWNLOAD OPERATIONS**

#### 4.1 Download Resume by ID
**Endpoint:** `GET /api/resumes/{id}/download`

```bash
# Test 1: Valid download
curl -X GET http://localhost:8081/api/resumes/1/download -o downloaded_resume.pdf

# Test 2: Check headers
curl -I http://localhost:8081/api/resumes/1/download

# Test 3: Invalid ID (should return 404)
curl -X GET http://localhost:8081/api/resumes/999999/download
```

#### 4.2 Download Resume by Email (Legacy)
**Endpoint:** `GET /api/downloadResume/{email}`

```bash
# Test 1: Valid email
curl -X GET http://localhost:8081/api/downloadResume/john.doe@email.com -o downloaded_resume_by_email.pdf

# Test 2: Invalid email (should return 404)
curl -X GET http://localhost:8081/api/downloadResume/notfound@email.com
```

#### 4.3 Get Resume List for Downloads
**Endpoint:** `GET /api/resumesList`

```bash
# Test: Get simplified resume list
curl -X GET http://localhost:8081/api/resumesList
```

**Expected Response Fields:**
- ✅ `resumes` array with simplified info
- ✅ `downloadUrl` for each resume
- ✅ `hasFile` flag
- ✅ `totalCount`

---

### ✅ **5. JOB DESCRIPTION OPERATIONS**

#### 5.1 Get All Job Descriptions
**Endpoint:** `GET /api/matching/jobs`

```bash
# Test: Get all jobs
curl -X GET http://localhost:8081/api/matching/jobs
```

#### 5.2 Get Job Description by ID
**Endpoint:** `GET /api/matching/jobs/{jobId}`

```bash
# Test 1: Valid job ID
curl -X GET http://localhost:8081/api/matching/jobs/1

# Test 2: Invalid job ID (should return 404)
curl -X GET http://localhost:8081/api/matching/jobs/999999
```

---

### ✅ **6. ERROR HANDLING TESTS**

#### 6.1 File Validation Errors
```bash
# Test unsupported file types
curl -X POST http://localhost:8081/api/uploadResume \
  -H "Content-Type: multipart/form-data" \
  -F "file=@test.txt"

curl -X POST http://localhost:8081/api/uploadResume \
  -H "Content-Type: multipart/form-data" \
  -F "file=@test.jpg"
```

#### 6.2 Missing Parameters
```bash
# Test missing file
curl -X POST http://localhost:8081/api/uploadResume

# Test empty request
curl -X PUT http://localhost:8081/api/resumes/1/score \
  -H "Content-Type: application/json"
```

#### 6.3 Invalid JSON
```bash
# Test malformed JSON
curl -X PUT http://localhost:8081/api/resumes/1/score \
  -H "Content-Type: application/json" \
  -d 'invalid json'
```

---

### ✅ **7. INTEGRATION FLOW TESTING**

#### 7.1 Complete Resume Processing Flow
```bash
# Step 1: Upload job description
JOB_RESPONSE=$(curl -s -X POST http://localhost:8081/api/matching/uploadJD \
  -F "title=Senior Java Developer" \
  -F "company=TechCorp" \
  -F "description=Java Spring Boot developer needed" \
  -F "requirements=Java, Spring Boot, SQL")

echo "Job uploaded: $JOB_RESPONSE"

# Step 2: Upload resume (should match against job)
RESUME_RESPONSE=$(curl -s -X POST http://localhost:8081/api/uploadResume \
  -F "file=@test_resume.pdf")

echo "Resume uploaded: $RESUME_RESPONSE"

# Step 3: Extract resume ID and verify data
RESUME_ID=$(echo $RESUME_RESPONSE | jq -r '.resumeId')

# Step 4: Get resume details
curl -X GET http://localhost:8081/api/resumes/$RESUME_ID

# Step 5: Download the file
curl -X GET http://localhost:8081/api/resumes/$RESUME_ID/download -o test_download.pdf

# Step 6: Get statistics
curl -X GET http://localhost:8081/api/resumes/statistics
```

#### 7.2 Search and Filter Flow
```bash
# Upload multiple resumes with different skills
curl -X POST http://localhost:8081/api/uploadResume -F "file=@java_resume.pdf"
curl -X POST http://localhost:8081/api/uploadResume -F "file=@python_resume.pdf"  
curl -X POST http://localhost:8081/api/uploadResume -F "file=@javascript_resume.pdf"

# Search by different skills
curl -X GET http://localhost:8081/api/resumes/search/skill/Java
curl -X GET http://localhost:8081/api/resumes/search/skill/Python
curl -X GET http://localhost:8081/api/resumes/search/skill/JavaScript

# Search by score ranges
curl -X GET "http://localhost:8081/api/resumes/search/score?minScore=80.0"
```

---

### ✅ **8. PERFORMANCE & LOAD TESTING**

#### 8.1 Multiple Concurrent Uploads
```bash
# Test concurrent resume uploads (run in parallel)
for i in {1..5}; do
  curl -X POST http://localhost:8081/api/uploadResume \
    -F "file=@test_resume.pdf" &
done
wait
```

#### 8.2 Large File Handling
```bash
# Test file size limits (should fail gracefully)
curl -X POST http://localhost:8081/api/uploadResume \
  -F "file=@large_file_over_10mb.pdf"
```

---

### ✅ **9. HEALTH & MONITORING**

#### 9.1 Application Health Check
```bash
# Test health endpoint
curl -X GET http://localhost:8081/actuator/health

# Test info endpoint
curl -X GET http://localhost:8081/actuator/info

# Test metrics
curl -X GET http://localhost:8081/actuator/metrics
```

---

## 🎯 **Testing Scenarios Matrix**

| **Category** | **Endpoint** | **Success Test** | **Error Test** | **Edge Case** |
|--------------|--------------|------------------|----------------|---------------|
| **Upload** | POST /api/uploadResume | ✅ PDF upload | ❌ Invalid file type | 📏 Large file |
| **Upload** | POST /api/matching/uploadJD | ✅ With all fields | ❌ No data | 📝 Text only |
| **Retrieve** | GET /api/resumes | ✅ Get all | N/A | 📊 Empty database |
| **Search** | GET /api/resumes/search/email/{email} | ✅ Valid email | ❌ Not found | 🔍 Special chars |
| **Search** | GET /api/resumes/search/skill/{skill} | ✅ Common skill | ❌ Invalid skill | 🔍 Case sensitivity |
| **Search** | GET /api/resumes/search/score | ✅ Valid range | ❌ Invalid range | 📊 Edge values |
| **Download** | GET /api/resumes/{id}/download | ✅ Valid ID | ❌ Invalid ID | 📁 Missing file |
| **Modify** | PUT /api/resumes/{id}/score | ✅ Valid score | ❌ Invalid score | 🔢 Boundary values |
| **Delete** | DELETE /api/resumes/{id} | ✅ Valid ID | ❌ Invalid ID | 🗑️ Already deleted |

---

## 📊 **Expected Test Results Summary**

### ✅ **Success Criteria**
- All uploads return `success: true`
- File downloads work with proper headers
- Search operations return appropriate results
- Error responses include meaningful messages
- Database operations maintain data integrity
- File storage operations work correctly

### 🔍 **Key Metrics to Verify**
- Response times < 5 seconds for file uploads
- Proper HTTP status codes (200, 400, 404, 500)
- Correct content-type headers for downloads
- Consistent JSON response format
- Proper error message structure

### 📝 **Testing Notes**
- Test with various file sizes (small, medium, large)
- Test with different file types and encodings
- Verify URL encoding for special characters
- Test concurrent operations
- Verify file cleanup after deletion
- Check database consistency after operations

---

## 🚨 **Common Issues & Troubleshooting**

### File Upload Issues
- **413 Payload Too Large**: File exceeds 10MB limit
- **415 Unsupported Media Type**: Invalid file type
- **400 Bad Request**: Missing file parameter

### Search Issues
- **URL Encoding**: Use proper encoding for special characters
- **Date Format**: Use ISO 8601 format for date searches
- **Case Sensitivity**: Skills search may be case-sensitive

### Download Issues
- **404 Not Found**: File deleted from disk or invalid ID
- **500 Internal Error**: File permission issues

---

**🎯 Use this checklist systematically to ensure complete functionality testing of your ResumAI API!** 