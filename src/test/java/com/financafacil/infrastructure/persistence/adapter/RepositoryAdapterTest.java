package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.*;
import com.financafacil.domain.port.out.RefreshTokenRepository.RefreshTokenData;
import com.financafacil.domain.port.out.EmailVerificationTokenRepository.TokenData;
import com.financafacil.infrastructure.persistence.entity.*;
import com.financafacil.infrastructure.persistence.jpa.*;
import com.financafacil.infrastructure.persistence.mapper.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RepositoryAdapterTest {

    // ===== AccountRepositoryAdapter =====

    @Mock AccountJpaRepository accountJpaRepo;
    @Mock AccountJpaMapper accountJpaMapper;
    @InjectMocks AccountRepositoryAdapter accountAdapter;

    @Test
    void accountAdapter_save_mapsAndSaves() {
        var account = buildAccount();
        var entity = new AccountJpaEntity();
        when(accountJpaMapper.toEntity(account)).thenReturn(entity);
        when(accountJpaRepo.save(entity)).thenReturn(entity);
        when(accountJpaMapper.toDomain(entity)).thenReturn(account);

        var result = accountAdapter.save(account);

        assertThat(result).isEqualTo(account);
        verify(accountJpaRepo).save(entity);
    }

    @Test
    void accountAdapter_findById_returnsEmpty_whenNotFound() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(accountJpaRepo.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        var result = accountAdapter.findById(id, userId);

        assertThat(result).isEmpty();
    }

    @Test
    void accountAdapter_findById_returnsMapped_whenFound() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var entity = new AccountJpaEntity();
        var account = buildAccount();
        when(accountJpaRepo.findByIdAndUserId(id, userId)).thenReturn(Optional.of(entity));
        when(accountJpaMapper.toDomain(entity)).thenReturn(account);

        var result = accountAdapter.findById(id, userId);

        assertThat(result).contains(account);
    }

    @Test
    void accountAdapter_findAllByUserId_returnsAll() {
        var userId = UUID.randomUUID();
        var entity = new AccountJpaEntity();
        var account = buildAccount();
        when(accountJpaRepo.findAllByUserId(userId)).thenReturn(List.of(entity));
        when(accountJpaMapper.toDomain(entity)).thenReturn(account);

        var result = accountAdapter.findAllByUserId(userId);

        assertThat(result).containsExactly(account);
    }

    @Test
    void accountAdapter_existsByIdAndUserId() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(accountJpaRepo.existsByIdAndUserId(id, userId)).thenReturn(true);

        assertThat(accountAdapter.existsByIdAndUserId(id, userId)).isTrue();
    }

    @Test
    void accountAdapter_hasTransactions() {
        var accountId = UUID.randomUUID();
        when(accountJpaRepo.hasTransactions(accountId)).thenReturn(false);

        assertThat(accountAdapter.hasTransactions(accountId)).isFalse();
    }

    private Account buildAccount() {
        return Account.builder().id(UUID.randomUUID()).userId(UUID.randomUUID())
            .name("Test").type("checking").balance(BigDecimal.ZERO).color("#000")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    // ===== CategoryRepositoryAdapter =====

    @Mock CategoryJpaRepository categoryJpaRepo;
    @Mock CategoryJpaMapper categoryJpaMapper;
    @InjectMocks CategoryRepositoryAdapter categoryAdapter;

    @Test
    void categoryAdapter_save_mapsAndSaves() {
        var category = buildCategory();
        var entity = new CategoryJpaEntity();
        when(categoryJpaMapper.toEntity(category)).thenReturn(entity);
        when(categoryJpaRepo.save(entity)).thenReturn(entity);
        when(categoryJpaMapper.toDomain(entity)).thenReturn(category);

        var result = categoryAdapter.save(category);

        assertThat(result).isEqualTo(category);
    }

    @Test
    void categoryAdapter_findById_returnsEmpty_whenNotFound() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(categoryJpaRepo.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThat(categoryAdapter.findById(id, userId)).isEmpty();
    }

    @Test
    void categoryAdapter_findAllByUserId_returnsList() {
        var userId = UUID.randomUUID();
        var entity = new CategoryJpaEntity();
        var category = buildCategory();
        when(categoryJpaRepo.findAllByUserId(userId)).thenReturn(List.of(entity));
        when(categoryJpaMapper.toDomain(entity)).thenReturn(category);

        var result = categoryAdapter.findAllByUserId(userId);

        assertThat(result).containsExactly(category);
    }

    @Test
    void categoryAdapter_findAllByUserIdAndType_returnsList() {
        var userId = UUID.randomUUID();
        var entity = new CategoryJpaEntity();
        var category = buildCategory();
        when(categoryJpaRepo.findAllByUserIdAndType(userId, "EXPENSE")).thenReturn(List.of(entity));
        when(categoryJpaMapper.toDomain(entity)).thenReturn(category);

        var result = categoryAdapter.findAllByUserIdAndType(userId, "EXPENSE");

        assertThat(result).containsExactly(category);
    }

    @Test
    void categoryAdapter_existsByIdAndUserId() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(categoryJpaRepo.existsByIdAndUserId(id, userId)).thenReturn(true);

        assertThat(categoryAdapter.existsByIdAndUserId(id, userId)).isTrue();
    }

    private Category buildCategory() {
        return Category.builder().id(UUID.randomUUID()).userId(UUID.randomUUID())
            .name("Food").type("EXPENSE").icon("food").color("#FF0000")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    // ===== BudgetRepositoryAdapter =====

    @Mock BudgetJpaRepository budgetJpaRepo;
    @Mock BudgetJpaMapper budgetJpaMapper;
    @InjectMocks BudgetRepositoryAdapter budgetAdapter;

    @Test
    void budgetAdapter_save_mapsAndSaves() {
        var budget = buildBudget();
        var entity = new BudgetJpaEntity();
        when(budgetJpaMapper.toEntity(budget)).thenReturn(entity);
        when(budgetJpaRepo.save(entity)).thenReturn(entity);
        when(budgetJpaMapper.toDomain(entity)).thenReturn(budget);

        var result = budgetAdapter.save(budget);

        assertThat(result).isEqualTo(budget);
    }

    @Test
    void budgetAdapter_findById_returnsEmpty_whenNotFound() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(budgetJpaRepo.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThat(budgetAdapter.findById(id, userId)).isEmpty();
    }

    @Test
    void budgetAdapter_findAllByUserId_returnsList() {
        var userId = UUID.randomUUID();
        var entity = new BudgetJpaEntity();
        var budget = buildBudget();
        when(budgetJpaRepo.findAllByUserId(userId)).thenReturn(List.of(entity));
        when(budgetJpaMapper.toDomain(entity)).thenReturn(budget);

        var result = budgetAdapter.findAllByUserId(userId);

        assertThat(result).containsExactly(budget);
    }

    @Test
    void budgetAdapter_existsByUserIdAndCategoryIdAndMonthAndYear() {
        var userId = UUID.randomUUID();
        var catId = UUID.randomUUID();
        when(budgetJpaRepo.existsByUserIdAndCategoryIdAndMonthAndYear(userId, catId, 6, 2025)).thenReturn(true);

        assertThat(budgetAdapter.existsByUserIdAndCategoryIdAndMonthAndYear(userId, catId, 6, 2025)).isTrue();
    }

    private Budget buildBudget() {
        return Budget.builder().id(UUID.randomUUID()).userId(UUID.randomUUID())
            .categoryId(UUID.randomUUID()).amount(BigDecimal.valueOf(500))
            .month(6).year(2025).createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    // ===== TransferRepositoryAdapter =====

    @Mock TransferJpaRepository transferJpaRepo;
    @Mock TransferJpaMapper transferJpaMapper;
    @InjectMocks TransferRepositoryAdapter transferAdapter;

    @Test
    void transferAdapter_save_mapsAndSaves() {
        var transfer = buildTransfer();
        var entity = new TransferJpaEntity();
        when(transferJpaMapper.toEntity(transfer)).thenReturn(entity);
        when(transferJpaRepo.save(entity)).thenReturn(entity);
        when(transferJpaMapper.toDomain(entity)).thenReturn(transfer);

        var result = transferAdapter.save(transfer);

        assertThat(result).isEqualTo(transfer);
    }

    @Test
    void transferAdapter_findById_returnsEmpty_whenNotFound() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(transferJpaRepo.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThat(transferAdapter.findById(id, userId)).isEmpty();
    }

    @Test
    void transferAdapter_findAllByUserId_returnsList() {
        var userId = UUID.randomUUID();
        var entity = new TransferJpaEntity();
        var transfer = buildTransfer();
        when(transferJpaRepo.findAllByUserId(userId)).thenReturn(List.of(entity));
        when(transferJpaMapper.toDomain(entity)).thenReturn(transfer);

        var result = transferAdapter.findAllByUserId(userId);

        assertThat(result).containsExactly(transfer);
    }

    private Transfer buildTransfer() {
        return Transfer.builder().id(UUID.randomUUID()).userId(UUID.randomUUID())
            .fromAccountId(UUID.randomUUID()).toAccountId(UUID.randomUUID())
            .amount(BigDecimal.valueOf(100)).transferDate(LocalDate.now())
            .description("Test").createdAt(Instant.now()).build();
    }

    // ===== RecurringTransactionRepositoryAdapter =====

    @Mock RecurringTransactionJpaRepository recurringJpaRepo;
    @Mock RecurringTransactionJpaMapper recurringJpaMapper;
    @InjectMocks RecurringTransactionRepositoryAdapter recurringAdapter;

    @Test
    void recurringAdapter_save_mapsAndSaves() {
        var rt = buildRecurring();
        var entity = new RecurringTransactionJpaEntity();
        when(recurringJpaMapper.toEntity(rt)).thenReturn(entity);
        when(recurringJpaRepo.save(entity)).thenReturn(entity);
        when(recurringJpaMapper.toDomain(entity)).thenReturn(rt);

        var result = recurringAdapter.save(rt);

        assertThat(result).isEqualTo(rt);
    }

    @Test
    void recurringAdapter_findById_returnsEmpty_whenNotFound() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(recurringJpaRepo.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThat(recurringAdapter.findById(id, userId)).isEmpty();
    }

    @Test
    void recurringAdapter_findAllByUserId_returnsList() {
        var userId = UUID.randomUUID();
        var entity = new RecurringTransactionJpaEntity();
        var rt = buildRecurring();
        when(recurringJpaRepo.findAllByUserId(userId)).thenReturn(List.of(entity));
        when(recurringJpaMapper.toDomain(entity)).thenReturn(rt);

        var result = recurringAdapter.findAllByUserId(userId);

        assertThat(result).containsExactly(rt);
    }

    private RecurringTransaction buildRecurring() {
        return RecurringTransaction.builder().id(UUID.randomUUID()).userId(UUID.randomUUID())
            .accountId(UUID.randomUUID()).categoryId(UUID.randomUUID())
            .amount(BigDecimal.valueOf(50)).type("EXPENSE").frequency("MONTHLY")
            .nextExecution(LocalDate.now()).active(true)
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    // ===== UserRepositoryAdapter =====

    @Mock com.financafacil.infrastructure.persistence.jpa.UserJpaRepository userJpaRepo;
    @Mock UserJpaMapper userJpaMapper;
    @InjectMocks UserRepositoryAdapter userAdapter;

    @Test
    void userAdapter_save_mapsAndSaves() {
        var user = buildUser();
        var entity = new UserJpaEntity();
        when(userJpaMapper.toEntity(user)).thenReturn(entity);
        when(userJpaRepo.save(entity)).thenReturn(entity);
        when(userJpaMapper.toDomain(entity)).thenReturn(user);

        var result = userAdapter.save(user);

        assertThat(result).isEqualTo(user);
    }

    @Test
    void userAdapter_findByEmail_returnsEmpty_whenNotFound() {
        when(userJpaRepo.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        assertThat(userAdapter.findByEmail("unknown@test.com")).isEmpty();
    }

    @Test
    void userAdapter_findByEmail_returnsMapped_whenFound() {
        var user = buildUser();
        var entity = new UserJpaEntity();
        when(userJpaRepo.findByEmail("test@test.com")).thenReturn(Optional.of(entity));
        when(userJpaMapper.toDomain(entity)).thenReturn(user);

        var result = userAdapter.findByEmail("test@test.com");

        assertThat(result).contains(user);
    }

    @Test
    void userAdapter_findById_returnsMapped_whenFound() {
        var id = UUID.randomUUID();
        var user = buildUser();
        var entity = new UserJpaEntity();
        when(userJpaRepo.findById(id)).thenReturn(Optional.of(entity));
        when(userJpaMapper.toDomain(entity)).thenReturn(user);

        var result = userAdapter.findById(id);

        assertThat(result).contains(user);
    }

    @Test
    void userAdapter_existsByEmail() {
        when(userJpaRepo.existsByEmail("test@test.com")).thenReturn(true);

        assertThat(userAdapter.existsByEmail("test@test.com")).isTrue();
    }

    private User buildUser() {
        return User.builder().id(UUID.randomUUID()).name("Test").email("test@test.com")
            .passwordHash("hash").createdAt(Instant.now()).updatedAt(Instant.now())
            .emailVerified(true).active(true).build();
    }

    // ===== TransactionRepositoryAdapter =====

    @Mock TransactionJpaRepository transactionJpaRepo;
    @Mock TransactionJpaMapper transactionJpaMapper;
    @InjectMocks TransactionRepositoryAdapter transactionAdapter;

    @Test
    void transactionAdapter_save_mapsAndSaves() {
        var transaction = buildTransaction();
        var entity = new TransactionJpaEntity();
        when(transactionJpaMapper.toEntity(transaction)).thenReturn(entity);
        when(transactionJpaRepo.save(entity)).thenReturn(entity);
        when(transactionJpaMapper.toDomain(entity)).thenReturn(transaction);

        var result = transactionAdapter.save(transaction);

        assertThat(result).isEqualTo(transaction);
    }

    @Test
    void transactionAdapter_findById_returnsEmpty_whenNotFound() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(transactionJpaRepo.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        assertThat(transactionAdapter.findById(id, userId)).isEmpty();
    }

    @Test
    void transactionAdapter_findById_returnsMapped_whenFound() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var entity = new TransactionJpaEntity();
        var transaction = buildTransaction();
        when(transactionJpaRepo.findByIdAndUserId(id, userId)).thenReturn(Optional.of(entity));
        when(transactionJpaMapper.toDomain(entity)).thenReturn(transaction);

        var result = transactionAdapter.findById(id, userId);

        assertThat(result).contains(transaction);
    }

    @Test
    void transactionAdapter_findByUserIdAndDateBetween_returnsList() {
        var userId = UUID.randomUUID();
        var start = LocalDate.now().minusDays(30);
        var end = LocalDate.now();
        var entity = new TransactionJpaEntity();
        var transaction = buildTransaction();
        when(transactionJpaRepo.findByUserIdAndDateBetween(userId, start, end)).thenReturn(List.of(entity));
        when(transactionJpaMapper.toDomain(entity)).thenReturn(transaction);

        var result = transactionAdapter.findByUserIdAndDateBetween(userId, start, end);

        assertThat(result).containsExactly(transaction);
    }

    @Test
    void transactionAdapter_findByAccountIdAndDateBetween_returnsList() {
        var accountId = UUID.randomUUID();
        var start = LocalDate.now().minusDays(7);
        var end = LocalDate.now();
        var entity = new TransactionJpaEntity();
        var transaction = buildTransaction();
        when(transactionJpaRepo.findByAccountIdAndDateBetween(accountId, start, end)).thenReturn(List.of(entity));
        when(transactionJpaMapper.toDomain(entity)).thenReturn(transaction);

        var result = transactionAdapter.findByAccountIdAndDateBetween(accountId, start, end);

        assertThat(result).containsExactly(transaction);
    }

    @Test
    void transactionAdapter_deleteById_whenFound_deletes() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var entity = new TransactionJpaEntity();
        when(transactionJpaRepo.findByIdAndUserId(id, userId)).thenReturn(Optional.of(entity));

        transactionAdapter.deleteById(id, userId);

        verify(transactionJpaRepo).delete(entity);
    }

    @Test
    void transactionAdapter_deleteById_whenNotFound_doesNothing() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        when(transactionJpaRepo.findByIdAndUserId(id, userId)).thenReturn(Optional.empty());

        transactionAdapter.deleteById(id, userId);

        verify(transactionJpaRepo, never()).delete(any(TransactionJpaEntity.class));
    }

    private Transaction buildTransaction() {
        return Transaction.builder().id(UUID.randomUUID()).userId(UUID.randomUUID())
            .accountId(UUID.randomUUID()).categoryId(UUID.randomUUID())
            .type("EXPENSE").amount(BigDecimal.valueOf(100)).description("Test")
            .transactionDate(LocalDate.now()).status("CONFIRMED")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
