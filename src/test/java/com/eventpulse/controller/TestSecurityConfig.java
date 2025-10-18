package com.eventpulse.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.eventpulse.security.JwtUtils;

@TestConfiguration
public class TestSecurityConfig {

    @MockBean
    private JwtUtils jwtUtils;
}

