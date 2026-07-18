package com.financafacil.infrastructure.persistence.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLogJpaEntity {
    @Id private UUID id;
    @Column(name = "user_id") private UUID userId;
    @Column(nullable = false, length = 100) private String action;
    @Column(length = 100) private String entity;
    @Column(name = "entity_id") private UUID entityId;
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb") private Map<String, Object> metadata;
    @Column(length = 45) private String ip;
    @Column(name = "user_agent") private String userAgent;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
