package com.financafacil.presentation.dto.request;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class TransactionFilterRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private UUID categoryId;
    private UUID accountId;
    private String type;
    private String status;
}
