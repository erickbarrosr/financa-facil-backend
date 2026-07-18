package com.financafacil.application.usecase;

import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Account;
import com.financafacil.domain.port.in.AccountUseCase;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.presentation.dto.response.AccountResponse;
import com.financafacil.presentation.mapper.AccountPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountUseCaseImpl implements AccountUseCase {
    private final AccountRepository accountRepository;
    private final AccountPresentationMapper presentationMapper;

    @Override
    @Transactional
    public AccountResponse create(UUID userId, String name, String type, BigDecimal initialBalance, String color) {
        var account = accountRepository.save(Account.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .name(name)
            .type(type)
            .balance(initialBalance)
            .color(color)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build());
        return presentationMapper.toResponse(account);
    }

    @Override
    public List<AccountResponse> findAll(UUID userId) {
        return accountRepository.findAllByUserId(userId).stream()
            .map(presentationMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountResponse update(UUID userId, UUID accountId, String name, String type, String color) {
        var account = accountRepository.findById(accountId, userId)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada"));
        var updated = accountRepository.save(account
            .withName(name)
            .withType(type)
            .withColor(color)
            .withUpdatedAt(Instant.now()));
        return presentationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID accountId) {
        if (!accountRepository.existsByIdAndUserId(accountId, userId)) {
            throw new NotFoundException("Conta não encontrada");
        }
        if (accountRepository.hasTransactions(accountId)) {
            throw new ConflictException("Não é possível remover uma conta com transações");
        }
        accountRepository.deleteById(accountId, userId);
    }

    @Override
    public BigDecimal getBalance(UUID userId, UUID accountId) {
        return accountRepository.findById(accountId, userId)
            .map(Account::getBalance)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada"));
    }
}
