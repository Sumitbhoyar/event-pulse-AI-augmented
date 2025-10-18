# EventPulse

A production-ready Spring Boot application for event management with real-time WebSocket support, JWT authentication, metrics aggregation, and comprehensive API documentation.

[![Build and Push](https://github.com/your-username/EventPulse-AI-Augmented/workflows/Build%20and%20Push/badge.svg)](https://github.com/your-username/EventPulse-AI-Augmented/actions)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 🚀 Features

- 🔐 **JWT Authentication** - Secure API endpoints with JSON Web Tokens
- 📊 **Event Management** - Create, retrieve, and filter events with validation
- 📈 **Metrics Aggregation** - Real-time event statistics using JPQL queries
- 🔌 **WebSocket Support** - Real-time event broadcasting via STOMP over WebSocket
- 💾 **Dual Database Support** - PostgreSQL (production) and H2 (development)
- ✅ **Input Validation** - Bean validation with custom error responses
- 🛡️ **Global Exception Handling** - Consistent error response format
- 📚 **OpenAPI/Swagger** - Interactive API documentation with Swagger UI
- 📊 **Spring Boot Actuator** - Health checks, metrics, and monitoring endpoints
- 🐳 **Docker Support** - Multi-stage Dockerfile and Docker Compose orchestration
- 🧪 **Comprehensive Tests** - JUnit 5 + Mockito unit and integration tests

## 📋 Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **Docker Desktop** (optional, for containerized deployment)

## 🎯 Quick Start

### Option 1: Using Docker (Recommended for Production)

**Prerequisites**: Docker Desktop running

```powershell
# Build and start all services (PostgreSQL + Backend)
docker compose up -d --build

# Check status
docker compose ps

# View logs
docker compose logs -f eventpulse-backend

# Test the application
.\docker-manage.ps1 test
```

The application will be available at `http://localhost:8080` with PostgreSQL running in a container.

📖 **Detailed guide**: [DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)

### Option 2: Using H2 In-Memory Database (Fastest for Development)

The application is pre-configured to use H2 database by default:

```powershell
mvn spring-boot:run
```

**Access H2 Console**:
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:eventpulse`
- Username: `sa`
- Password: (leave empty)

### Option 3: Using Docker PostgreSQL Only + Local Backend

**1. Start PostgreSQL only**:
```powershell
docker compose up -d postgres
```

**2. Update `application.yaml`** to use PostgreSQL (uncomment PostgreSQL config)

**3. Run application locally**:
```powershell
mvn spring-boot:run
```

## 📚 Documentation

- **[README.md](README.md)** - This file (Quick start and overview)
- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Complete API reference with examples
- **[DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)** - Docker deployment guide
- **[design/openapi.yaml](design/openapi.yaml)** - OpenAPI 3.0 specification

## 🌐 Access Points

Once the application is running:

| Resource | URL | Description |
|----------|-----|-------------|
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Interactive API documentation |
| **OpenAPI Spec** | http://localhost:8080/v3/api-docs | OpenAPI JSON specification |
| **Health Check** | http://localhost:8080/api/health | Simple health status |
| **Actuator Health** | http://localhost:8080/actuator/health | Detailed health information |
| **Actuator Metrics** | http://localhost:8080/actuator/metrics | Application metrics |
| **Actuator Info** | http://localhost:8080/actuator/info | Application information |
| **H2 Console** | http://localhost:8080/h2-console | H2 database console (dev only) |
| **WebSocket Client** | http://localhost:8080/ws-client.html | WebSocket test client |

## 🔑 Authentication

### Quick Authentication Guide

**1. Login to get JWT token**:
```powershell
$response = Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/auth/login `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"username":"admin","password":"admin"}'

$token = $response.token
Write-Host "Token: $token"
```

**Note**: For demo purposes, the login accepts **any username/password** combination.

**2. Use token for protected endpoints**:
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/events `
  -Headers @{ "Authorization" = "Bearer $token" }
```

### Public Endpoints (No Auth Required)
- `GET /api/health`
- `POST /api/auth/login`
- `GET /actuator/**`
- `GET /swagger-ui/**`
- `GET /h2-console/**`
- `GET /ws`, `/ws/**`

### Protected Endpoints (JWT Required)
- `POST /api/events`
- `GET /api/events`
- `GET /api/metrics`

## 📡 API Endpoints

### Health Check (Public)

```powershell
curl http://localhost:8080/api/health
```

**Response**: `{"status":"OK"}`

### Authentication

```powershell
# Login
Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/auth/login `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"username":"admin","password":"admin"}'
```

**Response**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Create Event (Protected)

```powershell
Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/events `
  -Headers @{ 
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json" 
  } `
  -Body '{"source":"user-service","type":"LOGIN","message":"User logged in","timestamp":"2025-10-18T12:00:00Z"}'
```

**Response** (201 Created):
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "source": "user-service",
  "type": "LOGIN",
  "message": "User logged in",
  "timestamp": "2025-10-18T12:00:00Z"
}
```

### Get Events (Protected)

```powershell
# Get all events
Invoke-RestMethod -Uri http://localhost:8080/api/events `
  -Headers @{ "Authorization" = "Bearer $token" }

# Filter by type
Invoke-RestMethod -Uri "http://localhost:8080/api/events?type=LOGIN" `
  -Headers @{ "Authorization" = "Bearer $token" }

# Filter by source
Invoke-RestMethod -Uri "http://localhost:8080/api/events?source=user-service" `
  -Headers @{ "Authorization" = "Bearer $token" }

# Filter by time range
Invoke-RestMethod -Uri "http://localhost:8080/api/events?from=2025-01-01T00:00:00Z&to=2025-12-31T23:59:59Z" `
  -Headers @{ "Authorization" = "Bearer $token" }
```

### Get Metrics (Protected)

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/metrics `
  -Headers @{ "Authorization" = "Bearer $token" }
```

**Response**:
```json
{
  "totalCount": 150,
  "countByType": {
    "LOGIN": 50,
    "LOGOUT": 30,
    "ERROR": 20
  },
  "countBySource": {
    "user-service": 80,
    "payment-service": 40
  }
}
```

## 🔌 WebSocket Real-Time Events

### Live Event Notifications

When you create an event via `POST /api/events`, it's automatically broadcast to all WebSocket subscribers.

**WebSocket Endpoint**: `ws://localhost:8080/ws`

**Event Topic**: `/topic/events`

### Test WebSocket

Open the built-in client:
```
http://localhost:8080/ws-client.html
```

1. Click **"Connect"** to establish WebSocket connection
2. You'll see subscription confirmation
3. Click **"Send Event"** to create a test event
4. Watch the event appear in real-time in the log

## 📊 Monitoring & Observability

### Actuator Endpoints

**Health Check** (Detailed):
```powershell
curl http://localhost:8080/actuator/health
```

**Kubernetes Probes**:
```powershell
# Liveness
curl http://localhost:8080/actuator/health/liveness

# Readiness
curl http://localhost:8080/actuator/health/readiness
```

**Application Metrics**:
```powershell
# List all metrics
curl http://localhost:8080/actuator/metrics

# Specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used
curl http://localhost:8080/actuator/metrics/http.server.requests
```

**Application Info**:
```powershell
curl http://localhost:8080/actuator/info
```

**Prometheus Metrics**:
```powershell
curl http://localhost:8080/actuator/prometheus
```

## 📁 Project Structure

```
eventpulse/
├── src/
│   ├── main/
│   │   ├── java/com/eventpulse/
│   │   │   ├── EventpulseApplication.java          # Main application
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java              # Swagger/OpenAPI config
│   │   │   │   ├── SecurityConfig.java             # JWT & Spring Security
│   │   │   │   └── WebSocketConfig.java            # STOMP WebSocket config
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java             # /api/auth/login
│   │   │   │   ├── EventController.java            # /api/events
│   │   │   │   ├── HealthController.java           # /api/health
│   │   │   │   └── MetricsController.java          # /api/metrics
│   │   │   ├── dto/
│   │   │   │   ├── EventRequest.java               # Event creation DTO
│   │   │   │   ├── EventResponse.java              # Event response DTO
│   │   │   │   ├── LoginRequest.java               # Login credentials
│   │   │   │   ├── LoginResponse.java              # JWT token response
│   │   │   │   └── MetricsResponse.java            # Metrics DTO
│   │   │   ├── entity/
│   │   │   │   └── Event.java                      # JPA entity
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java     # @RestControllerAdvice
│   │   │   │   └── InvalidRequestException.java    # Custom exception
│   │   │   ├── repository/
│   │   │   │   └── EventRepository.java            # JPA + JPQL queries
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java    # JWT validation filter
│   │   │   │   └── JwtUtils.java                   # JWT generation/validation
│   │   │   └── service/
│   │   │       ├── EventService.java               # Event business logic
│   │   │       └── MetricsService.java             # Metrics aggregation
│   │   └── resources/
│   │       ├── application.yaml                    # Main config (H2)
│   │       ├── application-docker.yaml             # Docker/PostgreSQL config
│   │       ├── application-h2.yaml                 # H2 development config
│   │       └── static/
│   │           └── ws-client.html                  # WebSocket test client
│   └── test/
│       └── java/com/eventpulse/
│           ├── controller/
│           │   ├── EventControllerTest.java        # Unit tests
│           │   ├── EventControllerIntegrationTest.java  # Integration tests
│           │   └── TestSecurityConfig.java         # Test security config
│           └── service/
│               ├── EventServiceTest.java           # Service unit tests
│               └── MetricsServiceTest.java         # Metrics unit tests
├── design/
│   └── openapi.yaml                                # OpenAPI 3.0 specification
├── pom.xml                                         # Maven dependencies
├── Dockerfile                                      # Multi-stage Docker build
├── docker-compose.yml                              # Docker orchestration
├── docker-manage.ps1                               # Docker management script
├── .dockerignore                                   # Docker build exclusions
├── README.md                                       # This file
├── API_DOCUMENTATION.md                            # Complete API reference
└── DOCKER_DEPLOYMENT.md                            # Docker deployment guide
```

## 🛠️ Technology Stack

### Core Framework
- **Spring Boot 3.2.0** - Application framework
- **Java 17** - Programming language
- **Maven** - Build and dependency management

### Spring Modules
- **Spring Web** - REST API endpoints
- **Spring Data JPA** - Database access and JPQL queries
- **Spring Security** - JWT authentication and authorization
- **Spring WebSocket** - Real-time STOMP messaging
- **Spring Boot Actuator** - Production monitoring

### Database
- **PostgreSQL 15** - Production database
- **H2 Database** - In-memory database for development
- **Hibernate** - JPA implementation

### Security & Authentication
- **Spring Security** - Security framework
- **JJWT 0.11.5** - JWT token generation and validation

### Documentation & API
- **SpringDoc OpenAPI 2.3.0** - OpenAPI/Swagger integration
- **Swagger UI 5.10.3** - Interactive API documentation

### Testing
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **MockMvc** - Spring MVC testing
- **AssertJ** - Fluent assertions
- **Spring Security Test** - Security testing utilities

### DevOps
- **Docker** - Containerization
- **Docker Compose** - Multi-container orchestration

## 💻 Development

### Build the Project

```powershell
mvn clean package
```

### Run Tests

```powershell
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=EventServiceTest

# Run with coverage
mvn test jacoco:report
```

**Test Coverage**:
- ✅ EventService: 10 unit tests
- ✅ MetricsService: 6 unit tests
- ✅ EventController: 10 unit tests + 9 integration tests

### Run Locally (Development)

```powershell
mvn spring-boot:run
```

Application starts with H2 in-memory database on `http://localhost:8080`

### Package for Deployment

```powershell
mvn clean package -DskipTests
```

JAR file created at: `target/eventpulse-0.0.1-SNAPSHOT.jar`

### Run JAR

```powershell
java -jar target/eventpulse-0.0.1-SNAPSHOT.jar
```

## 📖 Complete API Workflow Example

```powershell
# 1. Start the application
mvn spring-boot:run

# 2. Login and get JWT token
$response = Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/auth/login `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"username":"admin","password":"admin"}'
$token = $response.token

