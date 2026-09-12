package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.Transaction;
import com.financafacil.domain.port.out.TransactionRepository;
import com.financafacil.infrastructure.persistence.jpa.TransactionJpaRepository;
import com.financafacil.infrastructure.persistence.mapper.TransactionJpaMapper;
import com.financafacil.infrastructure.persistence.specification.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionRepositoryAdapter implements TransactionRepository {
    private final TransactionJpaRepository jpaRepository;
    private final TransactionJpaMapper mapper;

    @Override
    public Transaction save(Transaction t) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(t)));
    }

    @Override
    public Optional<Transaction> findById(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public Page<Transaction> findAll(UUID userId, TransactionFilter filter, Pageable pageable) {
        var spec = TransactionSpecification.withFilter(userId, filter);
        return jpaRepository.findAll(spec, pageable).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id, UUID userId) {
        jpaRepository.findByIdAndUserId(id, userId).ifPresent(jpaRepository::delete);
    }

    @Override
    public List<Transaction> findByUserIdAndDateBetween(UUID userId, LocalDate start, LocalDate end) {
        return jpaRepository.findByUserIdAndDateBetween(userId, start, end)
            .stream().map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<Transaction> findByAccountIdAndDateBetween(UUID accountId, LocalDate start, LocalDate end) {
        return jpaRepository.findByAccountIdAndDateBetween(accountId, start, end)
            .stream().map(mapper::toDomain).collect(Collectors.toList());
    }
}
