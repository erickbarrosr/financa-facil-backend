package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.Budget;
import com.financafacil.domain.port.out.BudgetRepository;
import com.financafacil.infrastructure.persistence.jpa.BudgetJpaRepository;
import com.financafacil.infrastructure.persistence.mapper.BudgetJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BudgetRepositoryAdapter implements BudgetRepository {
    private final BudgetJpaRepository jpaRepository;
    private final BudgetJpaMapper mapper;

    @Override
    public Budget save(Budget budget) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(budget)));
    }

    @Override
    public Optional<Budget> findById(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public List<Budget> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID id, UUID userId) {
        jpaRepository.deleteByIdAndUserId(id, userId);
    }

    @Override
    public boolean existsByUserIdAndCategoryIdAndMonthAndYear(UUID userId, UUID categoryId, int month, int year) {
        return jpaRepository.existsByUserIdAndCategoryIdAndMonthAndYear(userId, categoryId, month, year);
    }
}
