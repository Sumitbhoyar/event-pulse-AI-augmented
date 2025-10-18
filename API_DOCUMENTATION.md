# EventPulse API Documentation

This document provides comprehensive API documentation for the EventPulse application.

## Quick Links

- **Swagger UI**: http://localhost:8080/swagger-ui.html *(No authentication required)*
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **OpenAPI YAML**: http://localhost:8080/v3/api-docs.yaml
- **Actuator Endpoints**: http://localhost:8080/actuator *(Public access)*
- **WebSocket Test Client**: http://localhost:8080/ws-client.html

## Important Notes

- ✅ **Swagger UI is publicly accessible** - No login popup, no credentials needed
- ✅ **Actuator endpoints are public** - Health, metrics, and info available without authentication
- ✅ **API endpoints require JWT** - Use `/api/auth/login` to get a token for protected endpoints (`/api/events`, `/api/metrics`)

## API Documentation Tools

### Swagger UI (Interactive Documentation)

Access the interactive API documentation at:
```
http://localhost:8080/swagger-ui.html
```

Features:
- **Try it out**: Test all endpoints directly from the browser
- **JWT Authentication**: Configure bearer token for protected endpoints
- **Request/Response Examples**: See example payloads
- **Schema Documentation**: View all data models

#### Using Swagger UI with JWT

To test **protected endpoints** (`/api/events`, `/api/metrics`) in Swagger UI:

1. **Get a token**: 
   - Click on `POST /api/auth/login` 
   - Click "Try it out"
   - Enter any username/password (e.g., `admin`/`admin`)
   - Click "Execute"
   - Copy the `token` value from the response

2. **Authorize Swagger UI**:
   - Click the **"Authorize"** button (🔒 icon at the top right)
   - In the "Value" field, enter: `Bearer <your-token>`
   - Click **"Authorize"**
   - Click **"Close"**

3. **Test protected endpoints**:
   - Now all your requests will include the JWT token automatically
   - Try `GET /api/events` or `GET /api/metrics`

**Note**: Public endpoints like `/api/health` work without authorization.

### OpenAPI Specification

The OpenAPI 3.0 specification is available in multiple formats:

**JSON Format**:
```
http://localhost:8080/v3/api-docs
```

**YAML Format**:
```
http://localhost:8080/v3/api-docs.yaml
```

**Static File**:
```
design/openapi.yaml
```

## Actuator Endpoints

Spring Boot Actuator provides production-ready features for monitoring and management.

### Available Endpoints

#### Health Check
```
GET /actuator/health
```

Returns detailed health information:
- Application status
- Database connectivity
- Disk space
- Liveness and readiness probes

**Example Response**:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500107862016,
        "free": 250053931008,
        "threshold": 10485760,
        "exists": true
      }
    },
    "livenessState": {
      "status": "UP"
    },
    "readinessState": {
      "status": "UP"
    }
  }
}
```

#### Kubernetes Probes

**Liveness Probe**:
```
GET /actuator/health/liveness
```

**Readiness Probe**:
```
GET /actuator/health/readiness
```

#### Application Info
```
GET /actuator/info
```

Returns application metadata:
- Java version
- OS information
- Build details
- Environment variables (if configured)

**Example Response**:
```json
{
  "java": {
    "version": "17.0.9",
    "vendor": {
      "name": "Eclipse Adoptium"
    },
    "runtime": {
      "name": "OpenJDK Runtime Environment",
      "version": "17.0.9+9"
    },
    "jvm": {
      "name": "OpenJDK 64-Bit Server VM",
      "version": "17.0.9+9"
    }
  },
  "os": {
    "name": "Windows 11",
    "version": "10.0",
    "arch": "amd64"
  }
}
```

#### Metrics
```
GET /actuator/metrics
```

Lists all available metrics.

**Query specific metric**:
```
GET /actuator/metrics/{metricName}
```

Examples:
- `/actuator/metrics/jvm.memory.used`
- `/actuator/metrics/http.server.requests`
- `/actuator/metrics/system.cpu.usage`

**Example Response**:
```json
{
  "names": [
    "jvm.memory.used",
    "jvm.gc.pause",
    "http.server.requests",
    "system.cpu.usage",
    "process.uptime",
    "hikaricp.connections.active"
  ]
}
```

#### Prometheus Metrics
```
GET /actuator/prometheus
```

Returns metrics in Prometheus format for scraping.

## API Endpoints

### Authentication

#### POST /api/auth/login

**Description**: Authenticate and receive JWT token

**Request**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Response** (200 OK):
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**PowerShell Example**:
```powershell
$response = Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/auth/login `
  -Headers @{ "Content-Type" = "application/json" } `
  -Body '{"username":"admin","password":"admin"}'
