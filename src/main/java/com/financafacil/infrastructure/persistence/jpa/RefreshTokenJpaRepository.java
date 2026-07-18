package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.RefreshTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {
    Optional<RefreshTokenJpaEntity> findByTokenHash(String tokenHash);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE RefreshTokenJpaEntity r SET r.revoked = true WHERE r.userId = :userId")
    void revokeAllByUserId(UUID userId);
}
