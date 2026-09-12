package com.financafacil.application.usecase;

import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Budget;
import com.financafacil.domain.port.in.BudgetUseCase;
import com.financafacil.domain.port.out.BudgetRepository;
import com.financafacil.domain.port.out.CategoryRepository;
import com.financafacil.presentation.dto.response.BudgetResponse;
import com.financafacil.presentation.mapper.BudgetPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BudgetUseCaseImpl implements BudgetUseCase {
    private final BudgetRepository budgetRepository;
    private final BudgetPresentationMapper presentationMapper;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public BudgetResponse create(UUID userId, UUID categoryId, BigDecimal amount, int month, int year) {
        if (!categoryRepository.existsByIdAndUserId(categoryId, userId)) {
            throw new NotFoundException("Categoria não encontrada");
        }
        if (budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, month, year)) {
            throw new ConflictException("Já existe um orçamento para esta categoria neste mês/ano");
        }
        var budget = budgetRepository.save(Budget.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .categoryId(categoryId)
            .amount(amount)
            .month(month)
            .year(year)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build());
        return presentationMapper.toResponse(budget);
    }

    @Override
    public List<BudgetResponse> findAll(UUID userId) {
        return budgetRepository.findAllByUserId(userId).stream()
            .map(presentationMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BudgetResponse update(UUID userId, UUID budgetId, BigDecimal amount) {
        var budget = budgetRepository.findById(budgetId, userId)
            .orElseThrow(() -> new NotFoundException("Orçamento não encontrado"));
        var updated = budgetRepository.save(budget
            .withAmount(amount)
            .withUpdatedAt(Instant.now()));
        return presentationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID budgetId) {
        if (budgetRepository.findById(budgetId, userId).isEmpty()) {
            throw new NotFoundException("Orçamento não encontrado");
        }
        budgetRepository.deleteById(budgetId, userId);
    }
}
