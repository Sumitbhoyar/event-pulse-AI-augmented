# Docker Deployment Guide

This guide explains how to deploy the EventPulse application using Docker and Docker Compose.

## Prerequisites

- Docker 20.10 or higher
- Docker Compose 2.0 or higher
- 2GB free disk space
- Ports 8080 and 5432 available

## Quick Start

### 1. Build and Start All Services

```powershell
docker compose up -d --build
```

This command will:
- Build the Spring Boot application Docker image
- Start PostgreSQL database
- Start the EventPulse backend
- Wait for PostgreSQL to be ready before starting the backend

### 2. Check Service Status

```powershell
# View running containers
docker compose ps

# View logs
docker compose logs -f

# View backend logs only
docker compose logs -f eventpulse-backend

# View database logs only
docker compose logs -f postgres
```

### 3. Test the Application

```powershell
# Health check (public)
curl http://localhost:8080/api/health

# Access Swagger UI (public)
# Open in browser: http://localhost:8080/swagger-ui.html

# Login to get JWT token
$response = Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/auth/login `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"username":"admin","password":"admin"}'
$token = $response.token

# Create an event
Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/events `
  -Headers @{ 
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json" 
  } `
  -Body '{"source":"docker-test","type":"INFO","message":"Docker deployment test","timestamp":"2025-10-18T12:00:00Z"}'
```

## Docker Architecture

### Services

#### 1. PostgreSQL Database (`postgres`)
- **Image**: `postgres:15-alpine`
- **Port**: 5432
- **Database**: eventpulse
- **User/Password**: eventpulse/eventpulse
- **Volume**: `postgres_data` (persistent storage)
- **Health Check**: pg_isready check every 10 seconds

#### 2. EventPulse Backend (`eventpulse-backend`)
- **Image**: Built from Dockerfile (multi-stage build)
- **Port**: 8080
- **Profile**: docker
- **Health Check**: `/api/health` endpoint check every 30 seconds
- **Depends On**: postgres (waits for healthy status)
- **Resource Limits**: 1 CPU, 1GB RAM

### Network

All services run on the `eventpulse-network` bridge network, allowing them to communicate by service name.

## Configuration

### Environment Variables

You can override default configuration using environment variables:

```powershell
# Set custom JWT secret
$env:JWT_SECRET = "your-custom-base64-secret-here"

# Set custom JWT validity (in milliseconds)
$env:JWT_VALIDITY_MS = 7200000  # 2 hours

# Start with custom config
docker compose up -d
```

### Application Profiles

The application uses the `docker` profile by default, which:
- Connects to PostgreSQL at `postgres:5432`
- Disables verbose SQL logging
- Enables production-ready settings
- Configures graceful shutdown

## Dockerfile Details

The Dockerfile uses a **multi-stage build** approach:

### Stage 1: Build
- Base image: `maven:3.9.6-eclipse-temurin-17`
- Downloads dependencies (cached layer)
- Builds the application JAR
- Skips tests for faster builds

### Stage 2: Runtime
- Base image: `eclipse-temurin:17-jre-alpine` (minimal JRE)
- Creates non-root user `spring` for security
- Copies only the JAR file from build stage
- Configures JVM for container environment
- Exposes port 8080
- Includes health check

### Benefits
- **Smaller image size**: Only JRE in final image (~200MB vs ~600MB)
- **Security**: Runs as non-root user
- **Performance**: Optimized JVM settings for containers
- **Caching**: Dependencies layer is cached

## Management Commands

### Start Services

```powershell
docker compose up -d
```

### Stop Services

```powershell
docker compose stop
```

### Restart Services

```powershell
docker compose restart
```

### Rebuild and Restart

```powershell
docker compose up -d --build
```

### View Logs

```powershell
# All services
docker compose logs -f

# Specific service
docker compose logs -f eventpulse-backend

# Last 100 lines
docker compose logs --tail=100
```

### Execute Commands in Container

```powershell
# Access backend container shell
docker compose exec eventpulse-backend sh

# Access PostgreSQL shell
docker compose exec postgres psql -U eventpulse -d eventpulse
```

### Clean Up

```powershell
# Stop and remove containers
docker compose down

# Remove containers, networks, and volumes
docker compose down -v

# Remove everything including images
docker compose down -v --rmi all
```

## Database Operations

### Access PostgreSQL

```powershell
docker compose exec postgres psql -U eventpulse -d eventpulse
```

### Run SQL Queries

```sql
-- View all events
SELECT * FROM events ORDER BY timestamp DESC LIMIT 10;

-- Count events by type
SELECT type, COUNT(*) FROM events GROUP BY type;

