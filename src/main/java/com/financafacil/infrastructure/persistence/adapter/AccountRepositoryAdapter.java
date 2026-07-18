package com.financafacil.infrastructure.persistence.adapter;

import com.financafacil.domain.model.Account;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.infrastructure.persistence.jpa.AccountJpaRepository;
import com.financafacil.infrastructure.persistence.mapper.AccountJpaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {
    private final AccountJpaRepository jpaRepository;
    private final AccountJpaMapper mapper;

    @Override
    public Account save(Account a) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(a)));
    }

    @Override
    public Optional<Account> findById(UUID id, UUID userId) {
        return jpaRepository.findByIdAndUserId(id, userId).map(mapper::toDomain);
    }

    @Override
    public List<Account> findAllByUserId(UUID userId) {
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
    @Transactional
    public void updateBalance(UUID accountId, BigDecimal newBalance) {
        jpaRepository.updateBalance(accountId, newBalance);
    }

    @Override
    @Transactional
    public void adjustBalance(UUID accountId, BigDecimal delta) {
        jpaRepository.adjustBalance(accountId, delta);
    }

    @Override
    public boolean existsByIdAndUserId(UUID id, UUID userId) {
        return jpaRepository.existsByIdAndUserId(id, userId);
    }

    @Override
    public boolean hasTransactions(UUID accountId) {
        return jpaRepository.hasTransactions(accountId);
    }
}
