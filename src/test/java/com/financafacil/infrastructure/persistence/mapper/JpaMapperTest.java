package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.*;
import com.financafacil.infrastructure.persistence.entity.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MapStruct-generated JPA mapper implementations.
 * These tests instantiate the generated Impl classes directly to avoid Spring context overhead.
 */
class JpaMapperTest {

    private final AccountJpaMapper accountJpaMapper = new AccountJpaMapperImpl();
    private final TransactionJpaMapper transactionJpaMapper = new TransactionJpaMapperImpl();
    private final BudgetJpaMapper budgetJpaMapper = new BudgetJpaMapperImpl();
    private final CategoryJpaMapper categoryJpaMapper = new CategoryJpaMapperImpl();
    private final TransferJpaMapper transferJpaMapper = new TransferJpaMapperImpl();
    private final RecurringTransactionJpaMapper recurringJpaMapper = new RecurringTransactionJpaMapperImpl();
    private final UserJpaMapper userJpaMapper = new UserJpaMapperImpl();

    // ===== Account =====

    @Test
    void accountMapper_toEntity_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var now = Instant.now();
        var account = Account.builder()
            .id(id).userId(userId).name("Nubank").type("checking")
            .balance(BigDecimal.valueOf(1500)).color("#8B5CF6")
            .createdAt(now).updatedAt(now).build();

