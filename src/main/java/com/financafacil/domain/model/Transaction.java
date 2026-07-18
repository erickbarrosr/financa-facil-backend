package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Builder @With
public class Transaction {
    private final UUID id;
    private final UUID userId;
    private final UUID accountId;
    private final UUID categoryId;
    private final String type;
    private final BigDecimal amount;
    private final String description;
    private final LocalDate transactionDate;
    private final String status;
    private final Instant createdAt;
    private final Instant updatedAt;
}
