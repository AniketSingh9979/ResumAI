# Mail Service Microservice

A Spring Boot microservice that handles email notifications for candidate selection and rejection in the ResumAI ecosystem. This service provides REST endpoints to send notifications to both candidates and recruiters.

## Overview

This Mail Service provides:
- Selection notification emails to candidates and recruiters
- Rejection notification emails to candidates and recruiters
- Integration with Eureka service discovery
- REST API endpoints for mail operations
- Comprehensive validation and error handling

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Spring Mail**: Email sending capabilities
- **Netflix Eureka**: Service Discovery Client
- **Maven**: Dependency management

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+
- SMTP server configuration (Gmail, Outlook, etc.)
- Running Eureka Server (port 8761)

### Building the Application

```bash
mvn clean compile
```

### Running the Application

```bash
mvn spring-boot:run
```

The Mail Service will start on port `8082` by default and register with Eureka server.

## Configuration

### SMTP Configuration

Update `application.properties` with your SMTP settings:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### Environment Variables

Set the following environment variables for production:

```bash
export MAIL_USERNAME=your-email@gmail.com
export MAIL_PASSWORD=your-app-password
```

## API Endpoints

### Base URL
```
http://localhost:8082/mail-service/api/mail
```

### 1. Send Selection Notification

**POST** `/selected`

Sends success notification to both candidate and recruiter.

**Request Body:**
```json
{
  "candidate": {
    "name": "John Doe",
    "email": "john.doe@example.com"
  },
  "recruiter": {
    "name": "Jane Smith",
    "email": "jane.smith@company.com"
  }
}
```

**Response:**
```json
"Selection notification sent successfully to candidate: John Doe and recruiter: Jane Smith"
```

### 2. Send Rejection Notification

**POST** `/not-selected`

Sends rejection notification to both candidate and recruiter.

**Request Body:**
```json
{
  "candidate": {
    "name": "John Doe",
    "email": "john.doe@example.com"
  },
  "recruiter": {
    "name": "Jane Smith",
    "email": "jane.smith@company.com"
  }
}
```

**Response:**
```json
"Rejection notification sent successfully to candidate: John Doe and recruiter: Jane Smith"
```

### 3. Health Check

**GET** `/health`

Returns the health status of the service.

**Response:**
```json
"Mail service is running"
```

## Data Models

### Candidate

```java
public class Candidate {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}
```

### Recruiter

```java
public class Recruiter {
    @NotBlank(message = "Name is required")
    private String name;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
}
```

### MailRequest

```java
public class MailRequest {
    @NotNull(message = "Candidate is required")
    @Valid
    private Candidate candidate;
    
    @NotNull(message = "Recruiter is required")
    @Valid
    private Recruiter recruiter;
}
```

## Email Templates

### Selection Email (Candidate)
```
Subject: Congratulations! You've been selected

Dear [Candidate Name],

Congratulations! We are pleased to inform you that you have been selected for the position.

Best regards,
ResumAI Team
```

### Rejection Email (Candidate)
```
Subject: Application Update

Dear [Candidate Name],

Thank you for your interest in the position. After careful consideration, we have decided to move forward with other candidates.

Best regards,
ResumAI Team
```

### Notification Email (Recruiter)
```
Subject: Candidate Selected/Rejected

Dear [Recruiter Name],

The candidate [Candidate Name] has been [selected/rejected] and notified.

Best regards,
ResumAI System
```

## Testing

Run the test suite:

```bash
mvn test
```

### Manual Testing with cURL

**Selection Notification:**
```bash
curl -X POST http://localhost:8082/mail-service/api/mail/selected \
  -H "Content-Type: application/json" \
  -d '{
    "candidate": {
      "name": "John Doe",
      "email": "john.doe@example.com"
    },
    "recruiter": {
      "name": "Jane Smith",
      "email": "jane.smith@company.com"
    }
  }'
```

**Rejection Notification:**
```bash
curl -X POST http://localhost:8082/mail-service/api/mail/not-selected \
  -H "Content-Type: application/json" \
  -d '{
    "candidate": {
      "name": "John Doe",
      "email": "john.doe@example.com"
    },
    "recruiter": {
      "name": "Jane Smith",
      "email": "jane.smith@company.com"
    }
  }'
```

## Monitoring

### Health Endpoints

- **Health**: `/actuator/health`
- **Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`

### Service Discovery

The service registers with Eureka server at startup and can be discovered by other microservices.

## Error Handling

The service provides comprehensive error handling:

- **Validation Errors**: 400 Bad Request for invalid input
- **Mail Server Errors**: 500 Internal Server Error for SMTP issues
- **Service Errors**: 500 Internal Server Error for unexpected failures

## Packaging

Create a JAR file:

```bash
mvn clean package
```

## Docker Support

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/mail-service-1.0.0.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Production Considerations

1. **Security**: Use encrypted passwords and secure SMTP connections
2. **Rate Limiting**: Implement rate limiting for email sending
3. **Queue Management**: Consider using message queues for high volume
4. **Monitoring**: Set up comprehensive logging and monitoring
5. **Failover**: Configure backup SMTP servers

## Contributing

1. Follow Spring Boot best practices
2. Maintain comprehensive validation
3. Add appropriate logging
4. Update documentation for any changes 