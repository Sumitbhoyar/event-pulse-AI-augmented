package com.eventpulse.controller;

import com.eventpulse.dto.MetricsResponse;
import com.eventpulse.service.MetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metrics")
@Tag(name = "Metrics", description = "Event metrics and aggregation endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class MetricsController {

    private final MetricsService metricsService;

    public MetricsController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    @GetMapping
    @Operation(
            summary = "Get event metrics",
            description = "Retrieves aggregated metrics including total count, counts by type, and counts by source"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MetricsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - JWT token missing or invalid",
                    content = @Content)
    })
    public ResponseEntity<MetricsResponse> getMetrics() {
        return ResponseEntity.ok(metricsService.getMetrics());
    }
}


