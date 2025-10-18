package com.eventpulse.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Application health check endpoint (public)")
public class HealthController {

    @GetMapping("/health")
    @Operation(
            summary = "Health check",
            description = "Simple health check endpoint that returns application status. This endpoint is public and does not require authentication."
    )
    @ApiResponse(responseCode = "200", description = "Application is healthy",
            content = @Content(schema = @Schema(example = "{\"status\":\"OK\"}")))
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "OK"));
    }
}
