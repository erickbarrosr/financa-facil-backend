package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Transfer;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.domain.port.out.TransferRepository;
import com.financafacil.presentation.dto.response.TransferResponse;
import com.financafacil.presentation.mapper.TransferPresentationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferUseCaseImplTest {

    @Mock TransferRepository transferRepository;
    @Mock AccountRepository accountRepository;
    @Mock TransferPresentationMapper presentationMapper;
    @InjectMocks TransferUseCaseImpl transferUseCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID fromAccountId = UUID.randomUUID();
    private final UUID toAccountId = UUID.randomUUID();

    @Test
    void create_adjustsBalancesAndSaves() {
        var amount = BigDecimal.valueOf(200);
        var transfer = buildTransfer(amount);
        when(accountRepository.existsByIdAndUserId(fromAccountId, userId)).thenReturn(true);
        when(accountRepository.existsByIdAndUserId(toAccountId, userId)).thenReturn(true);
        when(transferRepository.save(any())).thenReturn(transfer);
        when(presentationMapper.toResponse(transfer)).thenReturn(buildResponse(transfer));

        transferUseCase.create(userId, fromAccountId, toAccountId, amount, LocalDate.now(), "Test");

        verify(accountRepository).adjustBalance(fromAccountId, amount.negate());
        verify(accountRepository).adjustBalance(toAccountId, amount);
        verify(transferRepository).save(any());
    }

    @Test
    void create_throwsNotFound_whenFromAccountNotBelongsToUser() {
        when(accountRepository.existsByIdAndUserId(fromAccountId, userId)).thenReturn(false);

        assertThatThrownBy(() -> transferUseCase.create(userId, fromAccountId, toAccountId,
                BigDecimal.valueOf(100), LocalDate.now(), null))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("origem");
    }

    @Test
    void create_throwsNotFound_whenToAccountNotBelongsToUser() {
        when(accountRepository.existsByIdAndUserId(fromAccountId, userId)).thenReturn(true);
        when(accountRepository.existsByIdAndUserId(toAccountId, userId)).thenReturn(false);

        assertThatThrownBy(() -> transferUseCase.create(userId, fromAccountId, toAccountId,
                BigDecimal.valueOf(100), LocalDate.now(), null))
            .isInstanceOf(NotFoundException.class)
            .hasMessageContaining("destino");
    }

    @Test
    void delete_revertsBalancesAndDeletes() {
        var amount = BigDecimal.valueOf(150);
        var transferId = UUID.randomUUID();
        var transfer = buildTransfer(amount);
        when(transferRepository.findById(transferId, userId)).thenReturn(Optional.of(transfer));

        transferUseCase.delete(userId, transferId);

        verify(accountRepository).adjustBalance(fromAccountId, amount);
        verify(accountRepository).adjustBalance(toAccountId, amount.negate());
        verify(transferRepository).deleteById(transferId, userId);
    }

    @Test
    void delete_throwsNotFound_whenTransferNotExists() {
        var transferId = UUID.randomUUID();
        when(transferRepository.findById(transferId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferUseCase.delete(userId, transferId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAll_returnsListForUser() {
        var transfer = buildTransfer(BigDecimal.valueOf(100));
        when(transferRepository.findAllByUserId(userId)).thenReturn(List.of(transfer));
        when(presentationMapper.toResponse(transfer)).thenReturn(buildResponse(transfer));

        var result = transferUseCase.findAll(userId);
        assertThat(result).hasSize(1);
    }

    private Transfer buildTransfer(BigDecimal amount) {
        return Transfer.builder()
            .id(UUID.randomUUID()).userId(userId)
            .fromAccountId(fromAccountId).toAccountId(toAccountId)
            .amount(amount).transferDate(LocalDate.now())
            .createdAt(Instant.now()).build();
    }

    private TransferResponse buildResponse(Transfer t) {
        return TransferResponse.builder()
            .id(t.getId()).fromAccountId(t.getFromAccountId()).toAccountId(t.getToAccountId())
            .amount(t.getAmount()).transferDate(t.getTransferDate()).build();
    }
}
