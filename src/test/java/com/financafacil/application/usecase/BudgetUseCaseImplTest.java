package com.financafacil.application.usecase;

import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Budget;
import com.financafacil.domain.port.out.BudgetRepository;
import com.financafacil.presentation.dto.response.BudgetResponse;
import com.financafacil.presentation.mapper.BudgetPresentationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetUseCaseImplTest {

    @Mock BudgetRepository budgetRepository;
    @Mock BudgetPresentationMapper presentationMapper;
    @InjectMocks BudgetUseCaseImpl budgetUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void create_savesBudget() {
        var categoryId = UUID.randomUUID();
        var budget = buildBudget(categoryId);
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, 1, 2024)).thenReturn(false);
        when(budgetRepository.save(any())).thenReturn(budget);
        when(presentationMapper.toResponse(budget)).thenReturn(buildResponse(budget));

        budgetUseCase.create(userId, categoryId, BigDecimal.valueOf(500), 1, 2024);

        var captor = ArgumentCaptor.forClass(Budget.class);
        verify(budgetRepository).save(captor.capture());
        assertThat(captor.getValue().getCategoryId()).isEqualTo(categoryId);
    }

    @Test
    void create_throwsConflict_whenDuplicateBudget() {
        var categoryId = UUID.randomUUID();
        when(budgetRepository.existsByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, 1, 2024)).thenReturn(true);

        assertThatThrownBy(() -> budgetUseCase.create(userId, categoryId, BigDecimal.valueOf(500), 1, 2024))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void findAll_returnsListForUser() {
        var budget = buildBudget(UUID.randomUUID());
        when(budgetRepository.findAllByUserId(userId)).thenReturn(List.of(budget));
        when(presentationMapper.toResponse(budget)).thenReturn(buildResponse(budget));

        var result = budgetUseCase.findAll(userId);
        assertThat(result).hasSize(1);
    }

    @Test
    void update_throwsNotFound_whenBudgetNotExists() {
        var budgetId = UUID.randomUUID();
        when(budgetRepository.findById(budgetId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetUseCase.update(userId, budgetId, BigDecimal.valueOf(600)))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_throwsNotFound_whenBudgetNotExists() {
        var budgetId = UUID.randomUUID();
        when(budgetRepository.findById(budgetId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetUseCase.delete(userId, budgetId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_callsRepository_whenExists() {
        var budgetId = UUID.randomUUID();
        var budget = buildBudget(UUID.randomUUID());
        when(budgetRepository.findById(budgetId, userId)).thenReturn(Optional.of(budget));

        budgetUseCase.delete(userId, budgetId);

        verify(budgetRepository).deleteById(budgetId, userId);
    }

    private Budget buildBudget(UUID categoryId) {
        return Budget.builder()
            .id(UUID.randomUUID()).userId(userId).categoryId(categoryId)
            .amount(BigDecimal.valueOf(500)).month(1).year(2024)
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private BudgetResponse buildResponse(Budget b) {
        return BudgetResponse.builder()
            .id(b.getId()).categoryId(b.getCategoryId()).amount(b.getAmount())
            .month(b.getMonth()).year(b.getYear()).build();
    }
}
