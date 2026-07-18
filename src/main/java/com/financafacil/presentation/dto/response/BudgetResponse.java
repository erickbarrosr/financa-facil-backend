package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class BudgetResponse {
    private final UUID id;
    private final UUID categoryId;
    private final BigDecimal amount;
    private final int month;
    private final int year;
    private final Instant createdAt;
    private final Instant updatedAt;
}
