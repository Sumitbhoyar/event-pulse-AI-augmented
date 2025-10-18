# Phase 2: Domain Model and Repository

## Objective
Create the Event JPA entity and repository with proper annotations.

## Requirements

### Event Entity Fields
- `UUID id` (primary key, auto-generated)
- `String source`
- `String type`
- `String message`
- `Instant timestamp`

### Annotations Required
- JPA annotations (@Entity, @Table, @Id, @Column)
- Validation annotations (@NotBlank, @NotNull)
- Builder pattern support

### Repository
- Interface extending `JpaRepository<Event, UUID>`
- Support for JpaSpecificationExecutor for dynamic filtering

## Implementation Steps

1. Created `Event.java` entity with:
   - UUID primary key with auto-generation
   - All required fields with JPA annotations
   - Validation constraints
   - Manual builder pattern implementation
   - Getter/setter methods

2. Created `EventRepository.java`:
   - Extended JpaRepository for CRUD operations
   - Extended JpaSpecificationExecutor for dynamic queries
   - Added JPQL queries for metrics:
     - `countAllEvents()`
     - `countByType()`
     - `countBySource()`

## Files Created

- `src/main/java/com/eventpulse/entity/Event.java`
- `src/main/java/com/eventpulse/repository/EventRepository.java`

## Technical Details

### Entity Configuration
```java
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    @NotBlank
    private String source;
    
    // ... other fields
}
```

### Repository JPQL Queries
```java
@Query("select count(e) from Event e")
long countAllEvents();

@Query("select e.type as key, count(e) as value from Event e group by e.type")
List<Object[]> countByType();
```

## Challenges Encountered

### Lombok vs Manual Implementation
**Issue**: Lombok annotation processing failed during compilation.

**Solution**: Implemented manual:
- Builder pattern (static inner class)
- Getter/setter methods
- Constructors (no-arg and all-args)

### UUID Generation Strategy
**Decision**: Used `GenerationType.UUID` for automatic UUID generation by Hibernate.

## Outcome

✅ Event entity properly configured with JPA  
✅ Validation annotations in place  
✅ Repository with custom JPQL queries  
✅ Builder pattern for easy object creation  
✅ Support for dynamic filtering via Specifications

