# EventPulse Docker Setup

This document provides instructions for running EventPulse using Docker and Docker Compose.

## Prerequisites

- Docker (version 20.10 or higher)
- Docker Compose (version 2.0 or higher)

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd EventPulse
```

### 2. Start the Application

```bash
# Start all services (PostgreSQL + EventPulse Backend)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### 3. Access the Application

- **EventPulse API**: http://localhost:8080
- **Health Check**: http://localhost:8080/api/health
- **API Documentation**: http://localhost:8080/swagger-ui.html (if enabled)

## Services

### PostgreSQL Database
- **Port**: 5432
- **Database**: eventpulse
- **Username**: eventpulse
- **Password**: eventpulse
- **Health Check**: Built-in PostgreSQL health check

### EventPulse Backend
- **Port**: 8080
- **Profile**: docker
- **Health Check**: HTTP endpoint at `/api/health`
- **Dependencies**: Waits for PostgreSQL to be ready before starting

### pgAdmin (Optional)
- **Port**: 5050
- **Email**: admin@eventpulse.com
- **Password**: admin123
- **Usage**: `docker-compose --profile admin up -d`

## Environment Variables

The following environment variables can be customized:

### Backend Service
```yaml
SPRING_PROFILES_ACTIVE: docker
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/eventpulse
SPRING_DATASOURCE_USERNAME: eventpulse
SPRING_DATASOURCE_PASSWORD: eventpulse
APP_JWT_SECRET: your-jwt-secret-here
APP_JWT_EXPIRATION: 86400000
```

### Database Service
```yaml
POSTGRES_DB: eventpulse
POSTGRES_USER: eventpulse
POSTGRES_PASSWORD: eventpulse
```

## Development Commands

### Build and Run
```bash
# Build the application
docker-compose build

# Run in development mode (with logs)
docker-compose up

# Run specific service
docker-compose up postgres
docker-compose up eventpulse-backend
```

### Database Management
```bash
# Access PostgreSQL directly
docker-compose exec postgres psql -U eventpulse -d eventpulse

# View database logs
docker-compose logs postgres

# Reset database (removes all data)
docker-compose down -v
docker-compose up -d
```

### Application Management
```bash
# View application logs
docker-compose logs eventpulse-backend

# Restart application
docker-compose restart eventpulse-backend

# Execute commands in running container
docker-compose exec eventpulse-backend bash
```

### Health Checks
```bash
# Check all services health
docker-compose ps

# Test database connection
docker-compose exec eventpulse-backend curl -f http://localhost:8080/api/health

# Test API endpoints
curl -X GET http://localhost:8080/api/health
```

## Production Considerations

### Security
1. **Change default passwords** in production
2. **Use secrets management** for sensitive data
3. **Enable SSL/TLS** for database connections
4. **Restrict network access** using Docker networks

### Performance
1. **Adjust JVM settings** in `JAVA_OPTS`
2. **Configure connection pooling** in `application-docker.yaml`
3. **Set appropriate memory limits** for containers
4. **Use volume mounts** for persistent data

### Monitoring
1. **Enable health checks** (already configured)
2. **Set up log aggregation**
3. **Monitor resource usage**
4. **Configure alerting**

## Troubleshooting

### Common Issues

#### Database Connection Issues
```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# Check database logs
docker-compose logs postgres

# Test database connectivity
docker-compose exec postgres pg_isready -U eventpulse -d eventpulse
```

#### Application Startup Issues
```bash
# Check application logs
docker-compose logs eventpulse-backend

# Verify database readiness
docker-compose exec postgres psql -U eventpulse -d eventpulse -c "SELECT 1;"

# Check health endpoint
curl http://localhost:8080/api/health
```

#### Port Conflicts
```bash
# Check port usage
netstat -tulpn | grep :8080
netstat -tulpn | grep :5432

# Use different ports in docker-compose.yaml
ports:
  - "8081:8080"  # Use port 8081 instead of 8080
```

### Logs and Debugging
```bash
# Follow all logs
docker-compose logs -f

# Follow specific service logs
docker-compose logs -f eventpulse-backend
docker-compose logs -f postgres

# Show last 100 lines
docker-compose logs --tail=100 eventpulse-backend
```

## Cleanup

```bash
# Stop and remove containers
docker-compose down

# Remove containers, networks, and volumes
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Complete cleanup (removes everything)
docker system prune -a
```

## Advanced Usage

### Custom Configuration
Create a `.env` file to override default values:
```bash
# .env
POSTGRES_PASSWORD=my-secure-password
APP_JWT_SECRET=my-super-secret-jwt-key
SPRING_PROFILES_ACTIVE=docker,production
```

### Scaling
```bash
# Scale backend service (requires load balancer)
docker-compose up -d --scale eventpulse-backend=3
```

### Backup and Restore
```bash
# Backup database
docker-compose exec postgres pg_dump -U eventpulse eventpulse > backup.sql

# Restore database
docker-compose exec -T postgres psql -U eventpulse eventpulse < backup.sql
```

## Support

For issues and questions:
1. Check the logs: `docker-compose logs`
2. Verify health checks: `docker-compose ps`
3. Test connectivity: Use the provided curl commands
4. Review this documentation for troubleshooting steps

