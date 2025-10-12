package com.eventpulse.controller;

import com.eventpulse.dto.LoginRequest;
import com.eventpulse.dto.LoginResponse;
import com.eventpulse.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // Allow CORS for development
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    /**
     * Authenticate user and return JWT token
     * POST /api/auth/login
     */
    @Operation(
            summary = "User Authentication",
            description = "Authenticate a user with username and password, returns a JWT token for accessing protected endpoints."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.eventpulse.exception.GlobalExceptionHandler.ErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.eventpulse.exception.GlobalExceptionHandler.ErrorResponse.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Authentication attempt for user: {}", loginRequest.getUsername());
        
        try {
            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Generate JWT token
            String jwt = jwtUtils.generateJwtToken(authentication);
            
            // Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // Calculate expiration time
            Instant expiresAt = Instant.now().plusSeconds(86400); // 24 hours
            
            // Create response
            LoginResponse response = LoginResponse.builder()
                    .token(jwt)
                    .username(userDetails.getUsername())
                    .expiresAt(expiresAt)
                    .message("Authentication successful")
                    .build();
            
            log.info("Authentication successful for user: {}", userDetails.getUsername());
            return ResponseEntity.ok(response);
            
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {} - {}", loginRequest.getUsername(), e.getMessage());
            throw new AuthenticationException("Invalid username or password") {
                @Override
                public String getMessage() {
                    return "Invalid username or password";
                }
            };
        } catch (Exception e) {
            log.error("Unexpected error during authentication: {}", e.getMessage(), e);
            throw new AuthenticationException("Authentication failed") {
                @Override
                public String getMessage() {
                    return "Authentication failed due to server error";
                }
            };
        }
    }

    /**
     * Validate token endpoint (optional)
     * GET /api/auth/validate
     */
    @Operation(
            summary = "Validate JWT Token",
            description = "Validate an existing JWT token and return token information if valid."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token is valid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid or expired token",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = com.eventpulse.exception.GlobalExceptionHandler.ErrorResponse.class)
                    )
            )
    })
    @GetMapping("/validate")
    public ResponseEntity<LoginResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        log.debug("Token validation request");
        
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                if (jwtUtils.validateJwtToken(token)) {
                    String username = jwtUtils.getUserNameFromJwtToken(token);
                    Instant expiresAt = jwtUtils.getExpirationDateFromToken(token).toInstant();
                    
                    LoginResponse response = LoginResponse.builder()
                            .token(token)
                            .username(username)
                            .expiresAt(expiresAt)
                            .message("Token is valid")
                            .build();
                    
                    return ResponseEntity.ok(response);
                }
            }
            
            throw new AuthenticationException("Invalid token") {
                @Override
                public String getMessage() {
                    return "Invalid or expired token";
                }
            };
            
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            throw new AuthenticationException("Token validation failed") {
                @Override
                public String getMessage() {
                    return "Invalid or expired token";
                }
            };
        }
    }
}
