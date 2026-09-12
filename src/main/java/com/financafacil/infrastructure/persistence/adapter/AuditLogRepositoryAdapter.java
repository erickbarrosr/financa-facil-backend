package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.port.out.AuditLogRepository;
import com.financafacil.infrastructure.persistence.entity.AuditLogJpaEntity;
import com.financafacil.infrastructure.persistence.jpa.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {
    private final AuditLogJpaRepository jpaRepository;

    @Override
    public void log(UUID userId, String action, String entity, UUID entityId, Map<String, Object> metadata, String ip, String userAgent) {
        jpaRepository.save(AuditLogJpaEntity.builder()
            .id(UUID.randomUUID()).userId(userId).action(action)
            .entity(entity).entityId(entityId).metadata(metadata)
            .ip(ip).userAgent(userAgent).createdAt(Instant.now()).build());
    }
}
