package com.financafacil.application.usecase;

import com.financafacil.domain.model.Transaction;
import com.financafacil.domain.port.in.DashboardUseCase;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.domain.port.out.CategoryRepository;
import com.financafacil.domain.port.out.TransactionRepository;
import com.financafacil.presentation.dto.response.CategoryBreakdownResponse;
import com.financafacil.presentation.dto.response.DashboardSummaryResponse;
import com.financafacil.presentation.dto.response.MonthlySeriesResponse;
import com.financafacil.presentation.dto.response.TransactionResponse;
import com.financafacil.presentation.mapper.TransactionPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardUseCaseImpl implements DashboardUseCase {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionPresentationMapper transactionMapper;

    @Override
    public DashboardSummaryResponse getSummary(UUID userId, int year, int month) {
        var start = LocalDate.of(year, month, 1);
        var end = start.withDayOfMonth(start.lengthOfMonth());
        var transactions = transactionRepository.findByUserIdAndDateBetween(userId, start, end);

        var income = transactions.stream()
            .filter(t -> "income".equals(t.getType()))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        var expense = transactions.stream()
            .filter(t -> "expense".equals(t.getType()))
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalBalance = accountRepository.findAllByUserId(userId).stream()
            .map(a -> a.getBalance())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardSummaryResponse.builder()
            .totalBalance(totalBalance)
            .monthIncome(income)
            .monthExpense(expense)
            .year(year)
            .month(month)
            .build();
    }

    @Override
    public List<MonthlySeriesResponse> getMonthlySeries(UUID userId, int months) {
        var result = new ArrayList<MonthlySeriesResponse>();
        var now = LocalDate.now();

        for (int i = months - 1; i >= 0; i--) {
            var date = now.minusMonths(i);
            var start = date.withDayOfMonth(1);
            var end = date.withDayOfMonth(date.lengthOfMonth());
            var txs = transactionRepository.findByUserIdAndDateBetween(userId, start, end);

            var income = txs.stream()
                .filter(t -> "income".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            var expense = txs.stream()
                .filter(t -> "expense".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(MonthlySeriesResponse.builder()
                .label(date.getMonth().getDisplayName(TextStyle.SHORT, new Locale("pt", "BR")))
                .year(date.getYear())
                .month(date.getMonthValue())
                .income(income)
                .expense(expense)
                .build());
        }

        return result;
    }

    @Override
    public List<CategoryBreakdownResponse> getByCategory(UUID userId, int year, int month, String type) {
        var start = LocalDate.of(year, month, 1);
        var end = start.withDayOfMonth(start.lengthOfMonth());

        var transactions = transactionRepository.findByUserIdAndDateBetween(userId, start, end)
            .stream()
            .filter(t -> type.equals(t.getType()) && t.getCategoryId() != null)
            .collect(Collectors.toList());

        var categories = categoryRepository.findAllByUserId(userId).stream()
            .collect(Collectors.toMap(c -> c.getId(), c -> c));

        return transactions.stream()
            .collect(Collectors.groupingBy(
                Transaction::getCategoryId,
                Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)))
            .entrySet().stream()
            .map(e -> {
                var cat = categories.get(e.getKey());
                return CategoryBreakdownResponse.builder()
                    .categoryId(e.getKey())
                    .categoryName(cat != null ? cat.getName() : "Sem categoria")
                    .color(cat != null ? cat.getColor() : "#999999")
                    .total(e.getValue())
                    .build();
            })
            .sorted(Comparator.comparing(CategoryBreakdownResponse::getTotal).reversed())
            .collect(Collectors.toList());
    }

    @Override
    public List<TransactionResponse> getRecentTransactions(UUID userId, int limit) {
        var start = LocalDate.now().minusMonths(36);
        var end = LocalDate.now();
        return transactionRepository.findByUserIdAndDateBetween(userId, start, end)
            .stream()
            .sorted(Comparator.comparing(Transaction::getTransactionDate).reversed())
            .limit(limit)
            .map(transactionMapper::toResponse)
            .collect(Collectors.toList());
    }
}
