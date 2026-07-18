package com.financafacil.application.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponse {
    private final String accessToken;
    private final String tokenType;
    private final long expiresIn;
    private final UserResponse user;

    public static AuthResponse of(String accessToken, long expiresIn, UserResponse user) {
        return AuthResponse.builder()
            .accessToken(accessToken)
            .tokenType("Bearer")
            .expiresIn(expiresIn)
            .user(user)
            .build();
    }
}
