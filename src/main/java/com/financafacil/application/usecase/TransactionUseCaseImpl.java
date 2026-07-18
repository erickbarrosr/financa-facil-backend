package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Transaction;
import com.financafacil.domain.port.in.TransactionUseCase;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.domain.port.out.TransactionRepository;
import com.financafacil.domain.port.out.TransactionRepository.TransactionFilter;
import com.financafacil.presentation.dto.response.PageResponse;
import com.financafacil.presentation.dto.response.TransactionResponse;
import com.financafacil.presentation.mapper.TransactionPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionUseCaseImpl implements TransactionUseCase {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionPresentationMapper presentationMapper;

    @Override
    @Transactional
    public TransactionResponse create(UUID userId, UUID accountId, UUID categoryId, String type,
                                      BigDecimal amount, String description, LocalDate transactionDate, String status) {
        var account = accountRepository.findById(accountId, userId)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada"));

        var transaction = transactionRepository.save(Transaction.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .accountId(accountId)
            .categoryId(categoryId)
            .type(type)
            .amount(amount)
            .description(description)
            .transactionDate(transactionDate)
            .status(status)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build());

        var delta = "income".equals(type) ? amount : amount.negate();
        accountRepository.updateBalance(accountId, account.getBalance().add(delta));

        return presentationMapper.toResponse(transaction);
    }

    @Override
    @Transactional
    public TransactionResponse update(UUID userId, UUID transactionId, UUID accountId, UUID categoryId,
                                      String type, BigDecimal amount, String description,
                                      LocalDate transactionDate, String status) {
        var existing = transactionRepository.findById(transactionId, userId)
            .orElseThrow(() -> new NotFoundException("Transação não encontrada"));
        var account = accountRepository.findById(existing.getAccountId(), userId)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada"));

        // Revert old balance effect
        var oldDelta = "income".equals(existing.getType()) ? existing.getAmount().negate() : existing.getAmount();
        // Apply new balance effect
        var newDelta = "income".equals(type) ? amount : amount.negate();
        accountRepository.updateBalance(existing.getAccountId(), account.getBalance().add(oldDelta).add(newDelta));

        var updated = transactionRepository.save(existing
            .withAccountId(accountId)
            .withCategoryId(categoryId)
            .withType(type)
            .withAmount(amount)
            .withDescription(description)
            .withTransactionDate(transactionDate)
            .withStatus(status)
            .withUpdatedAt(Instant.now()));

        return presentationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID transactionId) {
        var tx = transactionRepository.findById(transactionId, userId)
            .orElseThrow(() -> new NotFoundException("Transação não encontrada"));
        var account = accountRepository.findById(tx.getAccountId(), userId)
            .orElseThrow(() -> new NotFoundException("Conta não encontrada"));

        var revertDelta = "income".equals(tx.getType()) ? tx.getAmount().negate() : tx.getAmount();
        accountRepository.updateBalance(tx.getAccountId(), account.getBalance().add(revertDelta));
        transactionRepository.deleteById(transactionId, userId);
    }

    @Override
    public PageResponse<TransactionResponse> findAll(UUID userId, TransactionFilter filter, Pageable pageable) {
        var page = transactionRepository.findAll(userId, filter, pageable);
        return PageResponse.<TransactionResponse>builder()
            .content(page.getContent().stream().map(presentationMapper::toResponse).collect(Collectors.toList()))
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .build();
    }
}
