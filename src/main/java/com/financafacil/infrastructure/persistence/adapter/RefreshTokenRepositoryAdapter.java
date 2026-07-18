package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.port.out.RefreshTokenRepository;
import com.financafacil.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import com.financafacil.infrastructure.persistence.jpa.RefreshTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {
    private final RefreshTokenJpaRepository jpaRepository;

    @Override
    public void save(UUID id, UUID userId, String tokenHash, Instant expiresAt) {
        jpaRepository.save(RefreshTokenJpaEntity.builder()
            .id(id).userId(userId).tokenHash(tokenHash)
            .expiresAt(expiresAt).revoked(false).createdAt(Instant.now()).build());
    }

    @Override
    public Optional<RefreshTokenData> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash)
            .map(e -> new RefreshTokenData(e.getId(), e.getUserId(), e.getExpiresAt(), e.isRevoked()));
    }

    @Override public void revokeAllByUserId(UUID userId) { jpaRepository.revokeAllByUserId(userId); }
    @Override public void revokeById(UUID id) { jpaRepository.findById(id).ifPresent(e -> { e.setRevoked(true); jpaRepository.save(e); }); }
}