$token = $response.token
```

### Events

#### POST /api/events

**Description**: Create a new event

**Authentication**: Required (JWT)

**Request**:
```json
{
  "source": "user-service",
  "type": "LOGIN",
  "message": "User successfully logged in",
  "timestamp": "2025-10-18T12:00:00Z"
}
```

**Response** (201 Created):
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "source": "user-service",
  "type": "LOGIN",
  "message": "User successfully logged in",
  "timestamp": "2025-10-18T12:00:00Z"
}
```

**PowerShell Example**:
```powershell
Invoke-RestMethod -Method POST `
  -Uri http://localhost:8080/api/events `
  -Headers @{ 
    "Authorization" = "Bearer $token"
    "Content-Type" = "application/json" 
  } `
  -Body '{"source":"user-service","type":"LOGIN","message":"User logged in","timestamp":"2025-10-18T12:00:00Z"}'
```

#### GET /api/events

**Description**: Retrieve events with optional filtering

**Authentication**: Required (JWT)

**Query Parameters**:
- `type` (optional): Filter by event type
- `source` (optional): Filter by event source
- `from` (optional): Start of time range (ISO 8601)
- `to` (optional): End of time range (ISO 8601)

**Response** (200 OK):
```json
[
  {
    "id": "123e4567-e89b-12d3-a456-426614174000",
    "source": "user-service",
    "type": "LOGIN",
    "message": "User successfully logged in",
    "timestamp": "2025-10-18T12:00:00Z"
  }
]
```

**PowerShell Examples**:
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

# Combined filters
Invoke-RestMethod -Uri "http://localhost:8080/api/events?type=LOGIN&source=user-service&from=2025-01-01T00:00:00Z" `
  -Headers @{ "Authorization" = "Bearer $token" }
```

### Metrics

#### GET /api/metrics

**Description**: Get aggregated event metrics

**Authentication**: Required (JWT)

**Response** (200 OK):
```json
{
  "totalCount": 150,
  "countByType": {
    "LOGIN": 50,
    "LOGOUT": 30,
    "ERROR": 20,
    "INFO": 50
  },
  "countBySource": {
    "user-service": 80,
    "payment-service": 40,
    "notification-service": 30
  }
}
```

**PowerShell Example**:
```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/metrics `
  -Headers @{ "Authorization" = "Bearer $token" }
```

### Health

#### GET /api/health

**Description**: Simple health check

**Authentication**: Public (no authentication required)

**Response** (200 OK):
```json
{
  "status": "OK"
}
```

**PowerShell Example**:
```powershell
curl http://localhost:8080/api/health
```

## Data Models

### Event

Represents an event in the system.

**Properties**:
- `id` (UUID): Unique identifier (auto-generated)
- `source` (string, required): Source system
- `type` (string, required): Event type
- `message` (string, required): Event message
- `timestamp` (datetime, required): ISO 8601 timestamp

**Example**:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "source": "user-service",
  "type": "LOGIN",
  "message": "User john.doe logged in successfully",
  "timestamp": "2025-10-18T12:00:00Z"
}
```

### Metrics

Aggregated event statistics.

**Properties**:
- `totalCount` (number): Total events
- `countByType` (object): Counts grouped by event type
- `countBySource` (object): Counts grouped by event source

