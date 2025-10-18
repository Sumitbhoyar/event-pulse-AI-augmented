package com.eventpulse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @NotBlank
    private String source;

    @Column(nullable = false)
    @NotBlank
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    @NotBlank
    private String message;

    @Column(nullable = false)
    @NotNull
    private Instant timestamp;

    // Constructors
    public Event() {}

    public Event(UUID id, String source, String type, String message, Instant timestamp) {
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

        public Event build() {
            return new Event(id, source, type, message, timestamp);
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
