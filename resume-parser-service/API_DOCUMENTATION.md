# Resume Parser Service - API Documentation

## Overview
The Resume Parser Service provides comprehensive functionality for parsing resumes and matching them with job descriptions using advanced similarity algorithms including TF-IDF and cosine similarity.

## Base URL
```
http://localhost:8081/api
```

## Core API Endpoints (Simplified)

### 1. Upload Resume (Complete Processing)
```http
POST /api/uploadResume
Content-Type: multipart/form-data
```
**Parameters:**
- `file`: Resume file (PDF or DOC only)

**What it does:**
- ✅ Validates file type (PDF/DOC only)
- ✅ Extracts text using Apache Tika
- ✅ Parses email, skills, experience
- ✅ Scores against ALL job descriptions
- ✅ Saves to `parsed_resumes` database table
- ✅ Returns comprehensive results

**Response:**
```json
{
  "success": true,
  "message": "Resume uploaded, parsed, scored, and saved successfully",
  "resumeId": 1,
  "originalFileName": "john_doe_resume.pdf",
  "fileSize": 245760,
  "contentType": "application/pdf",
  "detectedType": "application/pdf",
  "uploadTime": "2024-01-15T10:30:00",
  "extractedText": "John Doe\nSenior Software Engineer...",
  "textLength": 1542,
  "email": "john.doe@email.com",
  "skills": ["Java", "Spring Boot", "SQL", "AWS"],
  "skillsCount": 4,
  "experience": "5+ years software development",
  "bestMatchScore": 85.67,
  "totalJobsMatched": 3,
  "bestMatchJob": {
    "jobId": 2,
    "jobTitle": "Senior Java Developer", 
    "company": "TechCorp Inc",
    "matchCategory": "Excellent Match"
  },
  "savedToDatabase": true,
  "databaseId": 1,
  "fileSaved": true,
  "filePath": "uploaded-resumes/uuid-filename.pdf",
  "downloadUrl": "/api/resumes/1/download"
}
```

### 2. Upload Job Description (File or Text)
```http
POST /api/matching/uploadJD
Content-Type: multipart/form-data
```
**Parameters:**
- `file` (optional): Job description file (PDF, DOC, DOCX, RTF, TXT)
- `title` (optional): Job title (will be extracted from file if not provided)
- `company` (optional): Company name (will be extracted from file if not provided) 
- `description` (optional): Job description text (use if not uploading file)
- `requirements` (optional): Job requirements (will be extracted from file if not provided)
- `responsibilities` (optional): Job responsibilities (will be extracted from file if not provided)
- `location` (optional): Job location (will be extracted from file if not provided)
- `experienceLevel` (optional): Experience level (will be extracted from file if not provided)

**Response:**
```json
{
  "success": true,
  "message": "Job description uploaded and saved successfully",
  "jobId": 1,
  "job": {
    "id": 1,
    "title": "Senior Software Engineer",
    "company": "TechCorp Inc",
    "description": "Full job description text...",
    "requirements": "5+ years Java experience...",
    "responsibilities": "Design and develop applications...",
    "location": "New York, NY",
    "experienceLevel": "Senior",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  },
  "originalFileName": "job_description.pdf",
  "fileSize": 32768,
  "extractedTextLength": 2045
}
```

---

## Additional Endpoints (Read-Only Operations)

