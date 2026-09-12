package com.financafacil.domain.port.in;

import com.financafacil.presentation.dto.response.TransferResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransferUseCase {
    TransferResponse create(UUID userId, UUID fromAccountId, UUID toAccountId, BigDecimal amount, LocalDate transferDate, String description);
    List<TransferResponse> findAll(UUID userId);
    void delete(UUID userId, UUID transferId);
}
