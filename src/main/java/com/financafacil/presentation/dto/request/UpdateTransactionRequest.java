package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class UpdateTransactionRequest {
    @NotNull private UUID accountId;
    private UUID categoryId;
    @NotBlank @Pattern(regexp = "income|expense") private String type;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    private String description;
    @NotNull private LocalDate transactionDate;
    @NotBlank @Pattern(regexp = "PENDING|PAID|OVERDUE|CANCELLED") private String status;
}
