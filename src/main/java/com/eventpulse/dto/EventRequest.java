package com.eventpulse.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class EventRequest {

    @NotBlank(message = "Source is required")
    private String source;

    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Timestamp is required")
    private Instant timestamp;

    // Constructors
    public EventRequest() {}

    public EventRequest(String source, String type, String message, Instant timestamp) {
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
        private String source;
        private String type;
        private String message;
        private Instant timestamp;

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

        public EventRequest build() {
            return new EventRequest(source, type, message, timestamp);
        }
    }

    // Getters and Setters
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
