package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateRecurringTransactionRequest {
    @NotNull
    private UUID accountId;

    private UUID categoryId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotBlank
    @Pattern(regexp = "income|expense")
    private String type;

    @NotBlank
    @Pattern(regexp = "daily|weekly|monthly|yearly")
    private String frequency;

    @NotNull
    private LocalDate nextExecution;
}
