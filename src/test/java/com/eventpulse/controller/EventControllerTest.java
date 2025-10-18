package com.eventpulse.controller;

import com.eventpulse.dto.EventRequest;
import com.eventpulse.dto.EventResponse;
import com.eventpulse.entity.Event;
import com.eventpulse.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@Import(TestSecurityConfig.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private SimpMessagingTemplate messagingTemplate;

    private Event testEvent;
    private EventRequest testRequest;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(UUID.randomUUID());
        testEvent.setSource("test-service");
        testEvent.setType("INFO");
        testEvent.setMessage("Test message");
        testEvent.setTimestamp(Instant.parse("2025-01-17T21:30:00Z"));

        testRequest = new EventRequest();
        testRequest.setSource("test-service");
        testRequest.setType("INFO");
        testRequest.setMessage("Test message");
        testRequest.setTimestamp(Instant.parse("2025-01-17T21:30:00Z"));
    }

    @Test
    @WithMockUser
    void createEvent_ValidRequest_ShouldReturn201() throws Exception {
        // Arrange
        when(eventService.save(any(Event.class))).thenReturn(testEvent);

        // Act & Assert
        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testEvent.getId().toString()))
                .andExpect(jsonPath("$.source").value("test-service"))
                .andExpect(jsonPath("$.type").value("INFO"))
                .andExpect(jsonPath("$.message").value("Test message"));

        verify(eventService, times(1)).save(any(Event.class));
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/events"), any(EventResponse.class));
    }

    @Test
    @WithMockUser
    void createEvent_InvalidRequest_ShouldReturn400() throws Exception {
        // Arrange
        EventRequest invalidRequest = new EventRequest();
        // Missing required fields

        // Act & Assert
        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(eventService, never()).save(any());
    }

    @Test
    void createEvent_Unauthorized_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/events")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testRequest)))
                .andExpect(status().isUnauthorized());

        verify(eventService, never()).save(any());
    }

    @Test
    @WithMockUser
    void getEvents_NoFilters_ShouldReturnAllEvents() throws Exception {
        // Arrange
        when(eventService.findByFilters(null, null, null, null))
                .thenReturn(Arrays.asList(testEvent));

        // Act & Assert
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(testEvent.getId().toString()))
                .andExpect(jsonPath("$[0].source").value("test-service"))
                .andExpect(jsonPath("$[0].type").value("INFO"));

        verify(eventService, times(1)).findByFilters(null, null, null, null);
    }

    @Test
    @WithMockUser
    void getEvents_WithTypeFilter_ShouldReturnFilteredEvents() throws Exception {
        // Arrange
        when(eventService.findByFilters(eq("INFO"), isNull(), isNull(), isNull()))
                .thenReturn(Arrays.asList(testEvent));

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("type", "INFO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").value("INFO"));

        verify(eventService, times(1)).findByFilters(eq("INFO"), isNull(), isNull(), isNull());
    }

    @Test
    @WithMockUser
    void getEvents_WithSourceFilter_ShouldReturnFilteredEvents() throws Exception {
        // Arrange
        when(eventService.findByFilters(isNull(), eq("test-service"), isNull(), isNull()))
                .thenReturn(Arrays.asList(testEvent));

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("source", "test-service"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].source").value("test-service"));

        verify(eventService, times(1)).findByFilters(isNull(), eq("test-service"), isNull(), isNull());
    }

    @Test
    @WithMockUser
    void getEvents_WithTimeRange_ShouldReturnFilteredEvents() throws Exception {
        // Arrange
        String from = "2025-01-01T00:00:00Z";
        String to = "2025-12-31T23:59:59Z";
        when(eventService.findByFilters(isNull(), isNull(), any(Instant.class), any(Instant.class)))
                .thenReturn(Arrays.asList(testEvent));

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists());

        verify(eventService, times(1)).findByFilters(isNull(), isNull(), any(Instant.class), any(Instant.class));
    }

    @Test
    @WithMockUser
    void getEvents_AllFilters_ShouldReturnFilteredEvents() throws Exception {
        // Arrange
        String from = "2025-01-01T00:00:00Z";
        String to = "2025-12-31T23:59:59Z";
        when(eventService.findByFilters(eq("INFO"), eq("test-service"), any(Instant.class), any(Instant.class)))
                .thenReturn(Arrays.asList(testEvent));

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .param("type", "INFO")
                        .param("source", "test-service")
                        .param("from", from)
                        .param("to", to))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].type").value("INFO"))
                .andExpect(jsonPath("$[0].source").value("test-service"));

        verify(eventService, times(1)).findByFilters(eq("INFO"), eq("test-service"), any(Instant.class), any(Instant.class));
    }

    @Test
    @WithMockUser
    void getEvents_NoResults_ShouldReturnEmptyArray() throws Exception {
        // Arrange
        when(eventService.findByFilters(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getEvents_Unauthorized_ShouldReturn401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());

        verify(eventService, never()).findByFilters(any(), any(), any(), any());
    }
}

