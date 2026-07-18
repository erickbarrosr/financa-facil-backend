package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.port.out.EmailVerificationTokenRepository;
import com.financafacil.infrastructure.persistence.entity.EmailVerificationTokenJpaEntity;
import com.financafacil.infrastructure.persistence.jpa.EmailVerificationTokenJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EmailVerificationTokenRepositoryAdapter implements EmailVerificationTokenRepository {
    private final EmailVerificationTokenJpaRepository jpaRepository;

    @Override
    public void save(UUID id, UUID userId, String tokenHash, Instant expiresAt) {
        jpaRepository.save(EmailVerificationTokenJpaEntity.builder()
            .id(id).userId(userId).tokenHash(tokenHash)
            .expiresAt(expiresAt).used(false).createdAt(Instant.now()).build());
    }

    @Override
    public Optional<TokenData> findByTokenHash(String tokenHash) {
        return jpaRepository.findByTokenHash(tokenHash)
            .map(e -> new TokenData(e.getId(), e.getUserId(), e.getExpiresAt(), e.isUsed()));
    }

    @Override @Transactional
    public void markAsUsed(UUID id) { jpaRepository.markAsUsed(id); }
}
