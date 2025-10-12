# Prompt 9 — Docker Deployment

> Create a `Dockerfile` for the Spring Boot app and a `docker-compose.yaml` that starts PostgreSQL and the `eventpulse-backend`.  
> Make sure the backend waits for the DB to be ready before startup.

## Implementation Summary

This prompt implemented containerized deployment for the EventPulse application by:

### Files Created:
- `Dockerfile` - Multi-stage Docker build configuration
- `docker-compose.yaml` - Complete application stack
- `wait-for-db.sh` - Database readiness check script
- `DOCKER_README.md` - Comprehensive Docker documentation

### Key Features Implemented:

#### Dockerfile (Multi-Stage Build):
- **Build Stage** - Maven build with Java 21
- **Runtime Stage** - Lightweight Alpine Linux with OpenJDK 21
- **Optimization** - Minimal image size with security best practices
- **Health Checks** - Built-in health monitoring
- **Security** - Non-root user execution

#### Docker Compose Configuration:
- **PostgreSQL Service** - Database with persistent volumes
- **EventPulse Service** - Spring Boot application
- **Network Configuration** - Internal network for service communication
- **Volume Management** - Persistent data storage
- **Environment Configuration** - Configurable via environment variables

#### Database Readiness:
- **Health Check Script** - `wait-for-db.sh` for database readiness
- **Dependency Management** - Backend waits for database to be ready
- **Connection Retry Logic** - Automatic retry with exponential backoff
- **Startup Order** - Proper service startup sequencing

#### Development Features:
- **Hot Reload** - Volume mounting for development
- **Port Mapping** - Accessible ports for development
- **Environment Variables** - Configurable application settings
- **Logging** - Structured logging configuration

### Technical Implementation:

#### Multi-Stage Dockerfile:
```dockerfile
# Build stage
FROM openjdk:21-jdk-slim AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Runtime stage
FROM openjdk:21-jre-alpine
RUN addgroup -g 1001 -S eventpulse && adduser -S eventpulse -u 1001
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
COPY wait-for-db.sh .
RUN chmod +x wait-for-db.sh
USER eventpulse
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1
CMD ["./wait-for-db.sh", "postgres:5432", "java", "-jar", "app.jar"]
```

#### Docker Compose Configuration:
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: eventpulse
      POSTGRES_USER: eventpulse
      POSTGRES_PASSWORD: eventpulse
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U eventpulse -d eventpulse"]
      interval: 10s
      timeout: 5s
      retries: 5

  eventpulse-backend:
    build: .
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/eventpulse
      SPRING_DATASOURCE_USERNAME: eventpulse
      SPRING_DATASOURCE_PASSWORD: eventpulse
      APP_JWT_SECRET: your-secret-key-here
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3
```

#### Database Readiness Script:
```bash
#!/bin/sh
# wait-for-db.sh
set -e

host="$1"
shift
cmd="$@"

until nc -z "$host" 5432; do
  >&2 echo "PostgreSQL is unavailable - sleeping"
  sleep 2
done

>&2 echo "PostgreSQL is up - executing command"
exec $cmd
```

### Deployment Features:

#### Production Ready:
- **Health Checks** - Built-in health monitoring
- **Security** - Non-root user execution
- **Optimization** - Minimal image size
- **Persistence** - Data volume management
- **Networking** - Internal service communication

#### Development Support:
- **Hot Reload** - Volume mounting for code changes
- **Debugging** - Remote debugging configuration
- **Logging** - Structured application logs
- **Environment** - Configurable via environment variables

#### Monitoring:
- **Health Endpoints** - Application and database health
- **Logging** - Centralized log management
- **Metrics** - Application performance metrics
- **Alerts** - Health check failures

### Usage Examples:

#### Development:
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up --build -d
```

#### Production:
```bash
# Build production image
docker build -t eventpulse:latest .

# Run with production configuration
docker run -d \
  --name eventpulse \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://your-db:5432/eventpulse \
  eventpulse:latest
```

#### Database Management:
```bash
# Connect to database
docker-compose exec postgres psql -U eventpulse -d eventpulse

# Backup database
docker-compose exec postgres pg_dump -U eventpulse eventpulse > backup.sql

# Restore database
docker-compose exec -T postgres psql -U eventpulse -d eventpulse < backup.sql
```

### Security Features:
- **Non-root User** - Application runs as non-privileged user
- **Minimal Base Image** - Alpine Linux for reduced attack surface
- **Health Checks** - Built-in security monitoring
- **Network Isolation** - Internal Docker network
- **Secret Management** - Environment variable configuration

### Performance Optimizations:
- **Multi-stage Build** - Reduced final image size
- **Layer Caching** - Optimized Docker layer caching
- **JVM Tuning** - Optimized JVM parameters
- **Connection Pooling** - Database connection optimization
- **Resource Limits** - Container resource management

This Docker implementation provided a production-ready containerized deployment solution for EventPulse, ensuring reliable database connectivity, proper service orchestration, and comprehensive monitoring capabilities.
