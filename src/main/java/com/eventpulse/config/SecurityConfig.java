package com.eventpulse.config;

import com.eventpulse.security.JwtAuthenticationFilter;
import com.eventpulse.security.JwtUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Value("${security.jwt.secret:RXZlbnRQdWxzZVNlY3VyZUtleUZvckpXVEF1dGhlbnRpY2F0aW9uMjU2Qml0cw==}")
    private String jwtSecretBase64;

    @Value("${security.jwt.validity-ms:3600000}")
    private long validityMs;

    @Bean
    public JwtUtils jwtUtils() {
        return new JwtUtils(jwtSecretBase64, validityMs);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/health", "/h2-console/**", "/ws", "/ws/**", "/ws-client.html", "/", "/index.html", "/static/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .anyRequest().authenticated()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.disable()));

        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtils()), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}