# 3. Create events
$events = @(
  @{source="user-service"; type="LOGIN"; message="User john logged in"; timestamp=(Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")},
  @{source="payment-service"; type="PAYMENT"; message="Payment processed"; timestamp=(Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")},
  @{source="user-service"; type="LOGOUT"; message="User john logged out"; timestamp=(Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")}
)

foreach ($event in $events) {
  Invoke-RestMethod -Method POST `
    -Uri http://localhost:8080/api/events `
    -Headers @{ 
      "Authorization" = "Bearer $token"
      "Content-Type" = "application/json" 
    } `
    -Body ($event | ConvertTo-Json)
}

# 4. Get all events
$allEvents = Invoke-RestMethod -Uri http://localhost:8080/api/events `
  -Headers @{ "Authorization" = "Bearer $token" }
$allEvents | ConvertTo-Json

# 5. Get metrics
$metrics = Invoke-RestMethod -Uri http://localhost:8080/api/metrics `
  -Headers @{ "Authorization" = "Bearer $token" }
$metrics | ConvertTo-Json

# 6. Filter events by type
$loginEvents = Invoke-RestMethod `
  -Uri "http://localhost:8080/api/events?type=LOGIN" `
  -Headers @{ "Authorization" = "Bearer $token" }
$loginEvents | ConvertTo-Json
```

## 🐳 Docker Deployment

### Quick Docker Start

```powershell
# Using Docker Compose
docker compose up -d --build

# Or using the management script
.\docker-manage.ps1 start
```

### Docker Management Script

```powershell
# Start services
.\docker-manage.ps1 start

# Check status
.\docker-manage.ps1 status

# View logs
.\docker-manage.ps1 logs

# Test application
.\docker-manage.ps1 test

# Backup database
.\docker-manage.ps1 backup

# Stop services
.\docker-manage.ps1 stop

# Clean up everything
.\docker-manage.ps1 clean
```

### Docker Services

- **postgres**: PostgreSQL 15 database with persistent storage
- **eventpulse-backend**: Spring Boot application

Both services include health checks and automatic restart policies.

## 🧪 Testing

### Using Swagger UI

1. Open: http://localhost:8080/swagger-ui.html
2. Test `/api/auth/login` to get a token
3. Click **"Authorize"** button
4. Enter: `Bearer <your-token>`
5. Test all endpoints interactively

### Using PowerShell

See examples above or check [API_DOCUMENTATION.md](API_DOCUMENTATION.md)

### Using cURL

```bash
# Login
curl.exe -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Create event (replace <token> with actual token)
curl.exe -X POST http://localhost:8080/api/events \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"source":"test","type":"INFO","message":"Test","timestamp":"2025-10-18T12:00:00Z"}'
```

## ⚙️ Configuration

### Database Configuration

**H2 (Default)**:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:eventpulse
    driver-class-name: org.h2.Driver
```

**PostgreSQL**:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/eventpulse
    username: eventpulse
    password: eventpulse
    driver-class-name: org.postgresql.Driver
```

### JWT Configuration

```yaml
security:
  jwt:
    secret: <base64-encoded-secret-256-bits>
    validity-ms: 3600000  # 1 hour
```

**Environment Variables**:
```powershell
$env:JWT_SECRET = "your-base64-secret"
$env:JWT_VALIDITY_MS = 7200000  # 2 hours
```

### Actuator Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

## 🔍 Troubleshooting

### Application Won't Start

**Check Java version**:
```powershell
java -version
# Should be Java 17 or higher
```

**Check port availability**:
```powershell
netstat -ano | findstr :8080
```

### Database Connection Issues

**Using H2** (default):
- No external database needed
- Access H2 console: http://localhost:8080/h2-console

**Using PostgreSQL**:
1. Ensure PostgreSQL is running:
   ```powershell
   docker compose ps postgres
   ```
2. Check database logs:
   ```powershell
   docker compose logs postgres
   ```

### Swagger UI Issues

**Can't access Swagger UI**:
1. Verify application is running: http://localhost:8080/api/health
2. Check URL: http://localhost:8080/swagger-ui.html (note the `.html`)
3. Clear browser cache
4. Check application logs for errors

**401/403 errors in Swagger UI**:
1. Login via `/api/auth/login` endpoint in Swagger
2. Copy the JWT token from response
3. Click "Authorize" button (🔒 icon)
4. Enter: `Bearer <token>`
5. Click "Authorize" and "Close"

### JWT Token Issues

**Token expired**:
- Default expiry: 1 hour
- Request a new token via `/api/auth/login`

**Invalid token**:
- Ensure you're using the format: `Bearer <token>`
- Don't include quotes around the token
- Token should start with `eyJ...`

### WebSocket Connection Issues

1. Ensure `/ws` endpoint is accessible
2. Check browser console for errors
3. Verify STOMP connection in ws-client.html
4. Check application logs for WebSocket errors

## 📚 Complete Documentation

| Document | Description |
|----------|-------------|
| **[README.md](README.md)** | Main documentation (this file) - Quick start and overview |
| **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** | Complete API reference with examples in Python/Java/Node.js |
| **[DOCKER_DEPLOYMENT.md](DOCKER_DEPLOYMENT.md)** | Docker deployment guide and best practices |
| **[CICD_DOCUMENTATION.md](CICD_DOCUMENTATION.md)** | GitHub Actions CI/CD setup and workflows |
| **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** | One-page cheat sheet for common tasks |
| **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** | Project overview, metrics, and architecture |
| **[design/openapi.yaml](design/openapi.yaml)** | OpenAPI 3.0 specification for API contract |

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 API Specification

The complete OpenAPI 3.0 specification is available at:
- **Static file**: `design/openapi.yaml`
- **JSON endpoint**: http://localhost:8080/v3/api-docs
- **YAML endpoint**: http://localhost:8080/v3/api-docs.yaml

Import this into:
- Postman
- Insomnia
- API testing tools
- Code generators

## 🚦 Health & Status

### Check Application Health

```powershell
# Simple health check
curl http://localhost:8080/api/health

# Detailed health (database, disk space, etc.)
curl http://localhost:8080/actuator/health

# Kubernetes liveness probe
curl http://localhost:8080/actuator/health/liveness

# Kubernetes readiness probe
curl http://localhost:8080/actuator/health/readiness
```

### Monitor Metrics

```powershell
# List available metrics
curl http://localhost:8080/actuator/metrics

# JVM memory usage
curl http://localhost:8080/actuator/metrics/jvm.memory.used

# HTTP request metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Database connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

## 🎓 Learning Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/)
- [Spring Security JWT Guide](https://docs.spring.io/spring-security/)
- [Spring WebSocket Guide](https://docs.spring.io/spring-framework/reference/web/websocket.html)
- [SpringDoc Documentation](https://springdoc.org/)
- [Docker Documentation](https://docs.docker.com/)

## 📄 License

This project is created for demonstration purposes.

## 🎯 Next Steps

1. **Explore Swagger UI**: http://localhost:8080/swagger-ui.html
2. **Test WebSocket**: http://localhost:8080/ws-client.html
3. **Monitor Health**: http://localhost:8080/actuator/health
4. **Deploy with Docker**: `docker compose up -d --build`

---

**Built with ❤️ using Spring Boot**
