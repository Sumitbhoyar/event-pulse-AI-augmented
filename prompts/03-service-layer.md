# Phase 3: Service Layer Implementation

## Objective
Create service classes for event management and metrics aggregation with proper validation and exception handling.

## Requirements

### EventService
- Save events via repository
- Retrieve events filtered by:
  - Type
  - Source
  - Timestamp range (from/to)
- Proper exception handling
- Input validation

### MetricsService
- Calculate total event count
- Calculate count by event type
- Calculate count by event source
- Use JPQL queries from repository

## Implementation Steps

### 1. EventService Implementation

Created `EventService.java` with:

**Dependencies**:
- EventRepository (for data access)
- Validator (for bean validation)

**Methods**:
```java
public Event save(Event event)
```
- Validates event is not null
- Sets default timestamp if not provided
- Validates using Bean Validation
- Saves to repository

```java
public List<Event> findByFilters(String type, String source, Instant from, Instant to)
```
- Validates time range (from must be before to)
- Builds dynamic Specification based on filters
- Returns filtered results

### 2. MetricsService Implementation

Created `MetricsService.java` with:

**Methods**:
```java
public MetricsResponse getMetrics()
```
- Calls repository JPQL queries
- Converts Object[] tuples to Maps
- Handles null values gracefully
- Returns aggregated metrics

**Helper Methods**:
```java
private Map<String, Long> convertTuples(List<Object[]> tuples)
```
- Converts repository results to Map
- Handles null keys and values
- Robust error handling

### 3. DTOs Created

- **MetricsResponse**: Contains totalCount, countByType, countBySource
- **EventRequest**: For creating events (with validation)
- **EventResponse**: For returning events

### 4. Exception Handling

Created:
- `InvalidRequestException.java` - Custom runtime exception
- `GlobalExceptionHandler.java` - @RestControllerAdvice for consistent error responses

## Files Created

- `src/main/java/com/eventpulse/service/EventService.java`
- `src/main/java/com/eventpulse/service/MetricsService.java`
- `src/main/java/com/eventpulse/dto/MetricsResponse.java`
- `src/main/java/com/eventpulse/exception/InvalidRequestException.java`
- `src/main/java/com/eventpulse/exception/GlobalExceptionHandler.java`

## Technical Highlights

### Dynamic Filtering with Specifications

```java
Specification<Event> spec = Specification.where(null);

if (StringUtils.hasText(type)) {
    spec = spec.and((root, query, cb) -> 
        cb.equal(root.get("type"), type));
}

// ... additional filters
return eventRepository.findAll(spec);
```

### Validation Strategy

- Bean Validation annotations on entities
- Programmatic validation in service layer
- ConstraintViolationException for validation errors
- Custom exceptions for business logic errors

### Metrics Conversion

```java
for (Object[] row : tuples) {
    String key = row[0] != null ? row[0].toString() : "";
    Long value = row[1] instanceof Number ? 
        ((Number) row[1]).longValue() : 0L;
    result.put(key, value);
}
```

## Challenges Encountered

### Validator Injection
**Issue**: How to inject Validator bean into service.

**Solution**: Constructor injection with Spring's auto-wiring.

### Time Range Validation
**Issue**: Need to validate that 'from' is before 'to'.

**Solution**: Added explicit validation in service method:
```java
if (from != null && to != null && from.isAfter(to)) {
    throw new IllegalArgumentException("from must be before or equal to to");
}
```

### Null Handling in JPQL Results
**Issue**: JPQL queries can return null values.

**Solution**: Defensive null checking in convertTuples method.

## Outcome

✅ EventService with full CRUD and filtering capabilities  
✅ MetricsService with aggregation logic  
✅ Proper validation and exception handling  
✅ Null-safe implementations  
✅ Clean separation of concerns

