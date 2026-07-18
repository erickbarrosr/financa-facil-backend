package com.financafacil.infrastructure.persistence.jpa;

import com.financafacil.infrastructure.persistence.entity.BudgetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetJpaRepository extends JpaRepository<BudgetJpaEntity, UUID> {
    Optional<BudgetJpaEntity> findByIdAndUserId(UUID id, UUID userId);
    List<BudgetJpaEntity> findAllByUserId(UUID userId);
    boolean existsByUserIdAndCategoryIdAndMonthAndYear(UUID userId, UUID categoryId, int month, int year);

    @Modifying
    @Query("DELETE FROM BudgetJpaEntity b WHERE b.id = :id AND b.userId = :userId")
    void deleteByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);
}
