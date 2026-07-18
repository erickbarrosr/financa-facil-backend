package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID>,
        JpaSpecificationExecutor<TransactionJpaEntity> {

    Optional<TransactionJpaEntity> findByIdAndUserId(UUID id, UUID userId);

    @Query("SELECT t FROM TransactionJpaEntity t WHERE t.userId = :userId " +
           "AND t.transactionDate BETWEEN :start AND :end")
    List<TransactionJpaEntity> findByUserIdAndDateBetween(
        @Param("userId") UUID userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT t FROM TransactionJpaEntity t WHERE t.accountId = :accountId " +
           "AND t.transactionDate BETWEEN :start AND :end")
    List<TransactionJpaEntity> findByAccountIdAndDateBetween(
        @Param("accountId") UUID accountId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
