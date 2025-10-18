package com.eventpulse.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI eventPulseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EventPulse API")
                        .description("Event management system with real-time WebSocket support, JWT authentication, and metrics aggregation")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("EventPulse Team")
                                .email("support@eventpulse.com")
                                .url("https://github.com/eventpulse"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development"),
                        new Server().url("https://api.eventpulse.com").description("Production")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token obtained from /api/auth/login endpoint")))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }
}