**Example**:
```json
{
  "totalCount": 150,
  "countByType": {
    "LOGIN": 50,
    "LOGOUT": 30
  },
  "countBySource": {
    "user-service": 80,
    "payment-service": 40
  }
}
```

## Error Responses

All error responses follow a consistent format:

### Validation Error (400)
```json
{
  "error": "validation_error",
  "message": "Source is required"
}
```

### Unauthorized (401)
```json
{
  "error": "unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### Invalid Request (400)
```json
{
  "error": "invalid_request",
  "message": "from must be before or equal to to"
}
```

## Authentication Flow

### Step-by-Step Guide

1. **Login to get JWT token**:
   ```powershell
   POST /api/auth/login
   Body: {"username":"admin","password":"admin123"}
   ```

2. **Store the token**:
   ```powershell
   $token = $response.token
   ```

3. **Use token in subsequent requests**:
   ```
   Authorization: Bearer <token>
   ```

4. **Token expires after 1 hour** (default)
   - Request a new token when expired
   - Configure expiry with `JWT_VALIDITY_MS` environment variable

### Token Format

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY5NzY0MDAwMCwiZXhwIjoxNjk3NjQzNjAwfQ.signature
```

## WebSocket Support

### Connection

Connect to STOMP endpoint:
```
ws://localhost:8080/ws
```

### Subscribe to Events

Subscribe to event topic:
```
/topic/events
```

When a new event is created via POST `/api/events`, it's automatically broadcast to all WebSocket subscribers.

### JavaScript Example

See `src/main/resources/static/ws-client.html` for a complete working example.

## Rate Limiting

Currently, no rate limiting is implemented. For production:
- Consider using Spring Cloud Gateway
- Or implement custom rate limiting with Redis
- Or use API Gateway (Kong, AWS API Gateway, etc.)

## Versioning

Current API version: **v1.0.0**

Future versions may use URL versioning:
- `/api/v1/events`
- `/api/v2/events`

## Testing the API

### Using Swagger UI

1. Navigate to http://localhost:8080/swagger-ui.html
2. Click "Authorize" button
3. Login via `/api/auth/login` to get token
4. Enter token in authorization dialog
5. Test all endpoints interactively

### Using PowerShell

See README.md for complete PowerShell examples.

### Using cURL

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}'

# Create event
curl -X POST http://localhost:8080/api/events \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"source":"test","type":"INFO","message":"Test","timestamp":"2025-10-18T12:00:00Z"}'

# Get events
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/events

# Get metrics
curl -H "Authorization: Bearer <token>" \
  http://localhost:8080/api/metrics
