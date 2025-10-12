package com.eventpulse.service;

import com.eventpulse.dto.MetricsResponse;
import com.eventpulse.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MetricsService Tests")
class MetricsServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private MetricsService metricsService;

    private Instant testStartTime;
    private Instant testEndTime;

    @BeforeEach
    void setUp() {
        testStartTime = Instant.now().minusSeconds(3600);
        testEndTime = Instant.now();
    }

    @Test
    @DisplayName("Should get all metrics successfully")
    void shouldGetAllMetricsSuccessfully() {
        // Given
        Long totalEvents = 100L;
        List<Object[]> eventsByType = Arrays.asList(
                new Object[]{"login", 50L},
                new Object[]{"error", 30L},
                new Object[]{"logout", 20L}
        );
        List<Object[]> eventsBySource = Arrays.asList(
                new Object[]{"user-service", 60L},
                new Object[]{"auth-service", 40L}
        );

        when(eventRepository.getTotalEventCount()).thenReturn(totalEvents);
        when(eventRepository.getEventCountByType()).thenReturn(eventsByType);
        when(eventRepository.getEventCountBySource()).thenReturn(eventsBySource);

        // When
        MetricsResponse response = metricsService.getAllMetrics();

        // Then
        assertNotNull(response);
        assertEquals(100L, response.getTotalEvents());
        assertEquals(3, response.getEventsByType().size());
        assertEquals(2, response.getEventsBySource().size());
        
        // Verify type counts
        assertEquals("login", response.getEventsByType().get(0).getType());
        assertEquals(50L, response.getEventsByType().get(0).getCount());
        assertEquals("error", response.getEventsByType().get(1).getType());
        assertEquals(30L, response.getEventsByType().get(1).getCount());
        
        // Verify source counts
        assertEquals("user-service", response.getEventsBySource().get(0).getSource());
        assertEquals(60L, response.getEventsBySource().get(0).getCount());
        assertEquals("auth-service", response.getEventsBySource().get(1).getSource());
        assertEquals(40L, response.getEventsBySource().get(1).getCount());

        verify(eventRepository, times(1)).getTotalEventCount();
        verify(eventRepository, times(1)).getEventCountByType();
        verify(eventRepository, times(1)).getEventCountBySource();
    }

    @Test
    @DisplayName("Should get metrics by time range successfully")
    void shouldGetMetricsByTimeRangeSuccessfully() {
        // Given
        Long totalEvents = 50L;
        List<Object[]> eventsByType = Arrays.asList(
                new Object[]{"login", 25L},
                new Object[]{"error", 15L},
                new Object[]{"logout", 10L}
        );
        List<Object[]> eventsBySource = Arrays.asList(
                new Object[]{"user-service", 30L},
                new Object[]{"auth-service", 20L}
        );

        when(eventRepository.getTotalEventCountByTimeRange(testStartTime, testEndTime)).thenReturn(totalEvents);
        when(eventRepository.getEventCountByTypeInTimeRange(testStartTime, testEndTime)).thenReturn(eventsByType);
        when(eventRepository.getEventCountBySourceInTimeRange(testStartTime, testEndTime)).thenReturn(eventsBySource);

        // When
        MetricsResponse response = metricsService.getMetricsByTimeRange(testStartTime, testEndTime);

        // Then
        assertNotNull(response);
        assertEquals(50L, response.getTotalEvents());
        assertEquals(3, response.getEventsByType().size());
        assertEquals(2, response.getEventsBySource().size());

        verify(eventRepository, times(1)).getTotalEventCountByTimeRange(testStartTime, testEndTime);
        verify(eventRepository, times(1)).getEventCountByTypeInTimeRange(testStartTime, testEndTime);
        verify(eventRepository, times(1)).getEventCountBySourceInTimeRange(testStartTime, testEndTime);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid time range")
    void shouldThrowIllegalArgumentExceptionForInvalidTimeRange() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> metricsService.getMetricsByTimeRange(testEndTime, testStartTime)
        );

        assertTrue(exception.getMessage().contains("Start time cannot be after end time"));
        verify(eventRepository, never()).getTotalEventCountByTimeRange(any(), any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for null time range")
    void shouldThrowIllegalArgumentExceptionForNullTimeRange() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> metricsService.getMetricsByTimeRange(null, testEndTime)
        );

        assertTrue(exception.getMessage().contains("Both start and end times must be provided"));
        verify(eventRepository, never()).getTotalEventCountByTimeRange(any(), any());
    }

    @Test
    @DisplayName("Should get recent metrics successfully")
    void shouldGetRecentMetricsSuccessfully() {
        // Given
        int hours = 24;
        Long totalEvents = 25L;
        List<Object[]> eventsByType = Arrays.asList(
                new Object[]{"login", 15L},
                new Object[]{"error", 10L}
        );
        List<Object[]> eventsBySource = Arrays.asList(
                new Object[]{"user-service", 20L},
                new Object[]{"auth-service", 5L}
        );

        when(eventRepository.getTotalEventCountByTimeRange(any(Instant.class), any(Instant.class))).thenReturn(totalEvents);
        when(eventRepository.getEventCountByTypeInTimeRange(any(Instant.class), any(Instant.class))).thenReturn(eventsByType);
        when(eventRepository.getEventCountBySourceInTimeRange(any(Instant.class), any(Instant.class))).thenReturn(eventsBySource);

        // When
        MetricsResponse response = metricsService.getRecentMetrics(hours);

        // Then
        assertNotNull(response);
        assertEquals(25L, response.getTotalEvents());
        assertEquals(2, response.getEventsByType().size());
        assertEquals(2, response.getEventsBySource().size());

        verify(eventRepository, times(1)).getTotalEventCountByTimeRange(any(Instant.class), any(Instant.class));
        verify(eventRepository, times(1)).getEventCountByTypeInTimeRange(any(Instant.class), any(Instant.class));
        verify(eventRepository, times(1)).getEventCountBySourceInTimeRange(any(Instant.class), any(Instant.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid hours")
    void shouldThrowIllegalArgumentExceptionForInvalidHours() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> metricsService.getRecentMetrics(-1)
        );

        assertTrue(exception.getMessage().contains("Hours must be a positive number"));
        verify(eventRepository, never()).getTotalEventCountByTimeRange(any(), any());
    }

    @Test
    @DisplayName("Should get event count by type successfully")
    void shouldGetEventCountByTypeSuccessfully() {
        // Given
        String eventType = "login";
        Long count = 45L;
        when(eventRepository.getEventCountBySpecificType(eventType)).thenReturn(count);

        // When
        Long result = metricsService.getEventCountByType(eventType);

        // Then
        assertEquals(45L, result);
        verify(eventRepository, times(1)).getEventCountBySpecificType(eventType);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank type")
    void shouldThrowIllegalArgumentExceptionForBlankType() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> metricsService.getEventCountByType("")
        );

        assertTrue(exception.getMessage().contains("Event type cannot be blank"));
        verify(eventRepository, never()).getEventCountBySpecificType(any());
    }

    @Test
    @DisplayName("Should get event count by source successfully")
    void shouldGetEventCountBySourceSuccessfully() {
        // Given
        String eventSource = "user-service";
        Long count = 60L;
        when(eventRepository.getEventCountBySpecificSource(eventSource)).thenReturn(count);

        // When
        Long result = metricsService.getEventCountBySource(eventSource);

        // Then
        assertEquals(60L, result);
        verify(eventRepository, times(1)).getEventCountBySpecificSource(eventSource);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for blank source")
    void shouldThrowIllegalArgumentExceptionForBlankSource() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> metricsService.getEventCountBySource("   ")
        );

        assertTrue(exception.getMessage().contains("Event source cannot be blank"));
        verify(eventRepository, never()).getEventCountBySpecificSource(any());
    }

    @Test
    @DisplayName("Should get total event count successfully")
    void shouldGetTotalEventCountSuccessfully() {
        // Given
        Long totalCount = 150L;
        when(eventRepository.getTotalEventCount()).thenReturn(totalCount);

        // When
        Long result = metricsService.getTotalEventCount();

        // Then
        assertEquals(150L, result);
        verify(eventRepository, times(1)).getTotalEventCount();
    }

    @Test
    @DisplayName("Should get recent events count successfully")
    void shouldGetRecentEventsCountSuccessfully() {
        // Given
        int hours = 12;
        Long recentCount = 25L;
        when(eventRepository.getRecentEventsCount(any(Instant.class))).thenReturn(recentCount);

        // When
        Long result = metricsService.getRecentEventsCount(hours);

        // Then
        assertEquals(25L, result);
        verify(eventRepository, times(1)).getRecentEventsCount(any(Instant.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException for invalid hours in recent count")
    void shouldThrowIllegalArgumentExceptionForInvalidHoursInRecentCount() {
        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> metricsService.getRecentEventsCount(0)
        );

        assertTrue(exception.getMessage().contains("Hours must be a positive number"));
        verify(eventRepository, never()).getRecentEventsCount(any());
    }

    @Test
    @DisplayName("Should throw MetricsServiceException when repository fails")
    void shouldThrowMetricsServiceExceptionWhenRepositoryFails() {
        // Given
        when(eventRepository.getTotalEventCount())
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        MetricsService.MetricsServiceException exception = assertThrows(
                MetricsService.MetricsServiceException.class,
                () -> metricsService.getAllMetrics()
        );

        assertTrue(exception.getMessage().contains("Failed to retrieve metrics"));
        verify(eventRepository, times(1)).getTotalEventCount();
    }

    @Test
    @DisplayName("Should handle empty results gracefully")
    void shouldHandleEmptyResultsGracefully() {
        // Given
        when(eventRepository.getTotalEventCount()).thenReturn(0L);
        when(eventRepository.getEventCountByType()).thenReturn(Arrays.asList());
        when(eventRepository.getEventCountBySource()).thenReturn(Arrays.asList());

        // When
        MetricsResponse response = metricsService.getAllMetrics();

        // Then
        assertNotNull(response);
        assertEquals(0L, response.getTotalEvents());
        assertEquals(0, response.getEventsByType().size());
        assertEquals(0, response.getEventsBySource().size());
    }
}
