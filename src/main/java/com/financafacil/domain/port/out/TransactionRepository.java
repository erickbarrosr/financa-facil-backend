package com.financafacil.domain.port.out;

import com.financafacil.domain.model.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction transaction);
    Optional<Transaction> findById(UUID id, UUID userId);
    Page<Transaction> findAll(UUID userId, TransactionFilter filter, Pageable pageable);
    void deleteById(UUID id, UUID userId);
    List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDate start, LocalDate end);
    List<Transaction> findByAccountIdAndDateBetween(UUID accountId, LocalDate start, LocalDate end);

    record TransactionFilter(
        LocalDate startDate, LocalDate endDate,
        UUID categoryId, UUID accountId,
        String type, String status
    ) {}
}
