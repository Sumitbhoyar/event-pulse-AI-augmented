package com.eventpulse.dto;

import com.eventpulse.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventWebSocketMessage {

    private String messageType;
    private EventResponse event;
    private Instant timestamp;
    private String description;

    public static EventWebSocketMessage newEventCreated(Event event) {
        return EventWebSocketMessage.builder()
                .messageType("NEW_EVENT")
                .event(EventResponse.fromEntity(event))
                .timestamp(Instant.now())
                .description("A new event has been created")
                .build();
    }

    public static EventWebSocketMessage eventDeleted(UUID eventId) {
        return EventWebSocketMessage.builder()
                .messageType("EVENT_DELETED")
                .event(EventResponse.builder()
                        .id(eventId)
                        .build())
                .timestamp(Instant.now())
                .description("An event has been deleted")
                .build();
    }

    public static EventWebSocketMessage connectionEstablished() {
        return EventWebSocketMessage.builder()
                .messageType("CONNECTION_ESTABLISHED")
                .timestamp(Instant.now())
                .description("WebSocket connection established successfully")
                .build();
    }
}