        var entity = accountJpaMapper.toEntity(account);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getName()).isEqualTo("Nubank");
        assertThat(entity.getType()).isEqualTo("checking");
        assertThat(entity.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1500));
        assertThat(entity.getColor()).isEqualTo("#8B5CF6");
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void accountMapper_toDomain_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var now = Instant.now();
        var entity = AccountJpaEntity.builder()
            .id(id).userId(userId).name("Itaú").type("savings")
            .balance(BigDecimal.valueOf(500)).color("#000000")
            .createdAt(now).updatedAt(now).build();

        var account = accountJpaMapper.toDomain(entity);

        assertThat(account).isNotNull();
        assertThat(account.getId()).isEqualTo(id);
        assertThat(account.getUserId()).isEqualTo(userId);
        assertThat(account.getName()).isEqualTo("Itaú");
        assertThat(account.getType()).isEqualTo("savings");
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(account.getColor()).isEqualTo("#000000");
    }

    @Test
    void accountMapper_toEntity_nullReturnsNull() {
        assertThat(accountJpaMapper.toEntity(null)).isNull();
    }

    @Test
    void accountMapper_toDomain_nullReturnsNull() {
        assertThat(accountJpaMapper.toDomain(null)).isNull();
    }

    // ===== Transaction =====

    @Test
    void transactionMapper_toEntity_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var accountId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var now = Instant.now();
        var date = LocalDate.now();
        var transaction = Transaction.builder()
            .id(id).userId(userId).accountId(accountId).categoryId(categoryId)
            .type("EXPENSE").amount(BigDecimal.valueOf(200)).description("Supermercado")
            .transactionDate(date).status("CONFIRMED").createdAt(now).updatedAt(now).build();

        var entity = transactionJpaMapper.toEntity(transaction);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getAccountId()).isEqualTo(accountId);
        assertThat(entity.getCategoryId()).isEqualTo(categoryId);
        assertThat(entity.getType()).isEqualTo("EXPENSE");
        assertThat(entity.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(entity.getDescription()).isEqualTo("Supermercado");
        assertThat(entity.getTransactionDate()).isEqualTo(date);
        assertThat(entity.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void transactionMapper_toDomain_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var accountId = UUID.randomUUID();
        var date = LocalDate.now();
        var now = Instant.now();
        var entity = TransactionJpaEntity.builder()
            .id(id).userId(userId).accountId(accountId).categoryId(null)
            .type("INCOME").amount(BigDecimal.valueOf(3000)).description("Salário")
            .transactionDate(date).status("CONFIRMED").createdAt(now).updatedAt(now).build();

        var domain = transactionJpaMapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(id);
        assertThat(domain.getType()).isEqualTo("INCOME");
        assertThat(domain.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(domain.getDescription()).isEqualTo("Salário");
        assertThat(domain.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void transactionMapper_toEntity_nullReturnsNull() {
        assertThat(transactionJpaMapper.toEntity(null)).isNull();
    }

    @Test
    void transactionMapper_toDomain_nullReturnsNull() {
        assertThat(transactionJpaMapper.toDomain(null)).isNull();
    }

    // ===== Budget =====

    @Test
    void budgetMapper_toEntity_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var catId = UUID.randomUUID();
        var now = Instant.now();
        var budget = Budget.builder()
            .id(id).userId(userId).categoryId(catId)
            .amount(BigDecimal.valueOf(1000)).month(6).year(2025)
            .createdAt(now).updatedAt(now).build();

        var entity = budgetJpaMapper.toEntity(budget);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getUserId()).isEqualTo(userId);
        assertThat(entity.getCategoryId()).isEqualTo(catId);
        assertThat(entity.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(entity.getMonth()).isEqualTo((short) 6);
        assertThat(entity.getYear()).isEqualTo((short) 2025);
    }

    @Test
    void budgetMapper_toDomain_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var catId = UUID.randomUUID();
        var now = Instant.now();
        var entity = BudgetJpaEntity.builder()
            .id(id).userId(userId).categoryId(catId)
            .amount(BigDecimal.valueOf(500)).month((short) 3).year((short) 2024)
            .createdAt(now).updatedAt(now).build();

        var domain = budgetJpaMapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getMonth()).isEqualTo(3);
        assertThat(domain.getYear()).isEqualTo(2024);
        assertThat(domain.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void budgetMapper_nullReturnsNull() {
        assertThat(budgetJpaMapper.toEntity(null)).isNull();
        assertThat(budgetJpaMapper.toDomain(null)).isNull();
    }

    // ===== Category =====

    @Test
    void categoryMapper_toEntity_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var now = Instant.now();
        var category = Category.builder()
            .id(id).userId(userId).name("Alimentação").type("EXPENSE")
            .icon("food").color("#FF0000").createdAt(now).updatedAt(now).build();

        var entity = categoryJpaMapper.toEntity(category);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("Alimentação");
        assertThat(entity.getType()).isEqualTo("EXPENSE");
        assertThat(entity.getIcon()).isEqualTo("food");
        assertThat(entity.getColor()).isEqualTo("#FF0000");
    }

    @Test
    void categoryMapper_toDomain_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var now = Instant.now();
        var entity = CategoryJpaEntity.builder()
            .id(id).userId(userId).name("Transporte").type("EXPENSE")
            .icon("car").color("#0000FF").createdAt(now).updatedAt(now).build();

        var domain = categoryJpaMapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getName()).isEqualTo("Transporte");
        assertThat(domain.getIcon()).isEqualTo("car");
        assertThat(domain.getColor()).isEqualTo("#0000FF");
    }

    @Test
    void categoryMapper_nullReturnsNull() {
        assertThat(categoryJpaMapper.toEntity(null)).isNull();
        assertThat(categoryJpaMapper.toDomain(null)).isNull();
    }

    // ===== Transfer =====

    @Test
    void transferMapper_toEntity_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var fromId = UUID.randomUUID();
        var toId = UUID.randomUUID();
        var date = LocalDate.now();
        var now = Instant.now();
        var transfer = Transfer.builder()
            .id(id).userId(userId).fromAccountId(fromId).toAccountId(toId)
            .amount(BigDecimal.valueOf(300)).transferDate(date).description("PIX")
            .createdAt(now).build();

        var entity = transferJpaMapper.toEntity(transfer);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getFromAccountId()).isEqualTo(fromId);
        assertThat(entity.getToAccountId()).isEqualTo(toId);
        assertThat(entity.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(300));
        assertThat(entity.getDescription()).isEqualTo("PIX");
        assertThat(entity.getTransferDate()).isEqualTo(date);
    }

    @Test
    void transferMapper_toDomain_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var fromId = UUID.randomUUID();
        var toId = UUID.randomUUID();
        var date = LocalDate.now();
        var now = Instant.now();
        var entity = TransferJpaEntity.builder()
            .id(id).userId(userId).fromAccountId(fromId).toAccountId(toId)
            .amount(BigDecimal.valueOf(100)).transferDate(date).description("TED")
            .createdAt(now).build();

        var domain = transferJpaMapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getFromAccountId()).isEqualTo(fromId);
        assertThat(domain.getToAccountId()).isEqualTo(toId);
        assertThat(domain.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(domain.getDescription()).isEqualTo("TED");
    }

    @Test
    void transferMapper_nullReturnsNull() {
        assertThat(transferJpaMapper.toEntity(null)).isNull();
        assertThat(transferJpaMapper.toDomain(null)).isNull();
    }

    // ===== RecurringTransaction =====

    @Test
    void recurringTransactionMapper_toEntity_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var accountId = UUID.randomUUID();
        var catId = UUID.randomUUID();
        var date = LocalDate.now();
        var now = Instant.now();
        var rt = RecurringTransaction.builder()
            .id(id).userId(userId).accountId(accountId).categoryId(catId)
            .amount(BigDecimal.valueOf(50)).type("EXPENSE").frequency("MONTHLY")
            .nextExecution(date).active(true).createdAt(now).updatedAt(now).build();

        var entity = recurringJpaMapper.toEntity(rt);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getAccountId()).isEqualTo(accountId);
        assertThat(entity.getCategoryId()).isEqualTo(catId);
        assertThat(entity.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        assertThat(entity.getType()).isEqualTo("EXPENSE");
        assertThat(entity.getFrequency()).isEqualTo("MONTHLY");
        assertThat(entity.getNextExecution()).isEqualTo(date);
        assertThat(entity.isActive()).isTrue();
    }

    @Test
    void recurringTransactionMapper_toDomain_mapsAllFields() {
        var id = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var accountId = UUID.randomUUID();
        var date = LocalDate.now();
        var now = Instant.now();
        var entity = RecurringTransactionJpaEntity.builder()
            .id(id).userId(userId).accountId(accountId).categoryId(null)
            .amount(BigDecimal.valueOf(200)).type("INCOME").frequency("WEEKLY")
            .nextExecution(date).active(false).createdAt(now).updatedAt(now).build();

        var domain = recurringJpaMapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getType()).isEqualTo("INCOME");
        assertThat(domain.getFrequency()).isEqualTo("WEEKLY");
        assertThat(domain.isActive()).isFalse();
    }

    @Test
    void recurringTransactionMapper_nullReturnsNull() {
        assertThat(recurringJpaMapper.toEntity(null)).isNull();
        assertThat(recurringJpaMapper.toDomain(null)).isNull();
    }

    // ===== User =====

    @Test
    void userMapper_toEntity_mapsAllFields() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        var user = User.builder()
            .id(id).name("João").email("joao@test.com").passwordHash("hash123")
            .createdAt(now).updatedAt(now).lastLoginAt(now)
            .emailVerified(true).active(true).build();

        var entity = userJpaMapper.toEntity(user);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("João");
        assertThat(entity.getEmail()).isEqualTo("joao@test.com");
        assertThat(entity.getPasswordHash()).isEqualTo("hash123");
        assertThat(entity.isEmailVerified()).isTrue();
        assertThat(entity.isActive()).isTrue();
        assertThat(entity.getLastLoginAt()).isEqualTo(now);
    }

    @Test
    void userMapper_toDomain_mapsAllFields() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        var entity = UserJpaEntity.builder()
            .id(id).name("Maria").email("maria@test.com").passwordHash("hash456")
            .createdAt(now).updatedAt(now).lastLoginAt(null)
            .emailVerified(false).active(false).build();

        var domain = userJpaMapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getName()).isEqualTo("Maria");
        assertThat(domain.getEmail()).isEqualTo("maria@test.com");
        assertThat(domain.isEmailVerified()).isFalse();
        assertThat(domain.isActive()).isFalse();
        assertThat(domain.getLastLoginAt()).isNull();
    }

    @Test
    void userMapper_nullReturnsNull() {
        assertThat(userJpaMapper.toEntity(null)).isNull();
        assertThat(userJpaMapper.toDomain(null)).isNull();
    }
}
