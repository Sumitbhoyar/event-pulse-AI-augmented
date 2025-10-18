package com.eventpulse.controller;

import com.eventpulse.dto.EventRequest;
import com.eventpulse.entity.Event;
import com.eventpulse.repository.EventRepository;
import com.eventpulse.security.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        jwtToken = jwtUtils.generateToken("testuser");
    }

    @AfterEach
    void tearDown() {
        eventRepository.deleteAll();
    }

    @Test
    void createEvent_ValidRequest_ShouldPersistToDatabase() throws Exception {
        // Arrange
        EventRequest request = new EventRequest();
        request.setSource("integration-test");
        request.setType("TEST");
        request.setMessage("Integration test message");
        request.setTimestamp(Instant.now());

        // Act
        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.source").value("integration-test"))
                .andExpect(jsonPath("$.type").value("TEST"))
                .andExpect(jsonPath("$.message").value("Integration test message"));

        // Assert
        assertThat(eventRepository.count()).isEqualTo(1);
        Event savedEvent = eventRepository.findAll().get(0);
        assertThat(savedEvent.getSource()).isEqualTo("integration-test");
        assertThat(savedEvent.getType()).isEqualTo("TEST");
    }

    @Test
    void createEvent_WithoutToken_ShouldReturn403() throws Exception {
        // Arrange
        EventRequest request = new EventRequest();
        request.setSource("test");
        request.setType("TEST");
        request.setMessage("Test");
        request.setTimestamp(Instant.now());

        // Act & Assert - Expects 403 because CSRF protection is enabled
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        assertThat(eventRepository.count()).isEqualTo(0);
    }

    @Test
    void getEvents_AfterCreatingMultiple_ShouldReturnAll() throws Exception {
        // Arrange - Create 3 events
        createTestEvent("service1", "INFO");
        createTestEvent("service2", "ERROR");
        createTestEvent("service1", "WARN");

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void getEvents_FilterByType_ShouldReturnMatchingEvents() throws Exception {
        // Arrange
        createTestEvent("service1", "INFO");
        createTestEvent("service2", "ERROR");
        createTestEvent("service3", "INFO");

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("type", "INFO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].type").value("INFO"))
                .andExpect(jsonPath("$[1].type").value("INFO"));
    }

    @Test
    void getEvents_FilterBySource_ShouldReturnMatchingEvents() throws Exception {
        // Arrange
        createTestEvent("service1", "INFO");
        createTestEvent("service2", "ERROR");
        createTestEvent("service1", "WARN");

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("source", "service1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].source").value("service1"))
                .andExpect(jsonPath("$[1].source").value("service1"));
    }

    @Test
    void getEvents_FilterByTimeRange_ShouldReturnMatchingEvents() throws Exception {
        // Arrange
        Instant now = Instant.now();
        Instant past = now.minusSeconds(3600);
        Instant future = now.plusSeconds(3600);

        Event oldEvent = createTestEventWithTimestamp("service1", "INFO", past);
        Event newEvent = createTestEventWithTimestamp("service2", "ERROR", now);

        // Act & Assert - Query for recent events only
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("from", now.minusSeconds(60).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(newEvent.getId().toString()));
    }

    @Test
    void getEvents_CombinedFilters_ShouldReturnMatchingEvents() throws Exception {
        // Arrange
        createTestEvent("service1", "INFO");
        createTestEvent("service1", "ERROR");
        createTestEvent("service2", "INFO");

        // Act & Assert
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .param("type", "INFO")
                        .param("source", "service1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("INFO"))
                .andExpect(jsonPath("$[0].source").value("service1"));
    }

    @Test
    void healthEndpoint_ShouldBePublic() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    void createAndRetrieveEvent_EndToEnd_ShouldWork() throws Exception {
        // Arrange
        EventRequest request = new EventRequest();
        request.setSource("e2e-test");
        request.setType("E2E");
        request.setMessage("End to end test");
        request.setTimestamp(Instant.now());

        // Act - Create
        String response = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Act - Retrieve
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].source").value("e2e-test"))
                .andExpect(jsonPath("$[0].type").value("E2E"));

        // Assert - Database state
        assertThat(eventRepository.count()).isEqualTo(1);
    }

    // Helper methods
    private Event createTestEvent(String source, String type) {
        Event event = new Event();
        event.setSource(source);
        event.setType(type);
        event.setMessage("Test message");
        event.setTimestamp(Instant.now());
        return eventRepository.save(event);
    }

    private Event createTestEventWithTimestamp(String source, String type, Instant timestamp) {
        Event event = new Event();
        event.setSource(source);
        event.setType(type);
        event.setMessage("Test message");
        event.setTimestamp(timestamp);
        return eventRepository.save(event);
    }
}

