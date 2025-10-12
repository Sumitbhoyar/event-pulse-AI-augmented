package com.eventpulse.controller;

import com.eventpulse.dto.EventRequest;
import com.eventpulse.dto.EventResponse;
import com.eventpulse.entity.Event;
import com.eventpulse.security.JwtUtils;
import com.eventpulse.service.EventService;
import com.eventpulse.service.WebSocketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@DisplayName("EventController Integration Tests")
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private WebSocketService webSocketService;

    @MockBean
    private JwtUtils jwtUtils;

    private Event testEvent;
    private EventRequest testEventRequest;
    private String validJwtToken;

    @BeforeEach
    void setUp() {
        UUID eventId = UUID.randomUUID();
        testEvent = Event.builder()
                .id(eventId)
                .source("test-service")
                .type("test")
                .message("Test message")
                .timestamp(Instant.now())
                .build();

        testEventRequest = EventRequest.builder()
                .source("test-service")
                .type("test")
                .message("Test message")
                .build();

        validJwtToken = "valid.jwt.token";
    }

    @Test
    @DisplayName("Should create event successfully")
    void shouldCreateEventSuccessfully() throws Exception {
        // Given
        when(eventService.saveEvent(any(Event.class))).thenReturn(testEvent);
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testEvent.getId().toString()))
                .andExpect(jsonPath("$.source").value("test-service"))
                .andExpect(jsonPath("$.type").value("test"))
                .andExpect(jsonPath("$.message").value("Test message"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(eventService, times(1)).saveEvent(any(Event.class));
        verify(webSocketService, times(1)).broadcastNewEvent(any(Event.class));
    }

    @Test
    @DisplayName("Should return 401 when no authorization header provided")
    void shouldReturn401WhenNoAuthorizationHeaderProvided() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventRequest)))
                .andExpect(status().isUnauthorized());

        verify(eventService, never()).saveEvent(any());
        verify(webSocketService, never()).broadcastNewEvent(any());
    }

    @Test
    @DisplayName("Should return 401 when invalid JWT token provided")
    void shouldReturn401WhenInvalidJwtTokenProvided() throws Exception {
        // Given
        String invalidToken = "invalid.jwt.token";
        when(jwtUtils.validateJwtToken(invalidToken)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + invalidToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventRequest)))
                .andExpect(status().isUnauthorized());

        verify(eventService, never()).saveEvent(any());
        verify(webSocketService, never()).broadcastNewEvent(any());
    }

    @Test
    @DisplayName("Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        // Given
        EventRequest invalidRequest = EventRequest.builder()
                .source("") // Invalid: empty source
                .type("test")
                .message("Test message")
                .build();

        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.source").exists());

        verify(eventService, never()).saveEvent(any());
        verify(webSocketService, never()).broadcastNewEvent(any());
    }

    @Test
    @DisplayName("Should get events with filters successfully")
    void shouldGetEventsWithFiltersSuccessfully() throws Exception {
        // Given
        List<Event> events = Arrays.asList(testEvent);
        when(eventService.getEventsWithFilters("test", "test-service", null, null)).thenReturn(events);
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .param("type", "test")
                        .param("source", "test-service"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testEvent.getId().toString()))
                .andExpect(jsonPath("$[0].source").value("test-service"))
                .andExpect(jsonPath("$[0].type").value("test"));

        verify(eventService, times(1)).getEventsWithFilters("test", "test-service", null, null);
    }

    @Test
    @DisplayName("Should get events with time range filters successfully")
    void shouldGetEventsWithTimeRangeFiltersSuccessfully() throws Exception {
        // Given
        List<Event> events = Arrays.asList(testEvent);
        Instant startTime = Instant.now().minusSeconds(3600);
        Instant endTime = Instant.now();
        
        when(eventService.getEventsWithFilters(null, null, startTime, endTime)).thenReturn(events);
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .param("startTime", startTime.toString())
                        .param("endTime", endTime.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testEvent.getId().toString()));

        verify(eventService, times(1)).getEventsWithFilters(null, null, startTime, endTime);
    }

    @Test
    @DisplayName("Should return 400 when invalid time range provided")
    void shouldReturn400WhenInvalidTimeRangeProvided() throws Exception {
        // Given
        Instant startTime = Instant.now();
        Instant endTime = Instant.now().minusSeconds(3600); // End before start

        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .param("startTime", startTime.toString())
                        .param("endTime", endTime.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verify(eventService, never()).getEventsWithFilters(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should get event by ID successfully")
    void shouldGetEventByIdSuccessfully() throws Exception {
        // Given
        when(eventService.getEventById(testEvent.getId())).thenReturn(testEvent);
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(get("/api/events/{id}", testEvent.getId())
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(testEvent.getId().toString()))
                .andExpect(jsonPath("$.source").value("test-service"))
                .andExpect(jsonPath("$.type").value("test"))
                .andExpect(jsonPath("$.message").value("Test message"));

        verify(eventService, times(1)).getEventById(testEvent.getId());
    }

    @Test
    @DisplayName("Should return 404 when event not found")
    void shouldReturn404WhenEventNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(eventService.getEventById(nonExistentId))
                .thenThrow(new EventService.EventNotFoundException("Event not found with ID: " + nonExistentId));
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(get("/api/events/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(eventService, times(1)).getEventById(nonExistentId);
    }

    @Test
    @DisplayName("Should return 400 when invalid UUID format provided")
    void shouldReturn400WhenInvalidUuidFormatProvided() throws Exception {
        // Given
        String invalidUuid = "invalid-uuid";
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(get("/api/events/{id}", invalidUuid)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        verify(eventService, never()).getEventById(any());
    }

    @Test
    @DisplayName("Should delete event successfully")
    void shouldDeleteEventSuccessfully() throws Exception {
        // Given
        doNothing().when(eventService).deleteEvent(testEvent.getId());
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(delete("/api/events/{id}", testEvent.getId())
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNoContent());

        verify(eventService, times(1)).deleteEvent(testEvent.getId());
        verify(webSocketService, times(1)).broadcastEventDeleted(testEvent.getId());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent event")
    void shouldReturn404WhenDeletingNonExistentEvent() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        doThrow(new EventService.EventNotFoundException("Event not found with ID: " + nonExistentId))
                .when(eventService).deleteEvent(nonExistentId);
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(delete("/api/events/{id}", nonExistentId)
                        .header("Authorization", "Bearer " + validJwtToken))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));

        verify(eventService, times(1)).deleteEvent(nonExistentId);
        verify(webSocketService, never()).broadcastEventDeleted(any());
    }

    @Test
    @DisplayName("Should return 500 when service throws unexpected exception")
    void shouldReturn500WhenServiceThrowsUnexpectedException() throws Exception {
        // Given
        when(eventService.saveEvent(any(Event.class)))
                .thenThrow(new RuntimeException("Unexpected database error"));
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEventRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"));

        verify(eventService, times(1)).saveEvent(any(Event.class));
        verify(webSocketService, never()).broadcastNewEvent(any());
    }

    @Test
    @DisplayName("Should handle malformed JSON gracefully")
    void shouldHandleMalformedJsonGracefully() throws Exception {
        // Given
        String malformedJson = "{ \"source\": \"test\", \"type\": }"; // Missing value
        when(jwtUtils.validateJwtToken(validJwtToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validJwtToken)).thenReturn("testuser");

        // When & Then
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + validJwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).saveEvent(any());
        verify(webSocketService, never()).broadcastNewEvent(any());
    }
}
