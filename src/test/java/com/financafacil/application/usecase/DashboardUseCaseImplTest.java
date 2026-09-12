package com.financafacil.application.usecase;

import com.financafacil.domain.model.*;
import com.financafacil.domain.port.out.*;
import com.financafacil.presentation.dto.response.TransactionResponse;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardUseCaseImplTest {
    @Mock TransactionRepository transactionRepository;
    @Mock AccountRepository accountRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock TransactionPresentationMapper presentationMapper;
    @InjectMocks DashboardUseCaseImpl dashboardUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void getSummary_calculatesCorrectTotals() {
        var now = LocalDate.now();
        var start = now.withDayOfMonth(1);
        var end = now.withDayOfMonth(now.lengthOfMonth());
        var income = buildTx("income", BigDecimal.valueOf(5000));
        var expense = buildTx("expense", BigDecimal.valueOf(2000));
        when(transactionRepository.findByUserIdAndDateBetween(userId, start, end))
            .thenReturn(List.of(income, expense));
        when(accountRepository.findAllByUserId(userId)).thenReturn(
            List.of(Account.builder().id(UUID.randomUUID()).userId(userId).name("A")
                .type("checking").balance(BigDecimal.valueOf(3000)).color("#000")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build()));

        var result = dashboardUseCase.getSummary(userId, now.getYear(), now.getMonthValue());

        assertThat(result.getMonthIncome()).isEqualByComparingTo(BigDecimal.valueOf(5000));
        assertThat(result.getMonthExpense()).isEqualByComparingTo(BigDecimal.valueOf(2000));
        assertThat(result.getTotalBalance()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    void getMonthlySeries_returnsCorrectNumberOfMonths() {
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
            .thenReturn(List.of());
        var result = dashboardUseCase.getMonthlySeries(userId, 6);
        assertThat(result).hasSize(6);
    }

    @Test
    void getMonthlySeries_aggregatesIncomeAndExpensePerMonth() {
        var now = LocalDate.now();
        var income = buildTx("income", BigDecimal.valueOf(1000));
        var expense = buildTx("expense", BigDecimal.valueOf(500));
        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
            .thenReturn(List.of(income, expense));

        var result = dashboardUseCase.getMonthlySeries(userId, 3);

        assertThat(result).hasSize(3);
        result.forEach(r -> {
            assertThat(r.getIncome()).isEqualByComparingTo(BigDecimal.valueOf(1000));
            assertThat(r.getExpense()).isEqualByComparingTo(BigDecimal.valueOf(500));
        });
    }

    @Test
    void getByCategory_groupsTransactionsByCategory() {
        var catId = UUID.randomUUID();
        var tx1 = buildTxWithCategory("expense", BigDecimal.valueOf(300), catId);
        var tx2 = buildTxWithCategory("expense", BigDecimal.valueOf(200), catId);
        var now = LocalDate.now();
        var start = now.withDayOfMonth(1);
        var end = now.withDayOfMonth(now.lengthOfMonth());

        when(transactionRepository.findByUserIdAndDateBetween(userId, start, end))
            .thenReturn(List.of(tx1, tx2));
        when(categoryRepository.findAllByUserId(userId)).thenReturn(
            List.of(Category.builder().id(catId).userId(userId).name("Food")
                .type("expense").icon("food").color("#FF0000")
                .createdAt(Instant.now()).updatedAt(Instant.now()).build()));

        var result = dashboardUseCase.getByCategory(userId, now.getYear(), now.getMonthValue(), "expense");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategoryId()).isEqualTo(catId);
        assertThat(result.get(0).getCategoryName()).isEqualTo("Food");
        assertThat(result.get(0).getTotal()).isEqualByComparingTo(BigDecimal.valueOf(500));
    }

    @Test
    void getRecentTransactions_returnsLimitedAndSortedResults() {
        var tx1 = buildTxWithDate("expense", BigDecimal.valueOf(100), LocalDate.now().minusDays(1));
        var tx2 = buildTxWithDate("income", BigDecimal.valueOf(200), LocalDate.now());
        var tx3 = buildTxWithDate("expense", BigDecimal.valueOf(50), LocalDate.now().minusDays(2));

        when(transactionRepository.findByUserIdAndDateBetween(eq(userId), any(), any()))
            .thenReturn(List.of(tx1, tx2, tx3));

        var mockResponse = TransactionResponse.builder()
            .id(tx2.getId()).accountId(tx2.getAccountId())
            .type("income").amount(BigDecimal.valueOf(200))
            .transactionDate(LocalDate.now()).status("PAID")
            .build();
        when(presentationMapper.toResponse(any())).thenReturn(mockResponse);

        var result = dashboardUseCase.getRecentTransactions(userId, 2);

        assertThat(result).hasSize(2);
    }

    @Test
    void getSummary_returnsZeroWhenNoTransactions() {
        var now = LocalDate.now();
        var start = now.withDayOfMonth(1);
        var end = now.withDayOfMonth(now.lengthOfMonth());

        when(transactionRepository.findByUserIdAndDateBetween(userId, start, end))
            .thenReturn(List.of());
        when(accountRepository.findAllByUserId(userId)).thenReturn(List.of());

        var result = dashboardUseCase.getSummary(userId, now.getYear(), now.getMonthValue());

        assertThat(result.getMonthIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getMonthExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.getTotalBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private Transaction buildTx(String type, BigDecimal amount) {
        return Transaction.builder().id(UUID.randomUUID()).userId(userId)
            .accountId(UUID.randomUUID()).type(type).amount(amount)
            .transactionDate(LocalDate.now()).status("PAID")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private Transaction buildTxWithCategory(String type, BigDecimal amount, UUID categoryId) {
        return Transaction.builder().id(UUID.randomUUID()).userId(userId)
            .accountId(UUID.randomUUID()).categoryId(categoryId).type(type).amount(amount)
            .transactionDate(LocalDate.now()).status("PAID")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    private Transaction buildTxWithDate(String type, BigDecimal amount, LocalDate date) {
        return Transaction.builder().id(UUID.randomUUID()).userId(userId)
            .accountId(UUID.randomUUID()).type(type).amount(amount)
            .transactionDate(date).status("PAID")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
