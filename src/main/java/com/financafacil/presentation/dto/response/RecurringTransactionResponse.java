package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class RecurringTransactionResponse {
    private final UUID id;
    private final UUID accountId;
    private final UUID categoryId;
    private final BigDecimal amount;
    private final String type;
    private final String frequency;
    private final LocalDate nextExecution;
    private final boolean active;
    private final Instant createdAt;
    private final Instant updatedAt;
}
