# Eureka Registry Server

A Spring Boot microservice that acts as a service discovery registry for the ResumAI ecosystem. This server allows microservices to register themselves and discover other services in the system.

## Overview

This Eureka Server provides:
- Service registration and discovery
- Health monitoring of registered services
- Load balancing support
- Failover capabilities
- Management and monitoring endpoints

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Netflix Eureka**: Service Discovery Server

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6+

### Building the Application

```bash
mvn clean compile
```

### Running the Application

```bash
mvn spring-boot:run
```

The Eureka Server will start on port `8761` by default.

### Accessing the Eureka Dashboard

Once the application is running, you can access the Eureka dashboard at:

```
http://localhost:8761
```

## Configuration

The application is configured through `application.properties`:

- **Server Port**: 8761 (standard Eureka port)
- **Service Name**: eureka-server
- **Register with Eureka**: Disabled (server mode)
- **Fetch Registry**: Disabled (server mode)

## Registering Microservices

To register a microservice with this Eureka server, add the following to your client application's `application.properties`:

```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
spring.application.name=your-service-name
```

And include the Eureka client dependency:

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

## Health Monitoring

The server exposes several management endpoints:

- **Health**: `/actuator/health`
- **Info**: `/actuator/info`
- **Metrics**: `/actuator/metrics`

## Testing

Run the test suite:

```bash
mvn test
```

## Packaging

Create a JAR file:

```bash
mvn clean package
```

## Docker Deployment

To run in a Docker container, create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/eureka-server-1.0.0.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Production Considerations

For production deployment:

1. **Security**: Enable security configurations
2. **Clustering**: Set up multiple Eureka servers for high availability
3. **Network Configuration**: Configure appropriate hostnames and ports
4. **Monitoring**: Set up comprehensive logging and monitoring
5. **Resource Limits**: Configure appropriate JVM heap sizes

## Contributing

1. Follow Spring Boot best practices
2. Maintain minimal dependencies
3. Update documentation for any configuration changes 