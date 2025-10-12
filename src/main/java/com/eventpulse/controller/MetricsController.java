package com.eventpulse.controller;

import com.eventpulse.dto.MetricsResponse;
import com.eventpulse.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/metrics")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow CORS for development
@Tag(name = "Metrics", description = "Event analytics and metrics endpoints")
@SecurityRequirement(name = "bearerAuth")
public class MetricsController {

    private final MetricsService metricsService;

    /**
     * Get comprehensive metrics for all events
     * GET /api/metrics
     */
    @GetMapping
    public ResponseEntity<MetricsResponse> getAllMetrics() {
        log.info("Retrieving all metrics");
        
        try {
            MetricsResponse metrics = metricsService.getAllMetrics();
            log.debug("Retrieved metrics: totalEvents={}, types={}, sources={}", 
                    metrics.getTotalEvents(), 
                    metrics.getEventsByType().size(), 
                    metrics.getEventsBySource().size());
            
            return ResponseEntity.ok(metrics);
            
        } catch (MetricsService.MetricsServiceException e) {
            log.error("Failed to retrieve metrics: {}", e.getMessage());
            throw new MetricsRetrievalException("Failed to retrieve metrics: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving metrics: {}", e.getMessage(), e);
            throw new MetricsRetrievalException("An unexpected error occurred while retrieving metrics", e);
        }
    }

    /**
     * Get metrics for events within a specific time range
     * GET /api/metrics?startTime=2023-12-01T00:00:00Z&endTime=2023-12-01T23:59:59Z
     */
    @GetMapping(params = {"startTime", "endTime"})
    public ResponseEntity<MetricsResponse> getMetricsByTimeRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        
        log.info("Retrieving metrics for time range: {} to {}", startTime, endTime);
        
        try {
            MetricsResponse metrics = metricsService.getMetricsByTimeRange(startTime, endTime);
            log.debug("Retrieved metrics for time range: totalEvents={}", metrics.getTotalEvents());
            
            return ResponseEntity.ok(metrics);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid time range parameters: {}", e.getMessage());
            throw new InvalidTimeRangeException(e.getMessage());
        } catch (MetricsService.MetricsServiceException e) {
            log.error("Failed to retrieve metrics by time range: {}", e.getMessage());
            throw new MetricsRetrievalException("Failed to retrieve metrics by time range: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving metrics by time range: {}", e.getMessage(), e);
            throw new MetricsRetrievalException("An unexpected error occurred while retrieving metrics", e);
        }
    }

    /**
     * Get metrics for the last N hours
     * GET /api/metrics/recent?hours=24
     */
    @GetMapping("/recent")
    public ResponseEntity<MetricsResponse> getRecentMetrics(@RequestParam(defaultValue = "24") int hours) {
        log.info("Retrieving recent metrics for last {} hours", hours);
        
        try {
            MetricsResponse metrics = metricsService.getRecentMetrics(hours);
            log.debug("Retrieved recent metrics: totalEvents={}", metrics.getTotalEvents());
            
            return ResponseEntity.ok(metrics);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid hours parameter: {}", e.getMessage());
            throw new InvalidParameterException(e.getMessage());
        } catch (MetricsService.MetricsServiceException e) {
            log.error("Failed to retrieve recent metrics: {}", e.getMessage());
            throw new MetricsRetrievalException("Failed to retrieve recent metrics: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving recent metrics: {}", e.getMessage(), e);
            throw new MetricsRetrievalException("An unexpected error occurred while retrieving recent metrics", e);
        }
    }

    /**
     * Get total count of all events
     * GET /api/metrics/total
     */
    @GetMapping("/total")
    public ResponseEntity<CountResponse> getTotalEventCount() {
        log.debug("Retrieving total event count");
        
        try {
            Long totalCount = metricsService.getTotalEventCount();
            CountResponse response = CountResponse.builder()
                    .count(totalCount)
                    .build();
            
            log.debug("Total event count: {}", totalCount);
            return ResponseEntity.ok(response);
            
        } catch (MetricsService.MetricsServiceException e) {
            log.error("Failed to retrieve total event count: {}", e.getMessage());
            throw new MetricsRetrievalException("Failed to retrieve total event count: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving total event count: {}", e.getMessage(), e);
            throw new MetricsRetrievalException("An unexpected error occurred while retrieving total event count", e);
        }
    }

    /**
     * Get count of events by specific type
     * GET /api/metrics/type/{type}
     */
    @GetMapping("/type/{type}")
    public ResponseEntity<CountResponse> getEventCountByType(@PathVariable String type) {
        log.debug("Retrieving event count for type: {}", type);
        
        try {
            Long count = metricsService.getEventCountByType(type);
            CountResponse response = CountResponse.builder()
                    .count(count)
                    .build();
            
            log.debug("Event count for type '{}': {}", type, count);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid type parameter: {}", e.getMessage());
            throw new InvalidParameterException(e.getMessage());
        } catch (MetricsService.MetricsServiceException e) {
            log.error("Failed to retrieve event count by type: {}", e.getMessage());
            throw new MetricsRetrievalException("Failed to retrieve event count by type: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving event count by type: {}", e.getMessage(), e);
            throw new MetricsRetrievalException("An unexpected error occurred while retrieving event count by type", e);
        }
    }

    /**
     * Get count of events by specific source
     * GET /api/metrics/source/{source}
     */
    @GetMapping("/source/{source}")
    public ResponseEntity<CountResponse> getEventCountBySource(@PathVariable String source) {
        log.debug("Retrieving event count for source: {}", source);
        
        try {
            Long count = metricsService.getEventCountBySource(source);
            CountResponse response = CountResponse.builder()
                    .count(count)
                    .build();
            
            log.debug("Event count for source '{}': {}", source, count);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid source parameter: {}", e.getMessage());
            throw new InvalidParameterException(e.getMessage());
        } catch (MetricsService.MetricsServiceException e) {
            log.error("Failed to retrieve event count by source: {}", e.getMessage());
            throw new MetricsRetrievalException("Failed to retrieve event count by source: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error retrieving event count by source: {}", e.getMessage(), e);
            throw new MetricsRetrievalException("An unexpected error occurred while retrieving event count by source", e);
        }
    }

    // Custom exceptions for better error handling
    public static class MetricsRetrievalException extends RuntimeException {
        public MetricsRetrievalException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidTimeRangeException extends RuntimeException {
        public InvalidTimeRangeException(String message) {
            super(message);
        }
    }

    public static class InvalidParameterException extends RuntimeException {
        public InvalidParameterException(String message) {
            super(message);
        }
    }

    // Simple response DTO for count endpoints
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CountResponse {
        private Long count;
    }
}
