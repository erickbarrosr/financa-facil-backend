package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.RecurringTransaction;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.domain.port.out.RecurringTransactionRepository;
import com.financafacil.presentation.dto.response.RecurringTransactionResponse;
import com.financafacil.presentation.mapper.RecurringTransactionPresentationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringTransactionUseCaseImplTest {

    @Mock RecurringTransactionRepository recurringTransactionRepository;
    @Mock RecurringTransactionPresentationMapper presentationMapper;
    @Mock AccountRepository accountRepository;
    @InjectMocks RecurringTransactionUseCaseImpl recurringTransactionUseCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID accountId = UUID.randomUUID();

    @Test
    void create_savesRecurringTransaction() {
        var rt = buildRecurring(true);
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);
        when(recurringTransactionRepository.save(any())).thenReturn(rt);
        when(presentationMapper.toResponse(rt)).thenReturn(buildResponse(rt));

        recurringTransactionUseCase.create(userId, accountId, null, BigDecimal.valueOf(100),
            "expense", "monthly", LocalDate.now());

        var captor = ArgumentCaptor.forClass(RecurringTransaction.class);
        verify(recurringTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isTrue();
    }

    @Test
    void findAll_returnsListForUser() {
        var rt = buildRecurring(true);
        when(recurringTransactionRepository.findAllByUserId(userId)).thenReturn(List.of(rt));
        when(presentationMapper.toResponse(rt)).thenReturn(buildResponse(rt));

        var result = recurringTransactionUseCase.findAll(userId);
        assertThat(result).hasSize(1);
    }

    @Test
    void delete_throwsNotFound_whenNotExists() {
        var id = UUID.randomUUID();
        when(recurringTransactionRepository.findById(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recurringTransactionUseCase.delete(userId, id))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_callsRepository_whenExists() {
        var id = UUID.randomUUID();
        var rt = buildRecurring(true);
        when(recurringTransactionRepository.findById(id, userId)).thenReturn(Optional.of(rt));

        recurringTransactionUseCase.delete(userId, id);

        verify(recurringTransactionRepository).deleteById(id, userId);
    }

    @Test
    void toggleActive_switchesActiveFlag_fromTrueToFalse() {
        var id = UUID.randomUUID();
        var rt = buildRecurring(true);
        var toggled = rt.withActive(false).withUpdatedAt(Instant.now());
        when(recurringTransactionRepository.findById(id, userId)).thenReturn(Optional.of(rt));
        when(recurringTransactionRepository.save(any())).thenReturn(toggled);
        when(presentationMapper.toResponse(toggled)).thenReturn(buildResponse(toggled));

        recurringTransactionUseCase.toggleActive(userId, id);

        var captor = ArgumentCaptor.forClass(RecurringTransaction.class);
        verify(recurringTransactionRepository).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
    }

    @Test
    void toggleActive_throwsNotFound_whenNotExists() {
        var id = UUID.randomUUID();
        when(recurringTransactionRepository.findById(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recurringTransactionUseCase.toggleActive(userId, id))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_throwsNotFound_whenAccountNotBelongsToUser() {
        when(accountRepository.existsByIdAndUserId(any(), eq(userId))).thenReturn(false);
        assertThatThrownBy(() -> recurringTransactionUseCase.create(
            userId, UUID.randomUUID(), null, BigDecimal.valueOf(100), "expense", "monthly",
            java.time.LocalDate.now()))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_throwsNotFound_whenNotExists() {
        var id = UUID.randomUUID();
        when(recurringTransactionRepository.findById(id, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recurringTransactionUseCase.update(userId, id, null, null, null, null, null, null))
            .isInstanceOf(NotFoundException.class);
    }

    private RecurringTransaction buildRecurring(boolean active) {
        return RecurringTransaction.builder()
            .id(UUID.randomUUID()).userId(userId).accountId(accountId)
            .amount(BigDecimal.valueOf(100)).type("expense").frequency("monthly")
            .nextExecution(LocalDate.now()).active(active)
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private RecurringTransactionResponse buildResponse(RecurringTransaction rt) {
        return RecurringTransactionResponse.builder()
            .id(rt.getId()).accountId(rt.getAccountId()).categoryId(rt.getCategoryId())
            .amount(rt.getAmount()).type(rt.getType()).frequency(rt.getFrequency())
            .nextExecution(rt.getNextExecution()).active(rt.isActive()).build();
    }
}
