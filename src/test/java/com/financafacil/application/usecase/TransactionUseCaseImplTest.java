package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Account;
import com.financafacil.domain.model.Transaction;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.domain.port.out.TransactionRepository;
import com.financafacil.presentation.mapper.TransactionPresentationMapper;
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
class TransactionUseCaseImplTest {
    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository accountRepository;
    @Mock TransactionPresentationMapper presentationMapper;
    @InjectMocks TransactionUseCaseImpl transactionUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void create_updatesAccountBalance_forIncome() {
        var accountId = UUID.randomUUID();
        var account = buildAccount(accountId, BigDecimal.valueOf(1000));
        var savedTx = buildTransaction(accountId, "income", BigDecimal.valueOf(500));
        when(accountRepository.findById(accountId, userId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenReturn(savedTx);

        transactionUseCase.create(userId, accountId, null, "income", BigDecimal.valueOf(500), null, LocalDate.now(), "PAID");

        verify(accountRepository).updateBalance(accountId, BigDecimal.valueOf(1500));
    }

    @Test
    void create_updatesAccountBalance_forExpense() {
        var accountId = UUID.randomUUID();
        var account = buildAccount(accountId, BigDecimal.valueOf(1000));
        var savedTx = buildTransaction(accountId, "expense", BigDecimal.valueOf(300));
        when(accountRepository.findById(accountId, userId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any())).thenReturn(savedTx);

        transactionUseCase.create(userId, accountId, null, "expense", BigDecimal.valueOf(300), null, LocalDate.now(), "PAID");

        verify(accountRepository).updateBalance(accountId, BigDecimal.valueOf(700));
    }

    @Test
    void create_throwsNotFound_whenAccountNotBelongsToUser() {
        var accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionUseCase.create(userId, accountId, null, "expense",
            BigDecimal.valueOf(100), null, LocalDate.now(), "PAID"))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_revertsAccountBalance() {
        var accountId = UUID.randomUUID();
        var txId = UUID.randomUUID();
        var tx = buildTransaction(accountId, "expense", BigDecimal.valueOf(200));
        var account = buildAccount(accountId, BigDecimal.valueOf(800));
        when(transactionRepository.findById(txId, userId)).thenReturn(Optional.of(tx));
        when(accountRepository.findById(accountId, userId)).thenReturn(Optional.of(account));

        transactionUseCase.delete(userId, txId);

        verify(accountRepository).updateBalance(accountId, BigDecimal.valueOf(1000));
        verify(transactionRepository).deleteById(txId, userId);
    }

    private Account buildAccount(UUID id, BigDecimal balance) {
        return Account.builder().id(id).userId(userId).name("Test").type("checking")
            .balance(balance).color("#000").createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private Transaction buildTransaction(UUID accountId, String type, BigDecimal amount) {
        return Transaction.builder().id(UUID.randomUUID()).userId(userId).accountId(accountId)
            .type(type).amount(amount).transactionDate(LocalDate.now()).status("PAID")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
