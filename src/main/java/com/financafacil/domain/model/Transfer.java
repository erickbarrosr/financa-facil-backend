package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class Transfer {
    private final UUID id;
    private final UUID userId;
    private final UUID fromAccountId;
    private final UUID toAccountId;
    private final BigDecimal amount;
    private final LocalDate transferDate;
    private final String description;
    private final Instant createdAt;
}
