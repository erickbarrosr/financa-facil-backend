package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateBudgetRequest {
    @NotNull
    private UUID categoryId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @Min(1)
    @Max(12)
    private int month;

    @Min(2020)
    private int year;
}
