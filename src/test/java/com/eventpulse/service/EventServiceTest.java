package com.eventpulse.service;

import com.eventpulse.entity.Event;
import com.eventpulse.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Tests")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;
    private UUID testEventId;

    @BeforeEach
    void setUp() {
        testEventId = UUID.randomUUID();
        testEvent = Event.builder()
                .id(testEventId)
                .source("test-service")
                .type("test")
                .message("Test message")
                .timestamp(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should save event successfully")
    void shouldSaveEventSuccessfully() {
        // Given
        Event eventToSave = Event.builder()
                .source("test-service")
                .type("test")
                .message("Test message")
                .build();

        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // When
        Event savedEvent = eventService.saveEvent(eventToSave);

        // Then
        assertNotNull(savedEvent);
        assertEquals(testEventId, savedEvent.getId());
        assertEquals("test-service", savedEvent.getSource());
        assertEquals("test", savedEvent.getType());
        assertEquals("Test message", savedEvent.getMessage());
        assertNotNull(savedEvent.getTimestamp());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Should set timestamp when not provided")
    void shouldSetTimestampWhenNotProvided() {
        // Given
        Event eventWithoutTimestamp = Event.builder()
                .source("test-service")
                .type("test")
                .message("Test message")
                .build();

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setId(testEventId);
            return event;
        });

        // When
        Event savedEvent = eventService.saveEvent(eventWithoutTimestamp);

        // Then
        assertNotNull(savedEvent.getTimestamp());
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Should throw EventServiceException when save fails")
    void shouldThrowEventServiceExceptionWhenSaveFails() {
        // Given
        Event eventToSave = Event.builder()
                .source("test-service")
                .type("test")
                .message("Test message")
                .build();

        when(eventRepository.save(any(Event.class)))
                .thenThrow(new RuntimeException("Database error"));

        // When & Then
        EventService.EventServiceException exception = assertThrows(
                EventService.EventServiceException.class,
                () -> eventService.saveEvent(eventToSave)
        );

        assertTrue(exception.getMessage().contains("Failed to save event"));
        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    @DisplayName("Should get event by ID successfully")
    void shouldGetEventByIdSuccessfully() {
        // Given
        when(eventRepository.findById(testEventId)).thenReturn(Optional.of(testEvent));

        // When
        Event retrievedEvent = eventService.getEventById(testEventId);

        // Then
        assertNotNull(retrievedEvent);
        assertEquals(testEventId, retrievedEvent.getId());
        verify(eventRepository, times(1)).findById(testEventId);
    }

    @Test
    @DisplayName("Should throw EventNotFoundException when event not found")
    void shouldThrowEventNotFoundExceptionWhenEventNotFound() {
        // Given
        when(eventRepository.findById(testEventId)).thenReturn(Optional.empty());

        // When & Then
        EventService.EventNotFoundException exception = assertThrows(
                EventService.EventNotFoundException.class,
                () -> eventService.getEventById(testEventId)
        );

        assertTrue(exception.getMessage().contains("Event not found with ID"));
        verify(eventRepository, times(1)).findById(testEventId);
    }

    @Test
    @DisplayName("Should get all events successfully")
    void shouldGetAllEventsSuccessfully() {
        // Given
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll()).thenReturn(events);

        // When
        List<Event> retrievedEvents = eventService.getAllEvents();

        // Then
        assertNotNull(retrievedEvents);
        assertEquals(1, retrievedEvents.size());
        assertEquals(testEventId, retrievedEvents.get(0).getId());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should get events by type successfully")
    void shouldGetEventsByTypeSuccessfully() {
        // Given
        String eventType = "login";
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findByTypeIgnoreCase(eventType)).thenReturn(events);

        // When
        List<Event> retrievedEvents = eventService.getEventsByType(eventType);

        // Then
        assertNotNull(retrievedEvents);
        assertEquals(1, retrievedEvents.size());
        verify(eventRepository, times(1)).findByTypeIgnoreCase(eventType);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank type")
    void shouldThrowIllegalArgumentExceptionForBlankType() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.getEventsByType("")
        );

        assertTrue(exception.getMessage().contains("Event type cannot be blank"));
        verify(eventRepository, never()).findByTypeIgnoreCase(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null type")
    void shouldThrowIllegalArgumentExceptionForNullType() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.getEventsByType(null)
        );

        assertTrue(exception.getMessage().contains("Event type cannot be blank"));
        verify(eventRepository, never()).findByTypeIgnoreCase(any());
    }

    @Test
    @DisplayName("Should get events by source successfully")
    void shouldGetEventsBySourceSuccessfully() {
        // Given
        String eventSource = "user-service";
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findBySourceIgnoreCase(eventSource)).thenReturn(events);

        // When
        List<Event> retrievedEvents = eventService.getEventsBySource(eventSource);

        // Then
        assertNotNull(retrievedEvents);
        assertEquals(1, retrievedEvents.size());
        verify(eventRepository, times(1)).findBySourceIgnoreCase(eventSource);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank source")
    void shouldThrowIllegalArgumentExceptionForBlankSource() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.getEventsBySource("   ")
        );

        assertTrue(exception.getMessage().contains("Event source cannot be blank"));
        verify(eventRepository, never()).findBySourceIgnoreCase(any());
    }

    @Test
    @DisplayName("Should get events by timestamp range successfully")
    void shouldGetEventsByTimestampRangeSuccessfully() {
        // Given
        Instant startTime = Instant.now().minusSeconds(3600);
        Instant endTime = Instant.now();
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findByTimestampBetween(startTime, endTime)).thenReturn(events);

        // When
        List<Event> retrievedEvents = eventService.getEventsByTimestampRange(startTime, endTime);

        // Then
        assertNotNull(retrievedEvents);
        assertEquals(1, retrievedEvents.size());
        verify(eventRepository, times(1)).findByTimestampBetween(startTime, endTime);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid time range")
    void shouldThrowIllegalArgumentExceptionForInvalidTimeRange() {
        // Given
        Instant startTime = Instant.now();
        Instant endTime = Instant.now().minusSeconds(3600);

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.getEventsByTimestampRange(startTime, endTime)
        );

        assertTrue(exception.getMessage().contains("Start time cannot be after end time"));
        verify(eventRepository, never()).findByTimestampBetween(any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null time range")
    void shouldThrowIllegalArgumentExceptionForNullTimeRange() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> eventService.getEventsByTimestampRange(null, Instant.now())
        );

        assertTrue(exception.getMessage().contains("Both start and end times must be provided"));
        verify(eventRepository, never()).findByTimestampBetween(any(), any());
    }

    @Test
    @DisplayName("Should get events with all filters successfully")
    void shouldGetEventsWithAllFiltersSuccessfully() {
        // Given
        String type = "login";
        String source = "user-service";
        Instant startTime = Instant.now().minusSeconds(3600);
        Instant endTime = Instant.now();
        List<Event> events = Arrays.asList(testEvent);

        when(eventRepository.findByTypeIgnoreCaseAndSourceIgnoreCaseAndTimestampBetween(
                type, source, startTime, endTime)).thenReturn(events);

        // When
        List<Event> retrievedEvents = eventService.getEventsWithFilters(type, source, startTime, endTime);

        // Then
        assertNotNull(retrievedEvents);
        assertEquals(1, retrievedEvents.size());
        verify(eventRepository, times(1)).findByTypeIgnoreCaseAndSourceIgnoreCaseAndTimestampBetween(
                type, source, startTime, endTime);
    }

    @Test
    @DisplayName("Should get events with type and source filters")
    void shouldGetEventsWithTypeAndSourceFilters() {
        // Given
        String type = "login";
        String source = "user-service";
        List<Event> events = Arrays.asList(testEvent);

        when(eventRepository.findByTypeIgnoreCaseAndSourceIgnoreCase(type, source)).thenReturn(events);

        // When
        List<Event> retrievedEvents = eventService.getEventsWithFilters(type, source, null, null);

        // Then
        assertNotNull(retrievedEvents);
        assertEquals(1, retrievedEvents.size());
        verify(eventRepository, times(1)).findByTypeIgnoreCaseAndSourceIgnoreCase(type, source);
    }

    @Test
    @DisplayName("Should delete event successfully")
    void shouldDeleteEventSuccessfully() {
        // Given
        when(eventRepository.existsById(testEventId)).thenReturn(true);

        // When
        eventService.deleteEvent(testEventId);

        // Then
        verify(eventRepository, times(1)).existsById(testEventId);
        verify(eventRepository, times(1)).deleteById(testEventId);
    }

    @Test
    @DisplayName("Should throw EventNotFoundException when deleting non-existent event")
    void shouldThrowEventNotFoundExceptionWhenDeletingNonExistentEvent() {
        // Given
        when(eventRepository.existsById(testEventId)).thenReturn(false);

        // When & Then
        EventService.EventNotFoundException exception = assertThrows(
                EventService.EventNotFoundException.class,
                () -> eventService.deleteEvent(testEventId)
        );

        assertTrue(exception.getMessage().contains("Event not found with ID"));
        verify(eventRepository, times(1)).existsById(testEventId);
        verify(eventRepository, never()).deleteById(any());
    }
}
