package com.financafacil.domain.port.in;

import com.financafacil.presentation.dto.response.CategoryBreakdownResponse;
import com.financafacil.presentation.dto.response.DashboardSummaryResponse;
import com.financafacil.presentation.dto.response.MonthlySeriesResponse;
import com.financafacil.presentation.dto.response.TransactionResponse;

import java.util.List;
import java.util.UUID;

public interface DashboardUseCase {
    DashboardSummaryResponse getSummary(UUID userId, int year, int month);
    List<MonthlySeriesResponse> getMonthlySeries(UUID userId, int months);
    List<CategoryBreakdownResponse> getByCategory(UUID userId, int year, int month, String type);
    List<TransactionResponse> getRecentTransactions(UUID userId, int limit);
}
