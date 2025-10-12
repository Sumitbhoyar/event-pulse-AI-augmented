# EventPulse Observability & Documentation

This document describes the observability and documentation features implemented in EventPulse.

## 📊 Spring Boot Actuator

EventPulse includes comprehensive monitoring and observability through Spring Boot Actuator endpoints.

### Available Endpoints

| Endpoint | Description | Access |
|----------|-------------|---------|
| `/actuator/health` | Application health status | Public |
| `/actuator/info` | Application information | Public |
| `/actuator/metrics` | Application metrics | Public |
| `/actuator/env` | Environment properties | Protected |
| `/actuator/beans` | Spring beans information | Protected |
| `/actuator/mappings` | Request mappings | Protected |

### Health Check Details

The health endpoint provides detailed information about:
- **Database connectivity** - PostgreSQL connection status
- **Disk space** - Available disk space monitoring
- **Application status** - Overall application health

Example health response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500068036608,
        "free": 350000000000,
        "threshold": 10485760,
        "exists": true
      }
    }
  }
}
```

### Application Information

The info endpoint provides:
- **Build information** - Version, timestamp, Git commit
- **Java information** - JVM version, vendor
- **Environment information** - Active profiles, configuration

### Metrics

Available metrics include:
- **JVM metrics** - Memory usage, GC statistics
- **HTTP metrics** - Request counts, response times
- **Database metrics** - Connection pool statistics
- **Custom metrics** - Event-specific counters

## 📚 OpenAPI Documentation

EventPulse provides comprehensive API documentation using SpringDoc OpenAPI.

### Access Points

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: Available in `design/openapi.yaml`

### Features

- **Interactive API testing** - Try endpoints directly from the browser
- **Authentication support** - JWT token integration
- **Request/Response examples** - Comprehensive examples for all endpoints
- **Schema documentation** - Detailed data models
- **Error response documentation** - All possible error scenarios

### API Categories

#### 1. Authentication (`/api/auth`)
- `POST /api/auth/login` - User authentication
- `GET /api/auth/validate` - Token validation

#### 2. Events (`/api/events`)
- `POST /api/events` - Create new event
- `GET /api/events` - List events with filtering
- `GET /api/events/{id}` - Get specific event
- `DELETE /api/events/{id}` - Delete event

#### 3. Metrics (`/api/metrics`)
- `GET /api/metrics` - Comprehensive metrics
- `GET /api/metrics/total` - Total event count
- `GET /api/metrics/type/{type}` - Events by type
- `GET /api/metrics/source/{source}` - Events by source
- `GET /api/metrics/recent` - Recent events metrics

#### 4. Health (`/api/health`)
- `GET /api/health` - Application health check

## 🔐 Security Integration

### JWT Authentication
- All protected endpoints require JWT authentication
- Token obtained from `/api/auth/login`
- Bearer token format: `Authorization: Bearer <token>`

### Available Test Credentials
```json
{
  "admin": {
    "username": "admin",
    "password": "password",
    "role": "ADMIN"
  },
  "user": {
    "username": "user", 
    "password": "password",
    "role": "USER"
  },
  "eventpulse": {
    "username": "eventpulse",
    "password": "password", 
    "role": "USER"
  }
}
```

## 🚀 Getting Started

### 1. Start the Application
```bash
mvn spring-boot:run
# or
docker-compose up -d
```

### 2. Access Documentation
```bash
# Open Swagger UI
open http://localhost:8080/swagger-ui.html

# Check health
curl http://localhost:8080/api/health

# View metrics
curl http://localhost:8080/actuator/metrics
```

### 3. Test Authentication
```bash
# Login to get JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Use token for protected endpoints
curl -X GET http://localhost:8080/api/events \
  -H "Authorization: Bearer <your-token>"
```

## 📈 Monitoring & Alerting

### Health Checks
The application provides health checks suitable for:
- **Load balancers** - Route traffic based on health status
- **Container orchestrators** - Kubernetes liveness/readiness probes
- **Monitoring systems** - Prometheus, Grafana integration

### Metrics Collection
Metrics can be exported to:
- **Prometheus** - Using Micrometer Prometheus registry
- **JMX** - For Java monitoring tools
- **Custom endpoints** - For specialized monitoring systems

### Logging
Comprehensive logging includes:
- **Request/Response logging** - HTTP request details
- **Security events** - Authentication attempts
- **Business events** - Event creation, metrics queries
- **Error tracking** - Exception details with stack traces

## 🔧 Configuration

### Actuator Configuration
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env,beans,mappings
      base-path: /actuator
  endpoint:
    health:
      show-details: always
      show-components: always
```

### OpenAPI Configuration
```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    tryItOutEnabled: true
  show-actuator: true
```

## 📊 Custom Metrics

EventPulse exposes custom metrics for business monitoring:

### Event Metrics
- `eventpulse.events.created.total` - Total events created
- `eventpulse.events.by.type` - Events by type
- `eventpulse.events.by.source` - Events by source
- `eventpulse.events.deleted.total` - Total events deleted

### Performance Metrics
- `eventpulse.api.response.time` - API response times
- `eventpulse.database.query.time` - Database query performance
- `eventpulse.websocket.connections` - Active WebSocket connections

## 🛠️ Development Tools

### API Testing
Use the integrated Swagger UI for:
- **Interactive testing** - No need for external tools
- **Schema validation** - Automatic request validation
- **Example data** - Pre-filled request examples
- **Authentication testing** - Built-in JWT token support

### Monitoring During Development
- **Real-time metrics** - Monitor application performance
- **Health status** - Quick health verification
- **Environment inspection** - Debug configuration issues
- **Bean inspection** - Understand Spring context

## 📋 Best Practices

### Security
1. **Limit actuator exposure** in production
2. **Use authentication** for sensitive endpoints
3. **Monitor access logs** for suspicious activity
4. **Rotate JWT secrets** regularly

### Performance
1. **Enable metrics collection** for performance monitoring
2. **Set up alerting** for critical metrics
3. **Monitor health endpoints** for uptime
4. **Use custom metrics** for business KPIs

### Documentation
1. **Keep OpenAPI spec updated** with code changes
2. **Provide comprehensive examples** for all endpoints
3. **Document error scenarios** thoroughly
4. **Include authentication requirements** clearly

## 🔍 Troubleshooting

### Common Issues

#### Health Check Failures
```bash
# Check database connectivity
curl http://localhost:8080/actuator/health

# Verify database status
docker-compose logs postgres
```

#### Documentation Not Loading
```bash
# Verify SpringDoc is enabled
curl http://localhost:8080/v3/api-docs

# Check application logs
docker-compose logs eventpulse-backend
```

#### Authentication Issues
```bash
# Test login endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password"}'

# Validate token format
echo "Bearer <your-token>" | base64
```

## 📚 Additional Resources

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Micrometer Documentation](https://micrometer.io/docs)

## 🤝 Contributing

When adding new endpoints:
1. **Add OpenAPI annotations** to controllers
2. **Update the OpenAPI spec** in `design/openapi.yaml`
3. **Include comprehensive examples** for requests/responses
4. **Document error scenarios** and status codes
5. **Test documentation** in Swagger UI
