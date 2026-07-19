package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.RecurringTransaction;
import com.financafacil.domain.port.in.RecurringTransactionUseCase;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.domain.port.out.RecurringTransactionRepository;
import com.financafacil.presentation.dto.response.RecurringTransactionResponse;
import com.financafacil.presentation.mapper.RecurringTransactionPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecurringTransactionUseCaseImpl implements RecurringTransactionUseCase {
    private final RecurringTransactionRepository recurringTransactionRepository;
    private final RecurringTransactionPresentationMapper presentationMapper;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public RecurringTransactionResponse create(UUID userId, UUID accountId, UUID categoryId,
                                               BigDecimal amount, String type, String frequency,
                                               LocalDate nextExecution) {
        if (!accountRepository.existsByIdAndUserId(accountId, userId)) {
            throw new NotFoundException("Conta não encontrada");
        }
        var recurring = recurringTransactionRepository.save(RecurringTransaction.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .accountId(accountId)
            .categoryId(categoryId)
            .amount(amount)
            .type(type)
            .frequency(frequency)
            .nextExecution(nextExecution)
            .active(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build());
        return presentationMapper.toResponse(recurring);
    }

    @Override
    public List<RecurringTransactionResponse> findAll(UUID userId) {
        return recurringTransactionRepository.findAllByUserId(userId).stream()
            .map(presentationMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RecurringTransactionResponse update(UUID userId, UUID id, UUID accountId, UUID categoryId,
                                               BigDecimal amount, String type, String frequency,
                                               LocalDate nextExecution) {
        var existing = recurringTransactionRepository.findById(id, userId)
            .orElseThrow(() -> new NotFoundException("Transação recorrente não encontrada"));

        var candidate = existing;
        if (accountId != null) { candidate = candidate.withAccountId(accountId); }
        if (categoryId != null) { candidate = candidate.withCategoryId(categoryId); }
        if (amount != null) { candidate = candidate.withAmount(amount); }
        if (type != null) { candidate = candidate.withType(type); }
        if (frequency != null) { candidate = candidate.withFrequency(frequency); }
        if (nextExecution != null) { candidate = candidate.withNextExecution(nextExecution); }
        candidate = candidate.withUpdatedAt(Instant.now());

        var updated = recurringTransactionRepository.save(candidate);
        return presentationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID id) {
        if (recurringTransactionRepository.findById(id, userId).isEmpty()) {
            throw new NotFoundException("Transação recorrente não encontrada");
        }
        recurringTransactionRepository.deleteById(id, userId);
    }

    @Override
    @Transactional
    public RecurringTransactionResponse toggleActive(UUID userId, UUID id) {
        var existing = recurringTransactionRepository.findById(id, userId)
            .orElseThrow(() -> new NotFoundException("Transação recorrente não encontrada"));
        var updated = recurringTransactionRepository.save(existing
            .withActive(!existing.isActive())
            .withUpdatedAt(Instant.now()));
        return presentationMapper.toResponse(updated);
    }
}
