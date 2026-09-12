package com.financafacil.domain.port.in;

import com.financafacil.presentation.dto.response.AccountResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountUseCase {
    AccountResponse create(UUID userId, String name, String type, BigDecimal initialBalance, String color);
    List<AccountResponse> findAll(UUID userId);
    AccountResponse update(UUID userId, UUID accountId, String name, String type, String color);
    void delete(UUID userId, UUID accountId);
    BigDecimal getBalance(UUID userId, UUID accountId);
}
