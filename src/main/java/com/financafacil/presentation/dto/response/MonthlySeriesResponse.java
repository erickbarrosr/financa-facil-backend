package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;

@Getter @Builder
public class MonthlySeriesResponse {
    private final String label;
    private final int year;
    private final int month;
    private final BigDecimal income;
    private final BigDecimal expense;
}
