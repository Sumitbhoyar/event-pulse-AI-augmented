# Phase 4: REST Controllers

## Objective
Create REST controllers for event management and metrics with proper HTTP status codes and validation.

## Requirements

### EventController - `/api/events`
- `POST /api/events` - Create event, validate input, return 201 Created
- `GET /api/events` - Retrieve events with query parameters:
  - `type` (optional)
  - `source` (optional)
  - `from` (optional, ISO 8601 datetime)
  - `to` (optional, ISO 8601 datetime)

### MetricsController - `/api/metrics`
- `GET /api/metrics` - Return aggregated metrics

## Implementation Steps

### 1. EventController

Created `EventController.java` with:

**POST endpoint**:
```java
@PostMapping
public ResponseEntity<EventResponse> createEvent(
    @Valid @RequestBody EventRequest eventRequest)
```
- Validates request with @Valid
- Converts EventRequest to Event entity
- Saves via EventService
- Converts Event to EventResponse
- Returns 201 Created status

**GET endpoint**:
```java
@GetMapping
public ResponseEntity<List<EventResponse>> getEvents(
    @RequestParam(required = false) String type,
    @RequestParam(required = false) String source,
    @RequestParam(required = false) Instant from,
    @RequestParam(required = false) Instant to)
```
- All parameters optional
- Uses @DateTimeFormat for Instant parsing
- Calls EventService.findByFilters
- Converts List<Event> to List<EventResponse>
- Returns 200 OK

### 2. MetricsController

Created `MetricsController.java`:

```java
@GetMapping
public ResponseEntity<MetricsResponse> getMetrics()
```
- Calls MetricsService.getMetrics
- Returns 200 OK with aggregated data

### 3. DTOs

**EventRequest**:
- source, type, message, timestamp (all required)
- Validation annotations
- Manual builder pattern

**EventResponse**:
- id, source, type, message, timestamp
- Manual builder pattern
- Getters/setters

## Files Created

- `src/main/java/com/eventpulse/controller/EventController.java`
- `src/main/java/com/eventpulse/controller/MetricsController.java`
- `src/main/java/com/eventpulse/dto/EventRequest.java`
- `src/main/java/com/eventpulse/dto/EventResponse.java`

## Technical Details

### HTTP Status Codes

| Operation | Success | Error |
|-----------|---------|-------|
| POST /api/events | 201 Created | 400 Bad Request |
| GET /api/events | 200 OK | 401 Unauthorized |
| GET /api/metrics | 200 OK | 401 Unauthorized |

### Validation

**Automatic validation** via @Valid:
- @NotBlank for strings
- @NotNull for timestamp
- Custom error messages

**Global exception handler** returns:
```json
{
  "error": "validation_error",
  "message": "Source is required"
}
```

### Date/Time Handling

```java
@RequestParam(required = false) 
@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) 
Instant from
```

**Accepts formats**:
- `2025-10-18T12:00:00Z`
- `2025-10-18T12:00:00.000Z`
- `2025-10-18T12:00:00+02:00`

## Challenges Encountered

### Maven Command Issues
**Problem**: PowerShell Maven commands with profiles didn't work.

**Solution**: 
- Changed to using H2 as default configuration
- Simplified to `mvn spring-boot:run`
- No complex profile arguments needed

### DTO Builder Pattern
**Problem**: Without Lombok, needed manual builder implementation.

**Solution**: Created static inner Builder class in each DTO with fluent API.

### Windows PowerShell Compatibility
**Problem**: curl commands weren't PowerShell-compatible.

**Solution**: 
- Provided Invoke-RestMethod examples
- Added curl.exe alternatives
- Fixed quoting and line continuation

## Outcome

✅ REST controllers fully functional  
✅ Proper HTTP status codes  
✅ Request validation working  
✅ Error handling consistent  
✅ Windows PowerShell compatible examples

