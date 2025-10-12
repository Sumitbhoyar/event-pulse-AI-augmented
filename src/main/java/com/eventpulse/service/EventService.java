package com.eventpulse.service;

import com.eventpulse.entity.Event;
import com.eventpulse.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;

    /**
     * Saves a new event to the database
     * @param event the event to save
     * @return the saved event with generated ID
     * @throws IllegalArgumentException if event data is invalid
     */
    @Transactional
    public Event saveEvent(@Valid @NotNull Event event) {
        log.debug("Saving event: type={}, source={}", event.getType(), event.getSource());
        
        try {
            // Ensure timestamp is set if not provided
            if (event.getTimestamp() == null) {
                event.setTimestamp(Instant.now());
            }
            
            Event savedEvent = eventRepository.save(event);
            log.info("Event saved successfully with ID: {}", savedEvent.getId());
            return savedEvent;
            
        } catch (Exception e) {
            log.error("Failed to save event: {}", e.getMessage(), e);
            throw new EventServiceException("Failed to save event: " + e.getMessage(), e);
        }
    }

    /**
     * Retrieves an event by its ID
     * @param id the event ID
     * @return the event if found
     * @throws EventNotFoundException if event not found
     */
    public Event getEventById(@NotNull UUID id) {
        log.debug("Retrieving event by ID: {}", id);
        
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + id));
    }

    /**
     * Retrieves all events
     * @return list of all events
     */
    public List<Event> getAllEvents() {
        log.debug("Retrieving all events");
        return eventRepository.findAll();
    }

    /**
     * Retrieves events filtered by type
     * @param type the event type to filter by
     * @return list of events matching the type
     * @throws IllegalArgumentException if type is blank
     */
    public List<Event> getEventsByType(@NotBlank String type) {
        log.debug("Retrieving events by type: {}", type);
        
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be blank");
        }
        
        return eventRepository.findByTypeIgnoreCase(type.trim());
    }

    /**
     * Retrieves events filtered by source
     * @param source the event source to filter by
     * @return list of events matching the source
     * @throws IllegalArgumentException if source is blank
     */
    public List<Event> getEventsBySource(@NotBlank String source) {
        log.debug("Retrieving events by source: {}", source);
        
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Event source cannot be blank");
        }
        
        return eventRepository.findBySourceIgnoreCase(source.trim());
    }

    /**
     * Retrieves events within a timestamp range
     * @param startTime the start of the time range (inclusive)
     * @param endTime the end of the time range (inclusive)
     * @return list of events within the time range
     * @throws IllegalArgumentException if time range is invalid
     */
    public List<Event> getEventsByTimestampRange(@NotNull Instant startTime, @NotNull Instant endTime) {
        log.debug("Retrieving events by timestamp range: {} to {}", startTime, endTime);
        
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Both start and end times must be provided");
        }
        
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        
        return eventRepository.findByTimestampBetween(startTime, endTime);
    }

    /**
     * Retrieves events filtered by multiple criteria
     * @param type the event type (optional)
     * @param source the event source (optional)
     * @param startTime the start of the time range (optional)
     * @param endTime the end of the time range (optional)
     * @return list of events matching all provided criteria
     */
    public List<Event> getEventsWithFilters(String type, String source, Instant startTime, Instant endTime) {
        log.debug("Retrieving events with filters: type={}, source={}, startTime={}, endTime={}", 
                type, source, startTime, endTime);
        
        // Validate time range if both are provided
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        
        // Handle different combinations of filters
        boolean hasType = type != null && !type.trim().isEmpty();
        boolean hasSource = source != null && !source.trim().isEmpty();
        boolean hasTimeRange = startTime != null && endTime != null;
        
        if (hasType && hasSource && hasTimeRange) {
            // All filters
            return eventRepository.findByTypeIgnoreCaseAndSourceIgnoreCaseAndTimestampBetween(
                    type.trim(), source.trim(), startTime, endTime);
        } else if (hasType && hasSource) {
            // Type and source only
            return eventRepository.findByTypeIgnoreCaseAndSourceIgnoreCase(type.trim(), source.trim());
        } else if (hasType && hasTimeRange) {
            // Type and time range
            return eventRepository.findByTypeIgnoreCaseAndTimestampBetween(type.trim(), startTime, endTime);
        } else if (hasSource && hasTimeRange) {
            // Source and time range
            return eventRepository.findBySourceIgnoreCaseAndTimestampBetween(source.trim(), startTime, endTime);
        } else if (hasType) {
            // Type only
            return getEventsByType(type);
        } else if (hasSource) {
            // Source only
            return getEventsBySource(source);
        } else if (hasTimeRange) {
            // Time range only
            return getEventsByTimestampRange(startTime, endTime);
        } else {
            // No filters - return all events
            return getAllEvents();
        }
    }

    /**
     * Deletes an event by its ID
     * @param id the event ID to delete
     * @throws EventNotFoundException if event not found
     */
    @Transactional
    public void deleteEvent(@NotNull UUID id) {
        log.debug("Deleting event by ID: {}", id);
        
        if (!eventRepository.existsById(id)) {
            throw new EventNotFoundException("Event not found with ID: " + id);
        }
        
        eventRepository.deleteById(id);
        log.info("Event deleted successfully with ID: {}", id);
    }

    /**
     * Custom exception for event service operations
     */
    public static class EventServiceException extends RuntimeException {
        public EventServiceException(String message, Throwable cause) {
            super(message, cause);
        }
        
        public EventServiceException(String message) {
            super(message);
        }
    }

    /**
     * Custom exception for when event is not found
     */
    public static class EventNotFoundException extends RuntimeException {
        public EventNotFoundException(String message) {
            super(message);
        }
    }
}
