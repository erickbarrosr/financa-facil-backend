package com.financafacil.application.dto;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class UserResponse {
    private final UUID id;
    private final String name;
    private final String email;
    private final boolean emailVerified;
}
