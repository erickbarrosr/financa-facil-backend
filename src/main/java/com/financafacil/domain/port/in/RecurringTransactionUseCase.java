package com.financafacil.domain.port.in;

import com.financafacil.presentation.dto.response.RecurringTransactionResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RecurringTransactionUseCase {
    RecurringTransactionResponse create(UUID userId, UUID accountId, UUID categoryId, BigDecimal amount,
                                        String type, String frequency, LocalDate nextExecution);
    List<RecurringTransactionResponse> findAll(UUID userId);
    RecurringTransactionResponse update(UUID userId, UUID id, UUID accountId, UUID categoryId,
                                        BigDecimal amount, String type, String frequency, LocalDate nextExecution);
    void delete(UUID userId, UUID id);
    RecurringTransactionResponse toggleActive(UUID userId, UUID id);
}