-- View recent events
SELECT * FROM events WHERE timestamp > NOW() - INTERVAL '1 hour';
```

### Backup Database

```powershell
docker compose exec postgres pg_dump -U eventpulse eventpulse > backup.sql
```

### Restore Database

```powershell
Get-Content backup.sql | docker compose exec -T postgres psql -U eventpulse -d eventpulse
```

## Health Checks

### Backend Health

The backend includes a health check that:
- Calls `/api/health` endpoint
- Runs every 30 seconds
- Allows 60 seconds for startup
- Retries 3 times before marking unhealthy

### Database Health

The PostgreSQL service includes a health check that:
- Runs `pg_isready` command
- Checks every 10 seconds
- Allows 10 seconds for startup
- Retries 5 times before marking unhealthy

### Check Health Status

```powershell
docker compose ps
```

Look for `(healthy)` status in the output.

## Resource Management

### View Resource Usage

```powershell
docker stats
```

### Configured Limits

- **Backend**:
  - CPU Limit: 1.0 core
  - Memory Limit: 1GB
  - CPU Reservation: 0.5 cores
  - Memory Reservation: 512MB

### Adjust Resources

Edit `docker-compose.yml` and modify the `deploy.resources` section:

```yaml
deploy:
  resources:
    limits:
      cpus: '2.0'      # Increase CPU limit
      memory: 2G       # Increase memory limit
```

## Troubleshooting

### Backend Won't Start

1. Check if PostgreSQL is healthy:
   ```powershell
   docker compose ps postgres
   ```

2. View backend logs:
   ```powershell
   docker compose logs eventpulse-backend
   ```

3. Ensure PostgreSQL is ready:
   ```powershell
   docker compose exec postgres pg_isready -U eventpulse
   ```

### Database Connection Errors

1. Check network connectivity:
   ```powershell
   docker compose exec eventpulse-backend ping postgres
   ```

2. Verify database credentials in docker-compose.yml

3. Check if database was initialized properly:
   ```powershell
   docker compose logs postgres | Select-String "database system is ready"
   ```

### Port Already in Use

If port 8080 or 5432 is already in use:

1. Find the process using the port:
   ```powershell
   netstat -ano | findstr :8080
   ```

2. Either stop that process or change the port in `docker-compose.yml`:
   ```yaml
   ports:
     - "8081:8080"  # Map to different host port
   ```

### Out of Memory

If the backend runs out of memory:

1. Increase memory limit in `docker-compose.yml`
2. Or reduce JVM memory in `Dockerfile`:
   ```dockerfile
   -XX:MaxRAMPercentage=50.0
   ```

### Slow Startup

The backend has a 60-second startup period. If it takes longer:

1. Check available system resources
2. Review logs for initialization issues
3. Increase `start_period` in health check:
   ```yaml
   healthcheck:
     start_period: 120s
   ```

## Production Deployment

### Security Recommendations

1. **Change Default Credentials**:
   ```yaml
   environment:
     POSTGRES_PASSWORD: ${DB_PASSWORD}
     JWT_SECRET: ${JWT_SECRET}
   ```

2. **Use Docker Secrets** for sensitive data

3. **Enable TLS/SSL** for PostgreSQL connection

4. **Run behind reverse proxy** (nginx, Traefik)

5. **Enable firewall** rules to restrict access

### Monitoring

1. **Enable Prometheus metrics**:
   - Metrics available at `/actuator/prometheus`

2. **Set up log aggregation**:
   - Use Docker logging drivers
   - Forward to ELK, Splunk, or CloudWatch

3. **Configure alerts**:
   - Monitor health check failures
   - Track resource usage
   - Alert on errors in logs

### Backup Strategy

1. **Automated database backups**:
   ```powershell
   # Add to scheduled task
   docker compose exec postgres pg_dump -U eventpulse eventpulse | gzip > "backup-$(Get-Date -Format 'yyyyMMdd').sql.gz"
   ```

2. **Volume backups**:
   ```powershell
   docker run --rm -v eventpulse_postgres_data:/data -v ${PWD}:/backup alpine tar czf /backup/postgres-data-backup.tar.gz /data
   ```

## Scaling

### Horizontal Scaling

To run multiple backend instances:

```powershell
docker compose up -d --scale eventpulse-backend=3
```

**Note**: Requires load balancer configuration.

### Vertical Scaling

Increase resources in `docker-compose.yml`:

```yaml
deploy:
  resources:
    limits:
      cpus: '4.0'
      memory: 4G
```

## CI/CD Integration

### Build Image

```powershell
docker build -t eventpulse:latest .
```

### Tag and Push

```powershell
docker tag eventpulse:latest your-registry/eventpulse:v1.0.0
docker push your-registry/eventpulse:v1.0.0
```

### Deploy

```powershell
docker compose pull
docker compose up -d
```

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [Spring Boot Docker Guide](https://spring.io/guides/topicals/spring-boot-docker/)

