package com.financafacil.domain.port.out;

import com.financafacil.domain.model.Budget;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository {
    Budget save(Budget budget);
    Optional<Budget> findById(UUID id, UUID userId);
    List<Budget> findAllByUserId(UUID userId);
    void deleteById(UUID id, UUID userId);
    boolean existsByUserIdAndCategoryIdAndMonthAndYear(UUID userId, UUID categoryId, int month, int year);
}
