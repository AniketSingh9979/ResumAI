# Resume Parser Service

A Spring Boot microservice that provides resume parsing capabilities using Apache Tika. This service is part of the ResumAI ecosystem and integrates with Eureka for service discovery.

## Features

- **Resume Parsing**: Extract text content from various resume formats (PDF, DOC, DOCX, etc.) using Apache Tika
- **Data Extraction**: Automatically extract key information such as:
  - Email addresses
  - Phone numbers
  - Skills
  - Education details
  - Work experience
- **Database Storage**: Store parsed resume data in SQLite database
- **REST API**: RESTful endpoints for uploading and parsing resumes
- **Service Discovery**: Registered with Eureka server for microservice architecture
- **Development Tools**: Hot reload with Spring Boot DevTools

## Dependencies

- Spring Boot Web
- Spring Data JPA
- Eureka Discovery Client
- SQLite JDBC Driver
- Apache Tika (Core & Standard Parsers)
- Lombok
- Spring Boot DevTools

## Configuration

The service runs on port `8081` and connects to:
- Eureka Server: `http://localhost:8761/eureka/`
- SQLite Database: `./resume_parser.db`

## API Endpoints

### Parse Resume
```
POST /api/resume-parser/parse
Content-Type: multipart/form-data
Parameter: file (MultipartFile)
```

Uploads and parses a resume file, returning extracted information in JSON format.

### Health Check
```
GET /api/resume-parser/health
```

Returns service health status.

## Running the Service

1. Ensure the Eureka server is running on port 8761
2. Build the project: `mvn clean install`
3. Run the application: `mvn spring-boot:run`

The service will register itself with the Eureka server and be available at `http://localhost:8081`

## Database

The service uses SQLite for simplicity and stores parsed resume data in the `parsed_resumes` table with the following fields:
- id (Primary Key)
- fileName
- fileSize
- contentType
- rawContent (Full extracted text)
- email (Extracted email address)
- parsedAt (Timestamp)

## Development

The service includes Spring Boot DevTools for automatic restarts during development. Simply modify any source file and the application will automatically restart. 