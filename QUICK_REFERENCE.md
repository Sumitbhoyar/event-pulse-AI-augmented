# EventPulse Quick Reference Card

## 🚀 Start Application

```powershell
# Development (H2 database)
mvn spring-boot:run

# Docker (PostgreSQL + Backend)
docker compose up -d --build

# Or use management script
.\docker-manage.ps1 start
```

## 🌐 Access Points

| Resource | URL | Auth Required |
|----------|-----|---------------|
| Swagger UI | http://localhost:8080/swagger-ui.html | ❌ No |
| Health Check | http://localhost:8080/api/health | ❌ No |
| Actuator Health | http://localhost:8080/actuator/health | ❌ No |
| Actuator Metrics | http://localhost:8080/actuator/metrics | ❌ No |
| H2 Console | http://localhost:8080/h2-console | ❌ No |
| WebSocket Client | http://localhost:8080/ws-client.html | ❌ No |
| Events API | http://localhost:8080/api/events | ✅ JWT |
| Metrics API | http://localhost:8080/api/metrics | ✅ JWT |

## 🔐 Authentication

### Get JWT Token

```powershell
$response = Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/auth/login `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"username":"admin","password":"admin"}'
$token = $response.token
```

**Note**: Demo mode - accepts any username/password!

### Use Token

```powershell
# Include in all protected endpoint requests
-Headers @{ "Authorization" = "Bearer $token" }
```

## 📡 API Endpoints

### Create Event
```powershell
Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/events `
  -Headers @{ 
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json" 
  } `
  -Body '{"source":"test","type":"INFO","message":"Test message","timestamp":"2025-10-18T12:00:00Z"}'
```

### Get All Events
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/events `
  -Headers @{ "Authorization" = "Bearer $token" }
```

### Filter Events
```powershell
# By type
Invoke-RestMethod -Uri "http://localhost:8080/api/events?type=LOGIN" `
  -Headers @{ "Authorization" = "Bearer $token" }

# By source
Invoke-RestMethod -Uri "http://localhost:8080/api/events?source=user-service" `
  -Headers @{ "Authorization" = "Bearer $token" }

# By time range
Invoke-RestMethod -Uri "http://localhost:8080/api/events?from=2025-01-01T00:00:00Z&to=2025-12-31T23:59:59Z" `
  -Headers @{ "Authorization" = "Bearer $token" }
```

### Get Metrics
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/metrics `
  -Headers @{ "Authorization" = "Bearer $token" }
```

## 🐳 Docker Commands

```powershell
# Start
docker compose up -d --build

# Status
docker compose ps

# Logs
docker compose logs -f

# Stop
docker compose stop

# Clean up
docker compose down -v
```

## 📊 Monitoring

```powershell
# Health check
curl http://localhost:8080/actuator/health

# Liveness probe (Kubernetes)
curl http://localhost:8080/actuator/health/liveness

# Readiness probe (Kubernetes)
curl http://localhost:8080/actuator/health/readiness

# Metrics list
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```

## 🧪 Testing

### Swagger UI
1. Open: http://localhost:8080/swagger-ui.html
2. Login via `/api/auth/login` (any username/password)
3. Click "Authorize", enter `Bearer <token>`
4. Test all endpoints interactively

### WebSocket
1. Open: http://localhost:8080/ws-client.html
2. Click "Connect"
3. Click "Send Event"
4. Watch events appear in real-time

### Automated Tests
```powershell
mvn test
```

## 🔧 Configuration Files

| File | Purpose |
|------|---------|
| `application.yaml` | Default config (H2) |
| `application-docker.yaml` | Docker/PostgreSQL config |
| `application-h2.yaml` | H2 development config |
| `docker-compose.yml` | Docker orchestration |
| `Dockerfile` | Multi-stage build |
| `pom.xml` | Maven dependencies |

## 📚 Documentation Files

| File | Description |
|------|-------------|
| `README.md` | Main documentation |
| `API_DOCUMENTATION.md` | Complete API reference |
| `DOCKER_DEPLOYMENT.md` | Docker guide |
| `QUICK_REFERENCE.md` | This file |
| `design/openapi.yaml` | OpenAPI spec |

## 🆘 Common Issues

### Swagger UI shows login popup
**Fix**: Restart application after updating SecurityConfig (removed `.httpBasic()`)

### Can't connect to database
**H2**: No setup needed, works out of the box
**PostgreSQL**: Run `docker compose up -d postgres`

### JWT token expired
**Fix**: Get a new token via `/api/auth/login` (expires after 1 hour)

### Port 8080 already in use
**Fix**: Stop other services or change port in `application.yaml`

## 🎯 One-Line Commands

```powershell
# Start dev mode
mvn spring-boot:run

# Start production (Docker)
docker compose up -d --build

# Test everything
.\docker-manage.ps1 test

# View Swagger
start http://localhost:8080/swagger-ui.html

# Health check
curl http://localhost:8080/api/health

# Get logs
docker compose logs -f
```

## 📞 Support

- Check application logs: `docker compose logs -f eventpulse-backend`
- View database logs: `docker compose logs -f postgres`
- Test connectivity: `curl http://localhost:8080/api/health`
- View metrics: `curl http://localhost:8080/actuator/metrics`

## 🏗️ Project Info

- **Language**: Java 17
- **Framework**: Spring Boot 3.2.0
- **Database**: PostgreSQL 15 / H2
- **Security**: JWT (JJWT 0.11.5)
- **API Docs**: SpringDoc OpenAPI 2.3.0
- **Tests**: JUnit 5 + Mockito

