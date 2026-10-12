package com.hemovia.api.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService("test-secret-key-for-unit-testing-123456", 3600000);
    }

    @Test
    void shouldGenerateTokenContainingOnlyUserId() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId);

        assertThat(token).isNotBlank();

        DecodedJWT decoded = JWT.decode(token);
        assertThat(decoded.getSubject()).isEqualTo(userId.toString());
        // Verify only standard subject, issued-at, expiration exist (no email, role, cpf, etc.)
        assertThat(decoded.getClaims().keySet()).containsExactlyInAnyOrder("sub", "iat", "exp");
    }

    @Test
    void shouldValidateAndExtractUserId() {
        UUID userId = UUID.randomUUID();

        String token = jwtService.generateToken(userId);
        String extractedUserId = jwtService.validateTokenAndGetUserId(token);

        assertThat(extractedUserId).isEqualTo(userId.toString());
    }
}
