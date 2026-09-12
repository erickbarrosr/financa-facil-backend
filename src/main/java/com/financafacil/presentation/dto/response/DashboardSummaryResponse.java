package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter @Builder
public class DashboardSummaryResponse {
    private final BigDecimal totalBalance;
    private final BigDecimal monthIncome;
    private final BigDecimal monthExpense;
    private final int year;
    private final int month;
}
