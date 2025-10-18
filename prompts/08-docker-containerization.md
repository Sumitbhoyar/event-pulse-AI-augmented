# Phase 8: Docker Containerization

## Objective
Create production-ready Docker deployment with multi-stage builds and container orchestration.

## Requirements

- Dockerfile for Spring Boot application
- docker-compose.yaml with PostgreSQL and backend
- Backend must wait for database to be ready
- Health checks for both services
- Persistent storage for database

## Implementation Steps

### 1. Multi-stage Dockerfile

Created `Dockerfile` with two stages:

**Stage 1: Build**
```dockerfile
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B
```

**Stage 2: Runtime**
```dockerfile
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build /app/target/eventpulse-*.jar app.jar
RUN chown spring:spring app.jar
USER spring:spring
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/health || exit 1
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
CMD ["--spring.profiles.active=docker"]
```

**Benefits**:
- **Smaller image**: ~200MB (JRE only) vs ~600MB (full JDK)
- **Security**: Non-root user
- **Performance**: Container-aware JVM
- **Caching**: Dependencies cached in separate layer

### 2. Docker Compose Orchestration

Created `docker-compose.yml`:

**PostgreSQL Service**:
```yaml
postgres:
  image: postgres:15-alpine
  environment:
    POSTGRES_DB: eventpulse
    POSTGRES_USER: eventpulse
    POSTGRES_PASSWORD: eventpulse
  volumes:
    - postgres_data:/var/lib/postgresql/data
  healthcheck:
    test: ["CMD-SHELL", "pg_isready -U eventpulse -d eventpulse"]
    interval: 10s
    timeout: 5s
    retries: 5
    start_period: 10s
```

**EventPulse Backend Service**:
```yaml
eventpulse-backend:
  build:
    context: .
    dockerfile: Dockerfile
  environment:
    SPRING_PROFILES_ACTIVE: docker
    SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/eventpulse
  ports:
    - "8080:8080"
  depends_on:
    postgres:
      condition: service_healthy
  healthcheck:
    test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/api/health"]
    interval: 30s
    start_period: 60s
  deploy:
    resources:
      limits:
        cpus: '1.0'
        memory: 1G
```

**Key Features**:
- Network isolation
- Health-based dependency
- Resource limits
- Persistent volumes
- Automatic restart

### 3. Application Configuration for Docker

Created `application-docker.yaml`:
- PostgreSQL connection (hostname: `postgres`)
- Production logging levels
- Graceful shutdown configuration
- Kubernetes-ready health probes
- Environment variable support

### 4. Docker Build Optimization

Created `.dockerignore`:
- Excludes unnecessary files (tests, docs, IDE files)
- Reduces build context size
- Faster builds
- Smaller images

### 5. Docker Management Script

Created `docker-manage.ps1`:

**Commands**:
- `start` - Start all services
- `stop` - Stop services
- `restart` - Restart services
- `rebuild` - Rebuild and restart
- `logs` - View logs
- `status` - Check health
- `test` - Run automated tests
- `backup` - Backup database
- `clean` - Remove everything

## Files Created

- `Dockerfile`
- `docker-compose.yml`
- `.dockerignore`
- `src/main/resources/application-docker.yaml`
- `docker-manage.ps1`
- `DOCKER_DEPLOYMENT.md`

## Technical Details

### Health Check Strategy

**PostgreSQL**:
- Command: `pg_isready -U eventpulse -d eventpulse`
- Interval: 10 seconds
- Start period: 10 seconds
- Retries: 5

**Backend**:
- Command: HTTP GET to `/api/health`
- Interval: 30 seconds
- Start period: 60 seconds (allows startup time)
- Retries: 3

### Dependency Management

```yaml
depends_on:
  postgres:
    condition: service_healthy
```

**Ensures**:
1. PostgreSQL starts first
2. Health check passes
3. Backend waits for healthy status
4. Backend starts only when DB ready

### Resource Limits

```yaml
deploy:
  resources:
    limits:
      cpus: '1.0'
      memory: 1G
    reservations:
      cpus: '0.5'
      memory: 512M
```

**Purpose**:
- Prevents resource exhaustion
- Predictable performance
- Kubernetes-compatible syntax

### Network Architecture

```
Host Machine
     ↓
Port 8080 → eventpulse-backend:8080
Port 5432 → postgres:5432
     ↓
eventpulse-network (bridge)
     ↓
postgres ←→ eventpulse-backend
(service discovery by name)
```

## Challenges Encountered

### Docker Desktop Not Running
**Problem**: User's Docker Desktop wasn't running initially.

**Solutions Provided**:
1. Instructions to start Docker Desktop
2. H2 alternative for quick development
3. Local PostgreSQL installation option

### Container Startup Ordering
**Problem**: Backend tried to connect before database was ready.

**Solution**: Used `depends_on` with health condition:
```yaml
depends_on:
  postgres:
    condition: service_healthy
```

### JVM Memory in Containers
**Problem**: JVM doesn't automatically detect container memory limits.

**Solution**: Added container-aware JVM flags:
```dockerfile
-XX:+UseContainerSupport
-XX:MaxRAMPercentage=75.0
```

## Docker Commands Reference

```powershell
# Build and start
docker compose up -d --build

# View logs
docker compose logs -f eventpulse-backend

# Check status
docker compose ps

# Execute commands in container
docker compose exec eventpulse-backend sh
docker compose exec postgres psql -U eventpulse -d eventpulse

# Stop services
docker compose down

# Remove volumes
docker compose down -v
```

## Best Practices Implemented

1. **Multi-stage builds**: Separate build and runtime
2. **Non-root user**: Security best practice
3. **Health checks**: Automatic monitoring
4. **Resource limits**: Prevent resource issues
5. **Persistent volumes**: Data survives restarts
6. **Network isolation**: Security through isolation
7. **Minimal base image**: Alpine Linux for small size
8. **Build caching**: Layer optimization

## Outcome

✅ Multi-stage Dockerfile created (~200MB image)  
✅ Docker Compose with health checks  
✅ Backend waits for database  
✅ Persistent PostgreSQL storage  
✅ Resource limits configured  
✅ Management script for easy operations  
✅ Complete Docker documentation

