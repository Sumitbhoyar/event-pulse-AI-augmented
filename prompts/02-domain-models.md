# Prompt 2 — Define Domain Models

> Add a new JPA entity class `Event` with fields:
> - UUID id (primary key)
> - String source
> - String type
> - String message
> - Instant timestamp
>
> Add JPA annotations and use `@Builder` and `@Data` from Lombok.  
> Create a `EventRepository` interface extending `JpaRepository<Event, UUID>`.

## Implementation Summary

This prompt established the core domain model for the EventPulse application by:

### Files Created:
- `src/main/java/com/eventpulse/entity/Event.java` - JPA entity with all required fields
- `src/main/java/com/eventpulse/repository/EventRepository.java` - Spring Data JPA repository interface

### Key Features Implemented:
- **Event Entity** with proper JPA annotations:
  - `@Entity` and `@Table(name = "events")`
  - `@Id` with `@GeneratedValue(strategy = GenerationType.UUID)`
  - `@Column` annotations with appropriate constraints
  - Lombok annotations: `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`

- **EventRepository Interface**:
  - Extends `JpaRepository<Event, UUID>`
  - `@Repository` annotation for Spring component scanning
  - Ready for custom query methods

### Entity Fields:
- `UUID id` - Primary key with automatic UUID generation
- `String source` - Event source identifier
- `String type` - Event type classification
- `String message` - Event message content (TEXT column type)
- `Instant timestamp` - Event timestamp

### Database Features:
- Automatic table creation via Hibernate DDL
- UUID primary keys for distributed systems compatibility
- Proper column constraints and indexing
- TEXT column type for longer messages

This domain model provided the foundation for all event-related operations in the application.
