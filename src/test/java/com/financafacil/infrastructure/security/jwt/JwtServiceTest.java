package com.financafacil.infrastructure.security.jwt;

import com.financafacil.infrastructure.config.AppProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        var props = new AppProperties();
        props.getJwt().setAccessSecret("test-access-secret-must-be-at-least-256-bits-long-ok");
        props.getJwt().setRefreshSecret("test-refresh-secret-must-be-at-least-256-bits-long-ok");
        props.getJwt().setAccessExpirationMs(900000L);
        props.getJwt().setRefreshExpirationMs(604800000L);
        jwtService = new JwtService(props);
    }

    @Test
    void generateAccessToken_returnsValidJwt() {
        var userId = UUID.randomUUID();
        var token = jwtService.generateAccessToken(userId, "user@test.com");
        assertThat(token).isNotBlank();
        assertThat(jwtService.isAccessTokenValid(token)).isTrue();
    }

    @Test
    void extractUserId_returnsCorrectId() {
        var userId = UUID.randomUUID();
        var token = jwtService.generateAccessToken(userId, "user@test.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(userId);
    }

    @Test
    void isAccessTokenValid_returnsFalseForTamperedToken() {
        var userId = UUID.randomUUID();
        var token = jwtService.generateAccessToken(userId, "user@test.com") + "tampered";
        assertThat(jwtService.isAccessTokenValid(token)).isFalse();
    }

    @Test
    void generateRefreshToken_returnsNonBlankUuid() {
        var token = jwtService.generateRefreshToken();
        assertThat(token).isNotBlank();
        assertThatCode(() -> UUID.fromString(token)).doesNotThrowAnyException();
    }
}
