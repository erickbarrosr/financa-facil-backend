package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class UpdateBudgetRequest {
    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;
}
