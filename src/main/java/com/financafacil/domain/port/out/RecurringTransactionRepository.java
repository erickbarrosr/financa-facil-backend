package com.financafacil.domain.port.out;

import com.financafacil.domain.model.RecurringTransaction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTransactionRepository {
    RecurringTransaction save(RecurringTransaction recurringTransaction);
    Optional<RecurringTransaction> findById(UUID id, UUID userId);
    List<RecurringTransaction> findAllByUserId(UUID userId);
    void deleteById(UUID id, UUID userId);
}
