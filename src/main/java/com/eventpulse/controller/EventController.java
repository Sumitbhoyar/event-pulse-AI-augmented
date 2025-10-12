package com.eventpulse.controller;

import com.eventpulse.dto.EventRequest;
import com.eventpulse.dto.EventResponse;
import com.eventpulse.entity.Event;
import com.eventpulse.service.EventService;
import com.eventpulse.service.WebSocketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow CORS for development
@Tag(name = "Events", description = "Event management endpoints for creating, retrieving, and managing events")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    private final EventService eventService;
    private final WebSocketService webSocketService;

    /**
     * Create a new event
     * POST /api/events
     */
    @Operation(
            summary = "Create Event",
            description = "Create a new event with source, type, and message. The timestamp is automatically set to current time."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Event created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EventResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.eventpulse.exception.GlobalExceptionHandler.ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - JWT token required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.eventpulse.exception.GlobalExceptionHandler.ErrorResponse.class)
                    )
            )
    })
    @PostMapping
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        log.info("Creating new event: type={}, source={}", eventRequest.getType(), eventRequest.getSource());
        
        try {
            // Convert DTO to entity
            Event event = Event.builder()
                    .source(eventRequest.getSource())
                    .type(eventRequest.getType())
                    .message(eventRequest.getMessage())
                    .timestamp(Instant.now()) // Set current timestamp
                    .build();
            
            // Save event
            Event savedEvent = eventService.saveEvent(event);
            
            // Convert to response DTO
            EventResponse response = EventResponse.fromEntity(savedEvent);
            
            // Broadcast the new event to WebSocket clients
            webSocketService.broadcastNewEvent(savedEvent);
            
            log.info("Event created successfully with ID: {}", savedEvent.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (EventService.EventServiceException e) {
            log.error("Failed to create event: {}", e.getMessage());
            throw new EventCreationException("Failed to create event: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error creating event: {}", e.getMessage(), e);
            throw new EventCreationException("An unexpected error occurred while creating the event", e);
        }
    }

    /**
     * Get events with optional filtering
     * GET /api/events?type=login&source=user-service&startTime=2023-01-01T00:00:00Z&endTime=2023-12-31T23:59:59Z
     */
    @GetMapping
    public ResponseEntity<List<EventResponse>> getEvents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        
        log.debug("Retrieving events with filters: type={}, source={}, startTime={}, endTime={}", 
                type, source, startTime, endTime);
        
        try {
            List<Event> events = eventService.getEventsWithFilters(type, source, startTime, endTime);
            
            List<EventResponse> responses = events.stream()
                    .map(EventResponse::fromEntity)
                    .collect(Collectors.toList());
            
            log.debug("Retrieved {} events", responses.size());
            return ResponseEntity.ok(responses);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid filter parameters: {}", e.getMessage());
            throw new InvalidFilterException(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error retrieving events: {}", e.getMessage(), e);
            throw new EventRetrievalException("An unexpected error occurred while retrieving events", e);
        }
    }

    /**
     * Get a specific event by ID
     * GET /api/events/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EventResponse> getEventById(@PathVariable String id) {
        log.debug("Retrieving event by ID: {}", id);
        
        try {
            java.util.UUID uuid = java.util.UUID.fromString(id);
            Event event = eventService.getEventById(uuid);
            EventResponse response = EventResponse.fromEntity(event);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", id);
            throw new InvalidUuidException("Invalid event ID format: " + id);
        } catch (EventService.EventNotFoundException e) {
            log.warn("Event not found: {}", id);
            throw new EventNotFoundException("Event not found with ID: " + id);
        } catch (Exception e) {
            log.error("Unexpected error retrieving event: {}", e.getMessage(), e);
            throw new EventRetrievalException("An unexpected error occurred while retrieving the event", e);
        }
    }

    /**
     * Delete an event by ID
     * DELETE /api/events/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id) {
        log.info("Deleting event by ID: {}", id);
        
        try {
            java.util.UUID uuid = java.util.UUID.fromString(id);
            eventService.deleteEvent(uuid);
            
            // Broadcast the event deletion to WebSocket clients
            webSocketService.broadcastEventDeleted(uuid);
            
            log.info("Event deleted successfully: {}", id);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format: {}", id);
            throw new InvalidUuidException("Invalid event ID format: " + id);
        } catch (EventService.EventNotFoundException e) {
            log.warn("Event not found for deletion: {}", id);
            throw new EventNotFoundException("Event not found with ID: " + id);
        } catch (Exception e) {
            log.error("Unexpected error deleting event: {}", e.getMessage(), e);
            throw new EventDeletionException("An unexpected error occurred while deleting the event", e);
        }
    }

    // Custom exceptions for better error handling
    public static class EventCreationException extends RuntimeException {
        public EventCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class EventRetrievalException extends RuntimeException {
        public EventRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class EventDeletionException extends RuntimeException {
        public EventDeletionException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class EventNotFoundException extends RuntimeException {
        public EventNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidFilterException extends RuntimeException {
        public InvalidFilterException(String message) {
            super(message);
        }
    }

    public static class InvalidUuidException extends RuntimeException {
        public InvalidUuidException(String message) {
            super(message);
        }
    }
}
