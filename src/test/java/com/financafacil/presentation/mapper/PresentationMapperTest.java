package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.*;
import com.financafacil.presentation.dto.response.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for MapStruct-generated presentation mapper implementations.
 */
class PresentationMapperTest {

    private final AccountPresentationMapper accountMapper = new AccountPresentationMapperImpl();
    private final TransactionPresentationMapper transactionMapper = new TransactionPresentationMapperImpl();
    private final BudgetPresentationMapper budgetMapper = new BudgetPresentationMapperImpl();
    private final CategoryPresentationMapper categoryMapper = new CategoryPresentationMapperImpl();
    private final TransferPresentationMapper transferMapper = new TransferPresentationMapperImpl();
    private final RecurringTransactionPresentationMapper recurringMapper = new RecurringTransactionPresentationMapperImpl();

    // ===== Account =====

    @Test
    void accountMapper_toResponse_mapsAllFields() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        var account = Account.builder()
            .id(id).userId(UUID.randomUUID()).name("Nubank").type("checking")
            .balance(BigDecimal.valueOf(1000)).color("#8B5CF6")
            .createdAt(now).updatedAt(now).build();

        var response = accountMapper.toResponse(account);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Nubank");
        assertThat(response.getType()).isEqualTo("checking");
        assertThat(response.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(response.getColor()).isEqualTo("#8B5CF6");
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void accountMapper_toResponse_nullReturnsNull() {
        assertThat(accountMapper.toResponse(null)).isNull();
    }

    // ===== Transaction =====

    @Test
    void transactionMapper_toResponse_mapsAllFields() {
        var id = UUID.randomUUID();
        var accountId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var date = LocalDate.now();
        var transaction = Transaction.builder()
            .id(id).userId(UUID.randomUUID()).accountId(accountId).categoryId(categoryId)
            .type("EXPENSE").amount(BigDecimal.valueOf(150)).description("Almoço")
            .transactionDate(date).status("CONFIRMED").createdAt(Instant.now()).updatedAt(Instant.now()).build();

        var response = transactionMapper.toResponse(transaction);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getAccountId()).isEqualTo(accountId);
        assertThat(response.getCategoryId()).isEqualTo(categoryId);
        assertThat(response.getType()).isEqualTo("EXPENSE");
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(150));
        assertThat(response.getDescription()).isEqualTo("Almoço");
        assertThat(response.getTransactionDate()).isEqualTo(date);
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void transactionMapper_toResponse_nullReturnsNull() {
        assertThat(transactionMapper.toResponse(null)).isNull();
    }

    // ===== Budget =====

    @Test
    void budgetMapper_toResponse_mapsAllFields() {
        var id = UUID.randomUUID();
        var catId = UUID.randomUUID();
        var now = Instant.now();
        var budget = Budget.builder()
            .id(id).userId(UUID.randomUUID()).categoryId(catId)
            .amount(BigDecimal.valueOf(2000)).month(7).year(2025)
            .createdAt(now).updatedAt(now).build();

        var response = budgetMapper.toResponse(budget);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getCategoryId()).isEqualTo(catId);
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(response.getMonth()).isEqualTo(7);
        assertThat(response.getYear()).isEqualTo(2025);
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void budgetMapper_toResponse_nullReturnsNull() {
        assertThat(budgetMapper.toResponse(null)).isNull();
    }

    // ===== Category =====

    @Test
    void categoryMapper_toResponse_mapsAllFields() {
        var id = UUID.randomUUID();
        var category = Category.builder()
            .id(id).userId(UUID.randomUUID()).name("Lazer").type("EXPENSE")
            .icon("game").color("#ABCDEF").createdAt(Instant.now()).updatedAt(Instant.now()).build();

        var response = categoryMapper.toResponse(category);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getName()).isEqualTo("Lazer");
        assertThat(response.getType()).isEqualTo("EXPENSE");
        assertThat(response.getIcon()).isEqualTo("game");
        assertThat(response.getColor()).isEqualTo("#ABCDEF");
    }

    @Test
    void categoryMapper_toResponse_nullReturnsNull() {
        assertThat(categoryMapper.toResponse(null)).isNull();
    }

    // ===== Transfer =====

    @Test
    void transferMapper_toResponse_mapsAllFields() {
        var id = UUID.randomUUID();
        var fromId = UUID.randomUUID();
        var toId = UUID.randomUUID();
        var date = LocalDate.now();
        var now = Instant.now();
        var transfer = Transfer.builder()
            .id(id).userId(UUID.randomUUID()).fromAccountId(fromId).toAccountId(toId)
            .amount(BigDecimal.valueOf(500)).transferDate(date).description("Reserva de emergência")
            .createdAt(now).build();

        var response = transferMapper.toResponse(transfer);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getFromAccountId()).isEqualTo(fromId);
        assertThat(response.getToAccountId()).isEqualTo(toId);
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(response.getTransferDate()).isEqualTo(date);
        assertThat(response.getDescription()).isEqualTo("Reserva de emergência");
        assertThat(response.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void transferMapper_toResponse_nullReturnsNull() {
        assertThat(transferMapper.toResponse(null)).isNull();
    }

    // ===== RecurringTransaction =====

    @Test
    void recurringTransactionMapper_toResponse_mapsAllFields() {
        var id = UUID.randomUUID();
        var accountId = UUID.randomUUID();
        var catId = UUID.randomUUID();
        var date = LocalDate.now();
        var now = Instant.now();
        var rt = RecurringTransaction.builder()
            .id(id).userId(UUID.randomUUID()).accountId(accountId).categoryId(catId)
            .amount(BigDecimal.valueOf(80)).type("EXPENSE").frequency("MONTHLY")
            .nextExecution(date).active(true).createdAt(now).updatedAt(now).build();

        var response = recurringMapper.toResponse(rt);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getAccountId()).isEqualTo(accountId);
        assertThat(response.getCategoryId()).isEqualTo(catId);
        assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(80));
        assertThat(response.getType()).isEqualTo("EXPENSE");
        assertThat(response.getFrequency()).isEqualTo("MONTHLY");
        assertThat(response.getNextExecution()).isEqualTo(date);
        assertThat(response.isActive()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(now);
        assertThat(response.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    void recurringTransactionMapper_toResponse_nullReturnsNull() {
        assertThat(recurringMapper.toResponse(null)).isNull();
    }
}
