package com.financafacil.domain.port.in;

import com.financafacil.domain.port.out.TransactionRepository.TransactionFilter;
import com.financafacil.presentation.dto.response.PageResponse;
import com.financafacil.presentation.dto.response.TransactionResponse;
import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface TransactionUseCase {
    TransactionResponse create(UUID userId, UUID accountId, UUID categoryId, String type,
        BigDecimal amount, String description, LocalDate transactionDate, String status);
    TransactionResponse update(UUID userId, UUID transactionId, UUID accountId, UUID categoryId,
        String type, BigDecimal amount, String description, LocalDate transactionDate, String status);
    void delete(UUID userId, UUID transactionId);
    PageResponse<TransactionResponse> findAll(UUID userId, TransactionFilter filter, Pageable pageable);
}
