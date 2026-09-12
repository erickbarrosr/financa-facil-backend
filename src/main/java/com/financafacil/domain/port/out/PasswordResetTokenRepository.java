package com.financafacil.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepository {
    void save(UUID id, UUID userId, String tokenHash, Instant expiresAt);
    Optional<TokenData> findByTokenHash(String tokenHash);
    void markAsUsed(UUID id);

    record TokenData(UUID id, UUID userId, Instant expiresAt, boolean used) {}
}