### Get All Job Descriptions
```http
GET /api/matching/jobs
```
**Response:**
```json
{
  "success": true,
  "message": "Job descriptions retrieved successfully",
  "totalJobs": 2,
  "jobs": [
    {
      "id": 1,
      "title": "Senior Software Engineer",
      "company": "TechCorp Inc",
      "description": "We are looking for an experienced software engineer...",
      "requirements": "5+ years Java experience, Spring Boot, microservices...",
      "responsibilities": "Design and develop scalable applications...",
      "location": "New York, NY",
      "experienceLevel": "Senior",
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

### Get Specific Job Description
```http
GET /api/matching/jobs/{jobId}
```
**Response:**
```json
{
  "success": true,
  "message": "Job description retrieved successfully",
  "job": {
    "id": 1,
    "title": "Senior Software Engineer",
    "company": "TechCorp Inc",
    "description": "We are looking for an experienced software engineer...",
    "requirements": "5+ years Java experience, Spring Boot, microservices...",
    "responsibilities": "Design and develop scalable applications...",
    "location": "New York, NY",
    "experienceLevel": "Senior",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### Create Job Description (JSON API)
```http
POST /api/matching/jobs
Content-Type: application/json
```
**Request Body:**
```json
{
  "title": "Senior Software Engineer",
  "company": "TechCorp Inc",
  "description": "We are looking for an experienced software engineer...",
  "requirements": "5+ years Java experience, Spring Boot, microservices...",
  "responsibilities": "Design and develop scalable applications...",
  "location": "New York, NY",
  "experienceLevel": "Senior"
}
```

### Download Original Resume File
```http
GET /api/resumes/{id}/download
```
**Parameters:**
- `id`: Resume ID from database

**Response:**
- Downloads the original resume file with proper content-type headers
- Filename preserved from original upload
- Returns 404 if resume not found or file doesn't exist on disk

**Example:**
```bash
curl -o downloaded_resume.pdf http://localhost:8081/api/resumes/1/download
```

---

## 🎯 **Why This Simplified API?**

All resume matching functionality has been **consolidated** into `/api/uploadResume`:
- ✅ **Automatic matching** against ALL job descriptions  
- ✅ **Best score calculation** and job recommendation
- ✅ **Complete parsing** and database storage
- ✅ **Comprehensive response** with all extracted data

**No need for separate matching endpoints** - everything happens automatically!

## Similarity Scoring Explained

### Overall Score Calculation
The overall score is a weighted combination of multiple similarity measures:

- **Overall Similarity (25%)**: TF-IDF cosine similarity between full texts
- **Weighted Similarity (25%)**: Weighted comparison of job components
- **Skill Match (30%)**: Percentage of required skills found in resume
- **Title Match (10%)**: Similarity between resume and job title
- **Requirements Match (10%)**: Similarity with specific requirements

### Match Categories
- **Excellent Match**: 80%+ similarity
- **Good Match**: 60-79% similarity  
- **Fair Match**: 40-59% similarity
- **Poor Match**: 20-39% similarity
- **No Match**: <20% similarity

### Skill Extraction
The system automatically extracts skills from both resume and job descriptions using a comprehensive keyword dictionary including:
- Programming languages (Java, Python, JavaScript, etc.)
- Frameworks (Spring, React, Angular, etc.)
- Databases (SQL, MongoDB, Redis, etc.)
- Cloud platforms (AWS, Azure, GCP)
- Development tools (Git, Docker, Kubernetes, etc.)

## Error Handling

All endpoints return standardized error responses:
```json
{
  "success": false,
  "message": "Error description",
  "details": "Additional error details if applicable"
}
```

Common HTTP status codes:
- `200`: Success
- `400`: Bad Request (invalid input, unsupported file type)
- `404`: Not Found (job description not found)
- `500`: Internal Server Error

## Example Usage with cURL

### Upload and process a resume (main endpoint):
```bash
curl -X POST \
  http://localhost:8081/api/uploadResume \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@resume.pdf'
```

### Upload a job description:
```bash
curl -X POST \
  http://localhost:8081/api/matching/uploadJD \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@job_description.pdf' \
  -F 'title=Senior Java Developer' \
  -F 'company=TechCorp'
```

### Create a job description via JSON:
```bash
curl -X POST \
  http://localhost:8081/api/matching/jobs \
  -H 'Content-Type: application/json' \
  -d '{
    "title": "Senior Java Developer",
    "company": "TechCorp",
    "description": "Seeking experienced Java developer...",
    "requirements": "Java, Spring Boot, microservices",
    "location": "Remote"
  }'
```

## Application Health

Monitor application health using Spring Boot Actuator:
```http
GET /actuator/health
```

## Notes
- **Resume file restrictions**: PDF and DOC only
- **Job description files**: PDF, DOC, DOCX, RTF, TXT supported
- Maximum file size: 10MB
- The service automatically registers with Eureka server at startup
- In-memory SQLite database stores parsed resumes and job descriptions
- **Original resume files** are saved to `uploaded-resumes/` directory
- All similarity scores are returned as percentages (0-100)
- Resume processing is **comprehensive**: upload → parse → score → save → store file in one call
- **File downloads** available via `/api/resumes/{id}/download` endpoint 