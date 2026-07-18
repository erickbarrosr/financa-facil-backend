package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.TransferJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransferJpaRepository extends JpaRepository<TransferJpaEntity, UUID> {
    Optional<TransferJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    List<TransferJpaEntity> findAllByUserId(UUID userId);

    @Modifying
    @Query("DELETE FROM TransferJpaEntity t WHERE t.id = :id AND t.userId = :userId")
    void deleteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
