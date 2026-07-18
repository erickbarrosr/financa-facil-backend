package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {
    Optional<AccountJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    List<AccountJpaEntity> findAllByUserId(UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("DELETE FROM AccountJpaEntity a WHERE a.id = :id AND a.userId = :userId")
    void deleteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE AccountJpaEntity a SET a.balance = :balance WHERE a.id = :accountId")
    void updateBalance(@Param("accountId") UUID accountId, @Param("balance") BigDecimal balance);

    @Modifying
    @Query("UPDATE AccountJpaEntity a SET a.balance = a.balance + :delta WHERE a.id = :accountId")
    void adjustBalance(@Param("accountId") UUID accountId, @Param("delta") BigDecimal delta);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM transactions WHERE account_id = :accountId", nativeQuery = true)
    boolean hasTransactions(@Param("accountId") UUID accountId);
}
