# Prompt 1 — Project Initialization

> Create a new **Spring Boot (Maven)** project named `eventpulse`.  
> Include dependencies:
> - Spring Web  
> - Spring Data JPA  
> - PostgreSQL Driver  
> - Lombok  
> - Validation  
> - Spring Boot Actuator  
> Use Java 21 and package name `com.eventpulse`.  
> Configure `application.yaml` for PostgreSQL connection (host=localhost, db=eventpulse, user=eventpulse, pass=eventpulse).  
> Create a simple root controller `/api/health` returning `{ "status": "OK" }`.

## Implementation Summary

This prompt established the foundation of the EventPulse project by:

### Files Created:
- `pom.xml` - Maven configuration with all required dependencies
- `src/main/java/com/eventpulse/EventPulseApplication.java` - Main Spring Boot application class
- `src/main/resources/application.yaml` - Database and application configuration
- `src/main/java/com/eventpulse/controller/HealthController.java` - Health check endpoint

### Key Features Implemented:
- Spring Boot 3.2.0 with Java 21
- PostgreSQL database configuration
- Health check endpoint at `/api/health`
- Proper package structure following `com.eventpulse` naming convention
- Maven build configuration with all requested dependencies

### Dependencies Added:
- `spring-boot-starter-web` - REST API support
- `spring-boot-starter-data-jpa` - Database access
- `postgresql` - Database driver
- `lombok` - Code generation
- `spring-boot-starter-validation` - Input validation
- `spring-boot-starter-actuator` - Monitoring endpoints

This initial setup provided a solid foundation for building the event management platform.
