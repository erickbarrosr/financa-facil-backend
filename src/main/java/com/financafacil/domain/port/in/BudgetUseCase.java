package com.financafacil.domain.port.in;

import com.financafacil.presentation.dto.response.BudgetResponse;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BudgetUseCase {
    BudgetResponse create(UUID userId, UUID categoryId, BigDecimal amount, int month, int year);
    List<BudgetResponse> findAll(UUID userId);
    BudgetResponse update(UUID userId, UUID budgetId, BigDecimal amount);
    void delete(UUID userId, UUID budgetId);
}