```

### Using Postman

1. Import the OpenAPI spec from http://localhost:8080/v3/api-docs
2. Configure Bearer Token authentication
3. Test endpoints

## Monitoring and Observability

### Health Checks

**Basic Health**:
```
GET /api/health
```

**Detailed Health** (with database, disk space, etc.):
```
GET /actuator/health
```

**Kubernetes Probes**:
```
GET /actuator/health/liveness
GET /actuator/health/readiness
```

### Metrics

**List all metrics**:
```
GET /actuator/metrics
```

**Query specific metric**:
```
GET /actuator/metrics/jvm.memory.used
GET /actuator/metrics/http.server.requests
GET /actuator/metrics/hikaricp.connections.active
```

**Prometheus endpoint** (for monitoring):
```
GET /actuator/prometheus
```

### Application Info

```
GET /actuator/info
```

Returns:
- Java version and vendor
- OS information
- Custom application properties

## Security

### Public Endpoints (No Authentication)

- `GET /api/health`
- `POST /api/auth/login`
- `GET /actuator/**`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`
- `GET /h2-console/**` (if H2 enabled)
- `GET /ws` (WebSocket endpoint)

### Protected Endpoints (JWT Required)

- `POST /api/events`
- `GET /api/events`
- `GET /api/metrics`

### Authentication Method

**Header**:
```
Authorization: Bearer <jwt-token>
```

**Token Expiry**: 1 hour (default)

## Best Practices

### API Usage

1. **Always validate input**: All endpoints validate request bodies
2. **Use ISO 8601 for timestamps**: `2025-10-18T12:00:00Z`
3. **Handle errors gracefully**: Check response codes
4. **Reuse JWT tokens**: Don't request new token for each request
5. **Use filtering**: Reduce data transfer with query parameters

### Performance

1. **Limit result sets**: Use time range filters
2. **Cache metrics**: Metrics are expensive to compute
3. **Use WebSocket**: For real-time updates instead of polling
4. **Index database**: Add indexes on frequently queried fields

### Security

1. **Rotate JWT secrets**: Change `JWT_SECRET` regularly
2. **Use HTTPS**: Always use TLS in production
3. **Validate tokens**: Tokens are validated on each request
4. **Short token expiry**: Default 1 hour is reasonable
5. **Secure storage**: Never log or expose tokens

## Integration Examples

### Python

```python
import requests
import json

# Login
response = requests.post(
    'http://localhost:8080/api/auth/login',
    json={'username': 'admin', 'password': 'admin123'}
)
token = response.json()['token']

# Create event
headers = {
    'Authorization': f'Bearer {token}',
    'Content-Type': 'application/json'
}
event = {
    'source': 'python-client',
    'type': 'INFO',
    'message': 'Event from Python',
    'timestamp': '2025-10-18T12:00:00Z'
}
response = requests.post(
    'http://localhost:8080/api/events',
    headers=headers,
    json=event
)
print(response.json())
```

### JavaScript/Node.js

```javascript
const axios = require('axios');

async function main() {
  // Login
  const loginResponse = await axios.post(
    'http://localhost:8080/api/auth/login',
    { username: 'admin', password: 'admin123' }
  );
  const token = loginResponse.data.token;

  // Create event
  const event = {
    source: 'nodejs-client',
    type: 'INFO',
    message: 'Event from Node.js',
    timestamp: new Date().toISOString()
  };
  
  const response = await axios.post(
    'http://localhost:8080/api/events',
    event,
    { headers: { 'Authorization': `Bearer ${token}` } }
  );
  
  console.log(response.data);
}

main();
```

### Java

```java
// Using RestTemplate
RestTemplate restTemplate = new RestTemplate();

// Login
LoginRequest loginRequest = new LoginRequest("admin", "admin123");
LoginResponse loginResponse = restTemplate.postForObject(
    "http://localhost:8080/api/auth/login",
    loginRequest,
    LoginResponse.class
);
String token = loginResponse.getToken();

// Create event
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Bearer " + token);
headers.setContentType(MediaType.APPLICATION_JSON);

EventRequest event = new EventRequest();
event.setSource("java-client");
event.setType("INFO");
event.setMessage("Event from Java");
event.setTimestamp(Instant.now());

HttpEntity<EventRequest> request = new HttpEntity<>(event, headers);
EventResponse response = restTemplate.postForEntity(
    "http://localhost:8080/api/events",
    request,
    EventResponse.class
).getBody();
```

## Troubleshooting

### Swagger UI Not Loading

1. Check if application is running: http://localhost:8080/api/health
2. Clear browser cache
3. Check security configuration allows `/swagger-ui/**`

### API Docs Not Available

1. Verify springdoc dependency is included
2. Check application logs for errors
3. Ensure `/v3/api-docs` is not blocked by security

### Actuator Endpoints Return 404

1. Verify `spring-boot-starter-actuator` dependency
2. Check `management.endpoints.web.exposure.include` in application.yaml
3. Ensure endpoints are not blocked by security

### Metrics Not Updating

1. Metrics are computed in real-time from database
2. Check database connectivity
3. Verify events exist in database

## Additional Resources

- [Spring Boot Actuator Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [SpringDoc OpenAPI Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI Guide](https://swagger.io/tools/swagger-ui/)

