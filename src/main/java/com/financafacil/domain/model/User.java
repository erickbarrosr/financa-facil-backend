package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@With
public class User {
    private final UUID id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final Instant createdAt;
    private final Instant updatedAt;
    private final Instant lastLoginAt;
    private final boolean emailVerified;
    private final boolean active;
}
