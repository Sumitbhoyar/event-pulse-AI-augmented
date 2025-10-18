package com.eventpulse.controller;

import com.eventpulse.dto.EventRequest;
import com.eventpulse.dto.EventResponse;
import com.eventpulse.entity.Event;
import com.eventpulse.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Event management endpoints for creating and retrieving events")
@SecurityRequirement(name = "bearer-jwt")
public class EventController {

    private final EventService eventService;
    private final SimpMessagingTemplate messagingTemplate;

    public EventController(EventService eventService, SimpMessagingTemplate messagingTemplate) {
        this.eventService = eventService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    @Operation(
            summary = "Create a new event",
            description = "Creates a new event and broadcasts it to WebSocket subscribers at /topic/events"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Event created successfully",
                    content = @Content(schema = @Schema(implementation = EventResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid",
                    content = @Content)
    })
    public ResponseEntity<EventResponse> createEvent(@Valid @RequestBody EventRequest eventRequest) {
        Event event = Event.builder()
                .source(eventRequest.getSource())
                .type(eventRequest.getType())
                .message(eventRequest.getMessage())
                .timestamp(eventRequest.getTimestamp())
                .build();

        Event savedEvent = eventService.save(event);
        EventResponse response = EventResponse.builder()
                .id(savedEvent.getId())
                .source(savedEvent.getSource())
                .type(savedEvent.getType())
                .message(savedEvent.getMessage())
                .timestamp(savedEvent.getTimestamp())
                .build();

        // Broadcast to STOMP subscribers
        messagingTemplate.convertAndSend("/topic/events", response);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
            summary = "Retrieve events",
            description = "Retrieves events with optional filtering by type, source, and time range"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EventResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid",
                    content = @Content)
    })
    public ResponseEntity<List<EventResponse>> getEvents(
            @Parameter(description = "Filter by event type") @RequestParam(required = false) String type,
            @Parameter(description = "Filter by event source") @RequestParam(required = false) String source,
            @Parameter(description = "Filter events from this timestamp (ISO 8601 format)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @Parameter(description = "Filter events until this timestamp (ISO 8601 format)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        List<Event> events = eventService.findByFilters(type, source, from, to);
        
        List<EventResponse> responses = events.stream()
                .map(event -> EventResponse.builder()
                        .id(event.getId())
                        .source(event.getSource())
                        .type(event.getType())
                        .message(event.getMessage())
                        .timestamp(event.getTimestamp())
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}
