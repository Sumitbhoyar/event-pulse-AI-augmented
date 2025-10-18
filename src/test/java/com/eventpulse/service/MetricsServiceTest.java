package com.eventpulse.service;

import com.eventpulse.dto.MetricsResponse;
import com.eventpulse.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void getMetrics_WithEvents_ShouldReturnCorrectMetrics() {
        // Arrange
        when(eventRepository.countAllEvents()).thenReturn(100L);
        
        List<Object[]> typeData = Arrays.asList(
                new Object[]{"LOGIN", 40L},
                new Object[]{"LOGOUT", 30L},
                new Object[]{"ERROR", 30L}
        );
        when(eventRepository.countByType()).thenReturn(typeData);

        List<Object[]> sourceData = Arrays.asList(
                new Object[]{"user-service", 60L},
                new Object[]{"payment-service", 40L}
        );
        when(eventRepository.countBySource()).thenReturn(sourceData);

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(100L);
        
        assertThat(result.getCountByType()).hasSize(3);
        assertThat(result.getCountByType().get("LOGIN")).isEqualTo(40L);
        assertThat(result.getCountByType().get("LOGOUT")).isEqualTo(30L);
        assertThat(result.getCountByType().get("ERROR")).isEqualTo(30L);
        
        assertThat(result.getCountBySource()).hasSize(2);
        assertThat(result.getCountBySource().get("user-service")).isEqualTo(60L);
        assertThat(result.getCountBySource().get("payment-service")).isEqualTo(40L);

        verify(eventRepository, times(1)).countAllEvents();
        verify(eventRepository, times(1)).countByType();
        verify(eventRepository, times(1)).countBySource();
    }

    @Test
    void getMetrics_NoEvents_ShouldReturnZeroMetrics() {
        // Arrange
        when(eventRepository.countAllEvents()).thenReturn(0L);
        when(eventRepository.countByType()).thenReturn(Collections.emptyList());
        when(eventRepository.countBySource()).thenReturn(Collections.emptyList());

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(0L);
        assertThat(result.getCountByType()).isEmpty();
        assertThat(result.getCountBySource()).isEmpty();
    }

    @Test
    void getMetrics_NullValuesInData_ShouldHandleGracefully() {
        // Arrange
        when(eventRepository.countAllEvents()).thenReturn(50L);
        
        List<Object[]> typeData = Arrays.asList(
                new Object[]{null, 10L},
                new Object[]{"INFO", null},
                new Object[]{"ERROR", 20L}
        );
        when(eventRepository.countByType()).thenReturn(typeData);

        List<Object[]> sourceData = new ArrayList<>();
        sourceData.add(new Object[]{"service1", 30L});
        when(eventRepository.countBySource()).thenReturn(sourceData);

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(50L);
        assertThat(result.getCountByType()).containsKeys("", "INFO", "ERROR");
        assertThat(result.getCountByType().get("")).isEqualTo(10L);
        assertThat(result.getCountByType().get("INFO")).isEqualTo(0L);
        assertThat(result.getCountByType().get("ERROR")).isEqualTo(20L);
    }

    @Test
    void getMetrics_EmptyArraysInData_ShouldSkip() {
        // Arrange
        when(eventRepository.countAllEvents()).thenReturn(10L);
        
        List<Object[]> typeData = new ArrayList<>();
        typeData.add(null);
        typeData.add(new Object[]{});
        typeData.add(new Object[]{"INFO"});
        typeData.add(new Object[]{"ERROR", 10L});
        when(eventRepository.countByType()).thenReturn(typeData);

        when(eventRepository.countBySource()).thenReturn(Collections.emptyList());

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(10L);
        assertThat(result.getCountByType()).containsKey("ERROR");
        assertThat(result.getCountByType().get("ERROR")).isEqualTo(10L);
    }

    @Test
    void getMetrics_SingleEvent_ShouldReturnCorrectCounts() {
        // Arrange
        when(eventRepository.countAllEvents()).thenReturn(1L);
        
        List<Object[]> typeData = Collections.singletonList(
                new Object[]{"LOGIN", 1L}
        );
        when(eventRepository.countByType()).thenReturn(typeData);

        List<Object[]> sourceData = Collections.singletonList(
                new Object[]{"api", 1L}
        );
        when(eventRepository.countBySource()).thenReturn(sourceData);

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(1L);
        assertThat(result.getCountByType()).hasSize(1);
        assertThat(result.getCountBySource()).hasSize(1);
    }

    @Test
    void getMetrics_LargeCounts_ShouldHandleCorrectly() {
        // Arrange
        when(eventRepository.countAllEvents()).thenReturn(1000000L);
        
        List<Object[]> typeData = Arrays.asList(
                new Object[]{"TYPE1", 500000L},
                new Object[]{"TYPE2", 500000L}
        );
        when(eventRepository.countByType()).thenReturn(typeData);

        List<Object[]> sourceData = new ArrayList<>();
        sourceData.add(new Object[]{"source1", 1000000L});
        when(eventRepository.countBySource()).thenReturn(sourceData);

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalCount()).isEqualTo(1000000L);
        assertThat(result.getCountByType().get("TYPE1")).isEqualTo(500000L);
        assertThat(result.getCountByType().get("TYPE2")).isEqualTo(500000L);
    }
}

