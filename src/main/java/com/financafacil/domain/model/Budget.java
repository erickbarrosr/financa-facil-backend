package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@With
public class Budget {
    private final UUID id;
    private final UUID userId;
    private final UUID categoryId;
    private final BigDecimal amount;
    private final int month;
    private final int year;
    private final Instant createdAt;
    private final Instant updatedAt;
}
