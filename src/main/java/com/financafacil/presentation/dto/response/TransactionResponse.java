package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Builder
public class TransactionResponse {
    private final UUID id;
    private final UUID accountId;
    private final UUID categoryId;
    private final String type;
    private final BigDecimal amount;
    private final String description;
    private final LocalDate transactionDate;
    private final String status;
}
