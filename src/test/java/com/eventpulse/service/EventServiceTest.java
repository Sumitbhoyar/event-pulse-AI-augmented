package com.eventpulse.service;

import com.eventpulse.entity.Event;
import com.eventpulse.repository.EventRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private Validator validator;

    @InjectMocks
    private EventService eventService;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = new Event();
        testEvent.setId(UUID.randomUUID());
        testEvent.setSource("test-service");
        testEvent.setType("INFO");
        testEvent.setMessage("Test message");
        testEvent.setTimestamp(Instant.now());
    }

    @Test
    void save_ValidEvent_ShouldSaveSuccessfully() {
        // Arrange
        when(validator.validate(any(Event.class))).thenReturn(Collections.emptySet());
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        Event saved = eventService.save(testEvent);

        // Assert
        assertThat(saved).isNotNull();
        assertThat(saved.getId()).isEqualTo(testEvent.getId());
        verify(eventRepository, times(1)).save(testEvent);
        verify(validator, times(1)).validate(testEvent);
    }

    @Test
    void save_NullEvent_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThatThrownBy(() -> eventService.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event must not be null");

        verify(eventRepository, never()).save(any());
    }

    @Test
    void save_EventWithNullTimestamp_ShouldSetTimestamp() {
        // Arrange
        testEvent.setTimestamp(null);
        when(validator.validate(any(Event.class))).thenReturn(Collections.emptySet());
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);

        // Act
        Event saved = eventService.save(testEvent);

        // Assert
        assertThat(saved.getTimestamp()).isNotNull();
        verify(eventRepository, times(1)).save(testEvent);
    }

    @Test
    void save_InvalidEvent_ShouldThrowConstraintViolationException() {
        // Arrange
        Set<ConstraintViolation<Event>> violations = new HashSet<>();
        ConstraintViolation<Event> violation = mock(ConstraintViolation.class);
        violations.add(violation);
        
        when(validator.validate(any(Event.class))).thenReturn(violations);

        // Act & Assert
        assertThatThrownBy(() -> eventService.save(testEvent))
                .isInstanceOf(ConstraintViolationException.class);

        verify(eventRepository, never()).save(any());
    }

    @Test
    void findByFilters_NoFilters_ShouldReturnAllEvents() {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll(any(Specification.class))).thenReturn(events);

        // Act
        List<Event> result = eventService.findByFilters(null, null, null, null);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(testEvent);
        verify(eventRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findByFilters_WithTypeFilter_ShouldReturnFilteredEvents() {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll(any(Specification.class))).thenReturn(events);

        // Act
        List<Event> result = eventService.findByFilters("INFO", null, null, null);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("INFO");
        verify(eventRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findByFilters_WithSourceFilter_ShouldReturnFilteredEvents() {
        // Arrange
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll(any(Specification.class))).thenReturn(events);

        // Act
        List<Event> result = eventService.findByFilters(null, "test-service", null, null);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSource()).isEqualTo("test-service");
        verify(eventRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findByFilters_WithTimeRange_ShouldReturnFilteredEvents() {
        // Arrange
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll(any(Specification.class))).thenReturn(events);

        // Act
        List<Event> result = eventService.findByFilters(null, null, from, to);

        // Assert
        assertThat(result).hasSize(1);
        verify(eventRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void findByFilters_InvalidTimeRange_ShouldThrowIllegalArgumentException() {
        // Arrange
        Instant from = Instant.now();
        Instant to = Instant.now().minusSeconds(3600);

        // Act & Assert
        assertThatThrownBy(() -> eventService.findByFilters(null, null, from, to))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("from must be before or equal to to");

        verify(eventRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void findByFilters_AllFilters_ShouldReturnFilteredEvents() {
        // Arrange
        Instant from = Instant.now().minusSeconds(3600);
        Instant to = Instant.now();
        List<Event> events = Arrays.asList(testEvent);
        when(eventRepository.findAll(any(Specification.class))).thenReturn(events);

        // Act
        List<Event> result = eventService.findByFilters("INFO", "test-service", from, to);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo("INFO");
        assertThat(result.get(0).getSource()).isEqualTo("test-service");
        verify(eventRepository, times(1)).findAll(any(Specification.class));
    }
}

