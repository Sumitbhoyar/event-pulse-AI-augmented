package com.eventpulse.util;

import com.eventpulse.dto.EventRequest;
import com.eventpulse.entity.Event;

import java.time.Instant;
import java.util.UUID;

public class TestUtils {

    public static Event createTestEvent() {
        return createTestEvent(UUID.randomUUID());
    }

    public static Event createTestEvent(UUID id) {
        return Event.builder()
                .id(id)
                .source("test-service")
                .type("test")
                .message("Test message")
                .timestamp(Instant.now())
                .build();
    }

    public static Event createTestEvent(String source, String type, String message) {
        return Event.builder()
                .id(UUID.randomUUID())
                .source(source)
                .type(type)
                .message(message)
                .timestamp(Instant.now())
                .build();
    }

    public static EventRequest createTestEventRequest() {
        return EventRequest.builder()
                .source("test-service")
                .type("test")
                .message("Test message")
                .build();
    }

    public static EventRequest createTestEventRequest(String source, String type, String message) {
        return EventRequest.builder()
                .source(source)
                .type(type)
                .message(message)
                .build();
    }

    public static EventRequest createInvalidEventRequest() {
        return EventRequest.builder()
                .source("") // Invalid: empty source
                .type("test")
                .message("Test message")
                .build();
    }

    public static String createValidJwtToken() {
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJpYXQiOjE2NzAwMDAwMDAsImV4cCI6MTY3MDA4NjQwMH0.test-signature";
    }

    public static String createExpiredJwtToken() {
        return "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0LXVzZXIiLCJpYXQiOjE2NzAwMDAwMDAsImV4cCI6MTY3MDAwMDAwMH0.expired-signature";
    }

    public static String createInvalidJwtToken() {
        return "invalid.jwt.token";
    }

    public static String createBearerToken(String token) {
        return "Bearer " + token;
    }
}
