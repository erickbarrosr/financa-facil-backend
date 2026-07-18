package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
    Optional<AccountJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    List<AccountJpaEntity> findAllByUserId(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("UPDATE AccountJpaEntity a SET a.balance = :balance WHERE a.id = :accountId")
    void updateBalance(UUID accountId, BigDecimal balance);

    @Query(value = "SELECT COUNT(*) > 0 FROM transactions t WHERE t.account_id = :accountId", nativeQuery = true)
    boolean hasTransactions(UUID accountId);
}
