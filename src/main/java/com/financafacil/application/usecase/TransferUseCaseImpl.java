package com.financafacil.application.usecase;

import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Transfer;
import com.financafacil.domain.port.in.TransferUseCase;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.domain.port.out.TransferRepository;
import com.financafacil.presentation.dto.response.TransferResponse;
import com.financafacil.presentation.mapper.TransferPresentationMapper;
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
public class TransferUseCaseImpl implements TransferUseCase {
    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final TransferPresentationMapper presentationMapper;

    @Override
    @Transactional
    public TransferResponse create(UUID userId, UUID fromAccountId, UUID toAccountId,
                                   BigDecimal amount, LocalDate transferDate, String description) {
        if (fromAccountId.equals(toAccountId)) {
            throw new ConflictException("Conta de origem e destino não podem ser iguais");
        }
        if (!accountRepository.existsByIdAndUserId(fromAccountId, userId)) {
            throw new NotFoundException("Conta de origem não encontrada");
        }
        if (!accountRepository.existsByIdAndUserId(toAccountId, userId)) {
            throw new NotFoundException("Conta de destino não encontrada");
        }
        accountRepository.adjustBalance(fromAccountId, amount.negate());
        accountRepository.adjustBalance(toAccountId, amount);

        var transfer = transferRepository.save(Transfer.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .fromAccountId(fromAccountId)
            .toAccountId(toAccountId)
            .amount(amount)
            .transferDate(transferDate)
            .description(description)
            .createdAt(Instant.now())
            .build());
        return presentationMapper.toResponse(transfer);
    }

    @Override
    public List<TransferResponse> findAll(UUID userId) {
        return transferRepository.findAllByUserId(userId).stream()
            .map(presentationMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID transferId) {
        var transfer = transferRepository.findById(transferId, userId)
            .orElseThrow(() -> new NotFoundException("Transferência não encontrada"));
        accountRepository.adjustBalance(transfer.getFromAccountId(), transfer.getAmount());
        accountRepository.adjustBalance(transfer.getToAccountId(), transfer.getAmount().negate());
        transferRepository.deleteById(transferId, userId);
    }
}
