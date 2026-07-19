package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateRecurringTransactionRequest {
    private UUID accountId;

    private UUID categoryId;

    @DecimalMin("0.01")
    private BigDecimal amount;

    @Pattern(regexp = "income|expense")
    private String type;

    @Pattern(regexp = "daily|weekly|monthly|yearly")
    private String frequency;

    private LocalDate nextExecution;
}
