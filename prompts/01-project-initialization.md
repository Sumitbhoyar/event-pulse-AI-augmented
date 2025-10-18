# Phase 1: Project Initialization

## Objective
Create a new Spring Boot (Maven) project with basic structure and PostgreSQL configuration.

## Requirements

- Project name: `eventpulse`
- Build tool: Maven
- Java version: 21 (later changed to 17 for compatibility)
- Package name: `com.eventpulse`

### Dependencies
- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Lombok (later removed due to annotation processing issues)
- Validation
- Spring Boot Actuator

### Configuration
- PostgreSQL connection: host=localhost, db=eventpulse, user=eventpulse, pass=eventpulse
- Simple health controller at `/api/health` returning `{"status": "OK"}`

## Implementation Steps

1. Created `pom.xml` with Spring Boot 3.2.0 parent
2. Added all required dependencies
3. Created `src/main/resources/application.yaml` with PostgreSQL configuration
4. Created main application class `EventpulseApplication.java`
5. Created `HealthController.java` with `/api/health` endpoint

## Files Created

- `pom.xml`
- `src/main/resources/application.yaml`
- `src/main/java/com/eventpulse/EventpulseApplication.java`
- `src/main/java/com/eventpulse/controller/HealthController.java`

## Challenges Encountered

### PostgreSQL Connection Error
**Problem**: Application failed to start due to PostgreSQL not running locally.

**Solutions Provided**:
1. Docker Compose setup for PostgreSQL
2. H2 in-memory database as alternative
3. Updated configuration to use H2 by default

### Lombok Annotation Processing
**Problem**: Lombok annotations weren't generating getter/setter/builder methods, causing compilation errors.

**Solution**: Removed Lombok dependency and manually implemented:
- Getter/setter methods
- Builder pattern
- Constructors

## Key Decisions

1. **Java 17 vs 21**: Changed from Java 21 to 17 due to Maven compiler plugin compatibility
2. **H2 as Default**: Configured H2 as default database for easier development
3. **Manual POJOs**: Removed Lombok in favor of manual implementations for reliability

## Outcome

✅ Basic Spring Boot project successfully created  
✅ Health endpoint working  
✅ Application compiles and runs  
✅ H2 in-memory database configured

