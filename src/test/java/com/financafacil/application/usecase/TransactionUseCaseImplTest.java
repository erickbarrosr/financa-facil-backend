package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Account;
import com.financafacil.domain.model.Transaction;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.domain.port.out.CategoryRepository;
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
    @Mock CategoryRepository categoryRepository;
    @Mock TransactionPresentationMapper presentationMapper;
    @InjectMocks TransactionUseCaseImpl transactionUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void create_updatesAccountBalance_forIncome() {
        var accountId = UUID.randomUUID();
        var savedTx = buildTransaction(accountId, "income", BigDecimal.valueOf(500));
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);
        when(transactionRepository.save(any())).thenReturn(savedTx);

        transactionUseCase.create(userId, accountId, null, "income", BigDecimal.valueOf(500), null, LocalDate.now(), "PAID");

        // Atomic delta: +500 for income
        verify(accountRepository).adjustBalance(accountId, BigDecimal.valueOf(500));
    }

    @Test
    void create_updatesAccountBalance_forExpense() {
        var accountId = UUID.randomUUID();
        var savedTx = buildTransaction(accountId, "expense", BigDecimal.valueOf(300));
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);
        when(transactionRepository.save(any())).thenReturn(savedTx);

        transactionUseCase.create(userId, accountId, null, "expense", BigDecimal.valueOf(300), null, LocalDate.now(), "PAID");

        // Atomic delta: -300 for expense
        verify(accountRepository).adjustBalance(accountId, BigDecimal.valueOf(300).negate());
    }

    @Test
    void create_throwsNotFound_whenAccountNotBelongsToUser() {
        var accountId = UUID.randomUUID();
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(false);

        assertThatThrownBy(() -> transactionUseCase.create(userId, accountId, null, "expense",
            BigDecimal.valueOf(100), null, LocalDate.now(), "PAID"))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void create_throwsNotFound_whenCategoryNotBelongsToUser() {
        var accountId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);
        when(categoryRepository.existsByIdAndUserId(categoryId, userId)).thenReturn(false);

        assertThatThrownBy(() -> transactionUseCase.create(userId, accountId, categoryId, "expense",
            BigDecimal.valueOf(100), null, LocalDate.now(), "PAID"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Categoria");
    }

    @Test
    void update_throwsNotFound_whenCategoryNotBelongsToUser() {
        var accountId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var txId = UUID.randomUUID();
        var existing = buildTransactionWithId(txId, accountId, "income", BigDecimal.valueOf(100));
        when(transactionRepository.findById(txId, userId)).thenReturn(Optional.of(existing));
        when(categoryRepository.existsByIdAndUserId(categoryId, userId)).thenReturn(false);

        assertThatThrownBy(() -> transactionUseCase.update(userId, txId, accountId, categoryId, "income",
            BigDecimal.valueOf(100), null, LocalDate.now(), "PAID"))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("Categoria");
    }

    @Test
    void delete_revertsAccountBalance() {
        var accountId = UUID.randomUUID();
        var txId = UUID.randomUUID();
        // Original transaction: expense of 200 (balance was reduced by 200)
        var tx = buildTransaction(accountId, "expense", BigDecimal.valueOf(200));
        when(transactionRepository.findById(txId, userId)).thenReturn(Optional.of(tx));

        transactionUseCase.delete(userId, txId);

        // Reverting an expense adds back the amount (+200 delta)
        verify(accountRepository).adjustBalance(accountId, BigDecimal.valueOf(200));
        verify(transactionRepository).deleteById(txId, userId);
    }

    @Test
    void delete_revertsIncomeBalance() {
        var accountId = UUID.randomUUID();
        var txId = UUID.randomUUID();
        // Original transaction: income of 500 (balance was increased by 500)
        var tx = buildTransaction(accountId, "income", BigDecimal.valueOf(500));
        when(transactionRepository.findById(txId, userId)).thenReturn(Optional.of(tx));

        transactionUseCase.delete(userId, txId);

        // Reverting an income subtracts the amount (-500 delta)
        verify(accountRepository).adjustBalance(accountId, BigDecimal.valueOf(500).negate());
        verify(transactionRepository).deleteById(txId, userId);
    }

    @Test
    void delete_throwsNotFound_whenTransactionNotFound() {
        var txId = UUID.randomUUID();
        when(transactionRepository.findById(txId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionUseCase.delete(userId, txId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_sameAccount_revertsAndAppliesNewBalance() {
        var accountId = UUID.randomUUID();
        var txId = UUID.randomUUID();
        // Existing: income 300 on same account
        var existing = buildTransactionWithId(txId, accountId, "income", BigDecimal.valueOf(300));
        var updated = buildTransactionWithId(txId, accountId, "expense", BigDecimal.valueOf(200));
        when(transactionRepository.findById(txId, userId)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any())).thenReturn(updated);

        transactionUseCase.update(userId, txId, accountId, null, "expense", BigDecimal.valueOf(200),
            null, LocalDate.now(), "PAID");

        // Revert old income: -300
        verify(accountRepository).adjustBalance(accountId, BigDecimal.valueOf(300).negate());
        // Apply new expense: -200
        verify(accountRepository).adjustBalance(accountId, BigDecimal.valueOf(200).negate());
    }

    @Test
    void update_differentAccount_revertsOldAndAppliesNewAccount() {
        var oldAccountId = UUID.randomUUID();
        var newAccountId = UUID.randomUUID();
        var txId = UUID.randomUUID();
        var existing = buildTransactionWithId(txId, oldAccountId, "income", BigDecimal.valueOf(400));
        var updated = buildTransactionWithId(txId, newAccountId, "income", BigDecimal.valueOf(400));
        when(transactionRepository.findById(txId, userId)).thenReturn(Optional.of(existing));
        when(accountRepository.existsByIdAndUserId(newAccountId, userId)).thenReturn(true);
        when(transactionRepository.save(any())).thenReturn(updated);

        transactionUseCase.update(userId, txId, newAccountId, null, "income", BigDecimal.valueOf(400),
            null, LocalDate.now(), "PAID");

        // Revert old income on old account: -400
        verify(accountRepository).adjustBalance(oldAccountId, BigDecimal.valueOf(400).negate());
        // Apply new income on new account: +400
        verify(accountRepository).adjustBalance(newAccountId, BigDecimal.valueOf(400));
    }

    @Test
    void update_differentAccount_throwsNotFound_whenNewAccountNotBelongsToUser() {
        var oldAccountId = UUID.randomUUID();
        var newAccountId = UUID.randomUUID();
        var txId = UUID.randomUUID();
        var existing = buildTransactionWithId(txId, oldAccountId, "income", BigDecimal.valueOf(100));
        when(transactionRepository.findById(txId, userId)).thenReturn(Optional.of(existing));
        when(accountRepository.existsByIdAndUserId(newAccountId, userId)).thenReturn(false);

        assertThatThrownBy(() -> transactionUseCase.update(userId, txId, newAccountId, null, "income",
            BigDecimal.valueOf(100), null, LocalDate.now(), "PAID"))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_throwsNotFound_whenTransactionNotFound() {
        var txId = UUID.randomUUID();
        var accountId = UUID.randomUUID();
        when(transactionRepository.findById(txId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transactionUseCase.update(userId, txId, accountId, null, "income",
            BigDecimal.valueOf(100), null, LocalDate.now(), "PAID"))
            .isInstanceOf(NotFoundException.class);
    }

    // --- helpers ---

    private Account buildAccount(UUID id, BigDecimal balance) {
        return Account.builder().id(id).userId(userId).name("Test").type("checking")
            .balance(balance).color("#000").createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private Transaction buildTransaction(UUID accountId, String type, BigDecimal amount) {
        return Transaction.builder().id(UUID.randomUUID()).userId(userId).accountId(accountId)
            .type(type).amount(amount).transactionDate(LocalDate.now()).status("PAID")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private Transaction buildTransactionWithId(UUID id, UUID accountId, String type, BigDecimal amount) {
        return Transaction.builder().id(id).userId(userId).accountId(accountId)
            .type(type).amount(amount).transactionDate(LocalDate.now()).status("PAID")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
