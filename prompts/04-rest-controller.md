# Prompt 4 — REST Controller for Event Ingestion

> Create a REST controller `/api/events` with:
> - POST `/api/events` → accepts event JSON, validates it, saves using `EventService`
> - GET `/api/events` → supports query params for `type`, `source`, and time range
>
> Return proper HTTP status codes and validation errors.

## Implementation Summary

This prompt implemented the REST API layer for event ingestion by:

### Files Created:
- `src/main/java/com/eventpulse/controller/EventController.java` - REST API endpoints
- `src/main/java/com/eventpulse/dto/EventRequest.java` - Request DTO with validation
- `src/main/java/com/eventpulse/dto/EventResponse.java` - Response DTO
- `src/main/java/com/eventpulse/exception/GlobalExceptionHandler.java` - Global exception handling

### Key Features Implemented:

#### REST Endpoints:
- `POST /api/events` - Create new events with validation
- `GET /api/events` - Retrieve events with filtering support
- `GET /api/events/{id}` - Get specific event by ID
- `DELETE /api/events/{id}` - Delete event by ID

#### Request/Response DTOs:
- **EventRequest** - Input validation with Bean Validation annotations
- **EventResponse** - Clean response structure with static factory method
- **ErrorResponse** - Structured error responses with timestamps and details

#### Query Parameter Support:
- `type` - Filter by event type
- `source` - Filter by event source  
- `startTime` - Start of time range (ISO-8601 format)
- `endTime` - End of time range (ISO-8601 format)

#### HTTP Status Codes:
- `201 Created` - Successful event creation
- `200 OK` - Successful retrieval
- `204 No Content` - Successful deletion
- `400 Bad Request` - Validation errors, invalid parameters
- `404 Not Found` - Event not found
- `500 Internal Server Error` - Unexpected errors

#### Validation Features:
- Bean Validation annotations (`@NotBlank`, `@Size`)
- Field length constraints for database optimization
- Custom validation logic for business rules
- Detailed validation error messages with field-specific details

#### Exception Handling:
- `MethodArgumentNotValidException` - Validation errors with field details
- `ConstraintViolationException` - Constraint violations
- `EventNotFoundException` - Event not found scenarios
- `InvalidUuidException` - Invalid UUID format
- `InvalidFilterException` - Invalid filter parameters
- `MethodArgumentTypeMismatchException` - Type conversion errors
- Generic exception handling for unexpected errors

#### Advanced Features:
- **CORS Support** - Enabled for development
- **Comprehensive Logging** - Request/response logging for debugging
- **Type Safety** - Proper UUID handling and date/time parsing
- **Error Response Structure** - Consistent error format with timestamps
- **Flexible Filtering** - Support for any combination of type, source, and time range filters

### API Usage Examples:
```bash
# Create event
POST /api/events
{
    "source": "user-service",
    "type": "login", 
    "message": "User logged in successfully"
}

# Get events with filters
GET /api/events?type=login&source=user-service&startTime=2023-12-01T00:00:00Z&endTime=2023-12-01T23:59:59Z
```

This REST controller provided a complete API for event ingestion with robust validation, error handling, and flexible querying capabilities.
