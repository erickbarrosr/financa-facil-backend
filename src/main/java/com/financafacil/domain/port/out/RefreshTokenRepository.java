package com.financafacil.domain.port.out;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository {
    void save(UUID id, UUID userId, String tokenHash, Instant expiresAt);
    Optional<RefreshTokenData> findByTokenHash(String tokenHash);
    void revokeAllByUserId(UUID userId);
    void revokeById(UUID id);

    record RefreshTokenData(UUID id, UUID userId, Instant expiresAt, boolean revoked) {}
}
