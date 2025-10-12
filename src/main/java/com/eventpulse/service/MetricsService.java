package com.eventpulse.service;

import com.eventpulse.dto.MetricsResponse;
import com.eventpulse.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MetricsService {

    private final EventRepository eventRepository;

    /**
     * Get comprehensive metrics for all events
     * @return MetricsResponse with total count, counts by type and source
     */
    public MetricsResponse getAllMetrics() {
        log.debug("Retrieving all metrics");
        
        try {
            Long totalEvents = eventRepository.getTotalEventCount();
            List<Object[]> eventsByType = eventRepository.getEventCountByType();
            List<Object[]> eventsBySource = eventRepository.getEventCountBySource();

            return buildMetricsResponse(totalEvents, eventsByType, eventsBySource);
            
        } catch (Exception e) {
            log.error("Failed to retrieve metrics: {}", e.getMessage(), e);
            throw new MetricsServiceException("Failed to retrieve metrics: " + e.getMessage(), e);
        }
    }

    /**
     * Get metrics for events within a specific time range
     * @param startTime the start of the time range
     * @param endTime the end of the time range
     * @return MetricsResponse with filtered metrics
     */
    public MetricsResponse getMetricsByTimeRange(Instant startTime, Instant endTime) {
        log.debug("Retrieving metrics for time range: {} to {}", startTime, endTime);
        
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Both start and end times must be provided");
        }
        
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        
        try {
            Long totalEvents = eventRepository.getTotalEventCountByTimeRange(startTime, endTime);
            List<Object[]> eventsByType = eventRepository.getEventCountByTypeInTimeRange(startTime, endTime);
            List<Object[]> eventsBySource = eventRepository.getEventCountBySourceInTimeRange(startTime, endTime);

            return buildMetricsResponse(totalEvents, eventsByType, eventsBySource);
            
        } catch (Exception e) {
            log.error("Failed to retrieve metrics by time range: {}", e.getMessage(), e);
            throw new MetricsServiceException("Failed to retrieve metrics by time range: " + e.getMessage(), e);
        }
    }

    /**
     * Get metrics for the last N hours
     * @param hours the number of hours to look back
     * @return MetricsResponse for recent events
     */
    public MetricsResponse getRecentMetrics(int hours) {
        log.debug("Retrieving metrics for last {} hours", hours);
        
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be a positive number");
        }
        
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        return getMetricsByTimeRange(since, Instant.now());
    }

    /**
     * Get count of events by specific type
     * @param type the event type
     * @return count of events with the specified type
     */
    public Long getEventCountByType(String type) {
        log.debug("Retrieving event count for type: {}", type);
        
        if (type == null || type.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be blank");
        }
        
        try {
            return eventRepository.getEventCountBySpecificType(type.trim());
        } catch (Exception e) {
            log.error("Failed to retrieve event count by type: {}", e.getMessage(), e);
            throw new MetricsServiceException("Failed to retrieve event count by type: " + e.getMessage(), e);
        }
    }

    /**
     * Get count of events by specific source
     * @param source the event source
     * @return count of events from the specified source
     */
    public Long getEventCountBySource(String source) {
        log.debug("Retrieving event count for source: {}", source);
        
        if (source == null || source.trim().isEmpty()) {
            throw new IllegalArgumentException("Event source cannot be blank");
        }
        
        try {
            return eventRepository.getEventCountBySpecificSource(source.trim());
        } catch (Exception e) {
            log.error("Failed to retrieve event count by source: {}", e.getMessage(), e);
            throw new MetricsServiceException("Failed to retrieve event count by source: " + e.getMessage(), e);
        }
    }

    /**
     * Get total count of all events
     * @return total event count
     */
    public Long getTotalEventCount() {
        log.debug("Retrieving total event count");
        
        try {
            return eventRepository.getTotalEventCount();
        } catch (Exception e) {
            log.error("Failed to retrieve total event count: {}", e.getMessage(), e);
            throw new MetricsServiceException("Failed to retrieve total event count: " + e.getMessage(), e);
        }
    }

    /**
     * Get recent events count (last 24 hours by default)
     * @return count of recent events
     */
    public Long getRecentEventsCount() {
        return getRecentEventsCount(24);
    }

    /**
     * Get recent events count for the last N hours
     * @param hours the number of hours to look back
     * @return count of recent events
     */
    public Long getRecentEventsCount(int hours) {
        log.debug("Retrieving recent events count for last {} hours", hours);
        
        if (hours <= 0) {
            throw new IllegalArgumentException("Hours must be a positive number");
        }
        
        try {
            Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
            return eventRepository.getRecentEventsCount(since);
        } catch (Exception e) {
            log.error("Failed to retrieve recent events count: {}", e.getMessage(), e);
            throw new MetricsServiceException("Failed to retrieve recent events count: " + e.getMessage(), e);
        }
    }

    /**
     * Build MetricsResponse from raw query results
     */
    private MetricsResponse buildMetricsResponse(Long totalEvents, List<Object[]> eventsByType, List<Object[]> eventsBySource) {
        List<MetricsResponse.TypeCount> typeCounts = eventsByType.stream()
                .map(result -> MetricsResponse.TypeCount.builder()
                        .type((String) result[0])
                        .count((Long) result[1])
                        .build())
                .collect(Collectors.toList());

        List<MetricsResponse.SourceCount> sourceCounts = eventsBySource.stream()
                .map(result -> MetricsResponse.SourceCount.builder()
                        .source((String) result[0])
                        .count((Long) result[1])
                        .build())
                .collect(Collectors.toList());

        return MetricsResponse.builder()
                .totalEvents(totalEvents)
                .eventsByType(typeCounts)
                .eventsBySource(sourceCounts)
                .build();
    }

    /**
     * Custom exception for metrics service operations
     */
    public static class MetricsServiceException extends RuntimeException {
        public MetricsServiceException(String message, Throwable cause) {
            super(message, cause);
        }
        
        public MetricsServiceException(String message) {
            super(message);
        }
    }
}
