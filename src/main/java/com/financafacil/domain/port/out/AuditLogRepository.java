package com.financafacil.domain.port.out;

import java.util.Map;
import java.util.UUID;

public interface AuditLogRepository {
    void log(UUID userId, String action, String entity, UUID entityId, Map<String, Object> metadata, String ip, String userAgent);
}
