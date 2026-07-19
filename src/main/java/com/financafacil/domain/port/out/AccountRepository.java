package com.financafacil.domain.port.out;

import com.financafacil.domain.model.Account;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account account);
    Optional<Account> findById(UUID id, UUID userId);
    List<Account> findAllByUserId(UUID userId);
    void deleteById(UUID id, UUID userId);
    void updateBalance(UUID accountId, BigDecimal newBalance);
    void adjustBalance(UUID accountId, BigDecimal delta);
    boolean existsByIdAndUserId(UUID id, UUID userId);
    boolean hasTransactions(UUID accountId);
    boolean hasTransfers(UUID accountId);
}
