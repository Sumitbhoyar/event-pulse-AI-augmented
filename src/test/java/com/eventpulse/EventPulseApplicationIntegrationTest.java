package com.eventpulse;

import com.eventpulse.dto.LoginRequest;
import com.eventpulse.dto.LoginResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("EventPulse Application Integration Tests")
class EventPulseApplicationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        // Login to get authentication token
        LoginRequest loginRequest = LoginRequest.builder()
                .username("admin")
                .password("password")
                .build();

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LoginResponse loginResponse = objectMapper.readValue(responseContent, LoginResponse.class);
        authToken = loginResponse.getToken();

        assertNotNull(authToken);
        assertTrue(authToken.length() > 0);
    }

    @Test
    @DisplayName("Should access health endpoint without authentication")
    void shouldAccessHealthEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value("OK"));
    }

    @Test
    @DisplayName("Should create, retrieve, and delete event with authentication")
    void shouldCreateRetrieveAndDeleteEventWithAuthentication() throws Exception {
        // Create event
        String eventJson = """
                {
                    "source": "integration-test",
                    "type": "test",
                    "message": "Integration test event"
                }
                """;

        MvcResult createResult = mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.source").value("integration-test"))
                .andExpect(jsonPath("$.type").value("test"))
                .andExpect(jsonPath("$.message").value("Integration test event"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        String eventId = objectMapper.readTree(createResponse).get("id").asText();

        // Retrieve event by ID
        mockMvc.perform(get("/api/events/{id}", eventId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.source").value("integration-test"))
                .andExpect(jsonPath("$.type").value("test"))
                .andExpect(jsonPath("$.message").value("Integration test event"));

        // Retrieve all events
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == '%s')]", eventId).exists());

        // Delete event
        mockMvc.perform(delete("/api/events/{id}", eventId)
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should access metrics endpoint with authentication")
    void shouldAccessMetricsEndpointWithAuthentication() throws Exception {
        mockMvc.perform(get("/api/metrics")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.totalEvents").exists())
                .andExpect(jsonPath("$.eventsByType").isArray())
                .andExpect(jsonPath("$.eventsBySource").isArray());
    }

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint without authentication")
    void shouldReturn401WhenAccessingProtectedEndpointWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when accessing protected endpoint with invalid token")
    void shouldReturn401WhenAccessingProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should validate event creation with proper error messages")
    void shouldValidateEventCreationWithProperErrorMessages() throws Exception {
        String invalidEventJson = """
                {
                    "source": "",
                    "type": "test",
                    "message": "Test message"
                }
                """;

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEventJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.details.source").exists());
    }

    @Test
    @DisplayName("Should handle event filtering with query parameters")
    void shouldHandleEventFilteringWithQueryParameters() throws Exception {
        // First create an event
        String eventJson = """
                {
                    "source": "filter-test",
                    "type": "filter-type",
                    "message": "Filter test event"
                }
                """;

        mockMvc.perform(post("/api/events")
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(eventJson))
                .andExpect(status().isCreated());

        // Filter by type
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + authToken)
                        .param("type", "filter-type"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());

        // Filter by source
        mockMvc.perform(get("/api/events")
                        .header("Authorization", "Bearer " + authToken)
                        .param("source", "filter-test"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @DisplayName("Should handle metrics filtering")
    void shouldHandleMetricsFiltering() throws Exception {
        // Test total metrics
        mockMvc.perform(get("/api/metrics/total")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").exists());

        // Test type-specific metrics
        mockMvc.perform(get("/api/metrics/type/test")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").exists());

        // Test source-specific metrics
        mockMvc.perform(get("/api/metrics/source/test-service")
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.count").exists());
    }

    @Test
    @DisplayName("Should handle CORS preflight requests")
    void shouldHandleCorsPreflightRequests() throws Exception {
        mockMvc.perform(options("/api/events")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }
}
