# Phase 9: API Documentation & Observability

## Objective
Add comprehensive API documentation using Swagger/OpenAPI and enhance monitoring with Spring Boot Actuator.

## Requirements

- Spring Boot Actuator endpoints:
  - `/actuator/health`
  - `/actuator/metrics`
  - `/actuator/info`
- Swagger/OpenAPI documentation for all REST endpoints
- Generate openapi.yaml in design/ folder
- Interactive Swagger UI

## Implementation Steps

### 1. Added SpringDoc OpenAPI Dependency

Updated `pom.xml`:
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**Provides**:
- Swagger UI
- OpenAPI 3.0 specification generation
- Automatic endpoint discovery

### 2. Enhanced Actuator Configuration

Updated `application.yaml`:
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
```

**Endpoints enabled**:
- `/actuator/health` - Detailed health
- `/actuator/health/liveness` - Kubernetes liveness
- `/actuator/health/readiness` - Kubernetes readiness
- `/actuator/metrics` - Application metrics
- `/actuator/info` - Application information
- `/actuator/prometheus` - Prometheus metrics

### 3. OpenAPI Configuration

Created `OpenApiConfig.java`:

```java
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI eventPulseOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("EventPulse API")
                .description("Event management system...")
                .version("1.0.0"))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", 
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement()
                .addList("bearer-jwt"));
    }
}
```

### 4. OpenAPI Annotations on Controllers

**AuthController**:
```java
@Tag(name = "Authentication", description = "...")
@Operation(summary = "User login", description = "...")
@ApiResponses(value = {
    @ApiResponse(responseCode = "200", ...),
    @ApiResponse(responseCode = "400", ...)
})
```

**EventController**:
```java
@Tag(name = "Events", description = "...")
@SecurityRequirement(name = "bearer-jwt")
@Operation(summary = "Create a new event", ...)
@Parameter(description = "Filter by event type")
```

**MetricsController** and **HealthController** similarly annotated.

### 5. Updated Security Configuration

Modified `SecurityConfig.java` to permit:
```java
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
```

**Removed**:
```java
.httpBasic(Customizer.withDefaults())  // Caused auth popup
```

### 6. OpenAPI Specification

Created `design/openapi.yaml`:
- Complete OpenAPI 3.0.1 specification
- All endpoints documented
- Request/response schemas
- Examples for all operations
- Security schemes
- Can be imported into Postman, Insomnia

### 7. SpringDoc Configuration

Added to `application.yaml`:
```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
  show-actuator: true
```

## Files Created/Modified

- `src/main/java/com/eventpulse/config/OpenApiConfig.java` (new)
- `design/openapi.yaml` (new)
- `API_DOCUMENTATION.md` (new)
- All controllers (modified with OpenAPI annotations)
- `src/main/resources/application.yaml` (modified)
- `src/main/java/com/eventpulse/config/SecurityConfig.java` (modified)

## Technical Details

### Swagger UI Access

**URL**: http://localhost:8080/swagger-ui.html

**Features**:
- Interactive API testing
- Try endpoints directly in browser
- View request/response schemas
- See examples
- Authenticate with JWT

**Authentication Flow in Swagger**:
1. Test `/api/auth/login` to get token
2. Click "Authorize" button
3. Enter `Bearer <token>`
4. Test protected endpoints

### Actuator Endpoints

**Health Information**:
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"},
    "livenessState": {"status": "UP"},
    "readinessState": {"status": "UP"}
  }
}
```

**Metrics**:
- JVM metrics (memory, threads, GC)
- HTTP metrics (requests, response times)
- Database metrics (connection pool)
- Custom metrics

**Application Info**:
- Java version
- OS information
- Build details
- Custom properties

### OpenAPI Specification Structure

```yaml
openapi: 3.0.1
info: { title, description, version, contact, license }
servers: [ { url, description } ]
tags: [ name, description ]
paths:
  /api/auth/login: { post: { ... } }
  /api/events: { post: {...}, get: {...} }
  /api/metrics: { get: {...} }
components:
  securitySchemes: { bearer-jwt }
  schemas: { LoginRequest, EventRequest, EventResponse, ... }
```

## Challenges Encountered

### Basic Auth Popup on Swagger UI
**Problem**: Spring Security's httpBasic caused browser auth popup.

**Root Cause**: 
```java
.httpBasic(Customizer.withDefaults())
```

**Solution**: Removed httpBasic configuration entirely:
- JWT authentication only
- No basic auth prompts
- Swagger UI directly accessible

### Actuator Endpoint Security
**Decision**: Make actuator endpoints public vs protected.

**Choice**: Public for development, should be protected in production.

**Rationale**:
- Easy access during development
- Health checks need to be public for Docker/Kubernetes
- Can be restricted per environment

### OpenAPI Security Scheme
**Challenge**: Document JWT authentication clearly.

**Solution**:
```yaml
securitySchemes:
  bearer-jwt:
    type: http
    scheme: bearer
    bearerFormat: JWT
```

Applied globally and per-endpoint as needed.

## Documentation Created

### API_DOCUMENTATION.md Contents

1. **Quick Links** - Swagger UI, OpenAPI endpoints
2. **Using Swagger UI** - Step-by-step authentication
3. **Actuator Endpoints** - Health, metrics, info details
4. **API Endpoints** - Complete reference with examples
5. **Data Models** - Schema documentation
6. **Error Responses** - Error format examples
7. **Authentication Flow** - Complete guide
8. **WebSocket Support** - Connection details
9. **Integration Examples** - Python, Java, Node.js code

## Configuration Details

### SpringDoc Settings

```yaml
springdoc:
  api-docs:
    path: /api-docs           # OpenAPI JSON endpoint
  swagger-ui:
    path: /swagger-ui.html    # Swagger UI path
    operationsSorter: method  # Sort by HTTP method
    tagsSorter: alpha         # Sort tags alphabetically
  show-actuator: true         # Include actuator endpoints
```

### Actuator Settings

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: always    # Show full health details
      probes:
        enabled: true         # Enable k8s probes
```

## Best Practices Applied

1. **OpenAPI annotations** on all endpoints
2. **Clear descriptions** for parameters
3. **Request/response examples** provided
4. **Security documented** (JWT requirement)
5. **Public endpoints clearly marked**
6. **Actuator endpoints exposed** appropriately
7. **Health probes** for Kubernetes
8. **Prometheus metrics** for monitoring

## Outcome

✅ Swagger UI accessible at /swagger-ui.html  
✅ No authentication popup (fixed)  
✅ All endpoints documented with OpenAPI annotations  
✅ Complete OpenAPI 3.0 spec in design/openapi.yaml  
✅ Actuator health checks working  
✅ Kubernetes-ready health probes  
✅ Metrics available for monitoring  
✅ Application info endpoint  
✅ Comprehensive API documentation file

