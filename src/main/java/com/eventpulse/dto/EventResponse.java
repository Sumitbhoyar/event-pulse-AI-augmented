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
public class EventResponse {

    private UUID id;
    private String source;
    private String type;
    private String message;
    private Instant timestamp;

    public static EventResponse fromEntity(Event event) {
        return EventResponse.builder()
                .id(event.getId())
                .source(event.getSource())
                .type(event.getType())
                .message(event.getMessage())
                .timestamp(event.getTimestamp())
                .build();
    }
}
