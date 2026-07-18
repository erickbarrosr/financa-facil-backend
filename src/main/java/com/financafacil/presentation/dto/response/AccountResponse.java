package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class AccountResponse {
    private final UUID id;
    private final String name;
    private final String type;
    private final BigDecimal balance;
    private final String color;
    private final Instant createdAt;
}
