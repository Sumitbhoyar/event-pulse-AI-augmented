package com.eventpulse.dto;

import java.time.Instant;
import java.util.UUID;

public class EventResponse {

    private UUID id;
    private String source;
    private String type;
    private String message;
    private Instant timestamp;

    // Constructors
    public EventResponse() {}

    public EventResponse(UUID id, String source, String type, String message, Instant timestamp) {
        this.id = id;
        this.source = source;
        this.type = type;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String source;
        private String type;
        private String message;
        private Instant timestamp;

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder source(String source) {
            this.source = source;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public EventResponse build() {
            return new EventResponse(id, source, type, message, timestamp);
        }
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
