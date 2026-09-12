package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.RecurringTransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringTransactionJpaRepository extends JpaRepository<RecurringTransactionJpaEntity, UUID> {
    Optional<RecurringTransactionJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    List<RecurringTransactionJpaEntity> findAllByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM RecurringTransactionJpaEntity r WHERE r.id = :id AND r.userId = :userId")
    void deleteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
