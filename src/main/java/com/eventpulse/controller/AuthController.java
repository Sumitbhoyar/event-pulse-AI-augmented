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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication endpoints for JWT token management")
public class AuthController {

    private final JwtUtils jwtUtils;

    public AuthController(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate user and receive JWT token. For demo purposes, accepts any username/password combination."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content)
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        // For demo, accept any username/password; replace with real auth later
        String token = jwtUtils.generateToken(request.getUsername());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}


