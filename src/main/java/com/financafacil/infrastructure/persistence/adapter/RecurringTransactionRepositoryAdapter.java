package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.RecurringTransaction;
import com.financafacil.domain.port.out.RecurringTransactionRepository;
import com.financafacil.infrastructure.persistence.jpa.RecurringTransactionJpaRepository;
import com.financafacil.infrastructure.persistence.mapper.RecurringTransactionJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RecurringTransactionRepositoryAdapter implements RecurringTransactionRepository {
    private final RecurringTransactionJpaRepository jpaRepository;
    private final RecurringTransactionJpaMapper mapper;

    @Override
    public RecurringTransaction save(RecurringTransaction recurringTransaction) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(recurringTransaction)));
    }

    @Override
    public Optional<RecurringTransaction> findById(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public List<RecurringTransaction> findAllByUserId(UUID userId) {
        return jpaRepository.findAllByUserId(userId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteById(UUID id, UUID userId) {
        jpaRepository.deleteByIdAndUserId(id, userId);
    }
}
