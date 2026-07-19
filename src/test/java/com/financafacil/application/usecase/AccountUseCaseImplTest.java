package com.financafacil.application.usecase;

import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Account;
import com.financafacil.domain.port.out.AccountRepository;
import com.financafacil.presentation.dto.response.AccountResponse;
import com.financafacil.presentation.mapper.AccountPresentationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountUseCaseImplTest {

    @Mock AccountRepository accountRepository;
    @Mock AccountPresentationMapper presentationMapper;
    @InjectMocks AccountUseCaseImpl accountUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void create_savesAccountWithInitialBalance() {
        var account = buildAccount(BigDecimal.valueOf(1000));
        when(accountRepository.save(any())).thenReturn(account);
        when(presentationMapper.toResponse(account)).thenReturn(AccountResponse.builder()
            .id(account.getId()).name(account.getName()).type(account.getType())
            .balance(account.getBalance()).color(account.getColor()).build());

        accountUseCase.create(userId, "Nubank", "checking", BigDecimal.valueOf(1000), "#8B5CF6");

        var captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        assertThat(captor.getValue().getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void delete_throwsConflict_whenAccountHasTransactions() {
        var accountId = UUID.randomUUID();
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);
        when(accountRepository.hasTransactions(accountId)).thenReturn(true);

        assertThatThrownBy(() -> accountUseCase.delete(userId, accountId))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("transações");
    }

    @Test
    void delete_throwsConflict_whenAccountHasTransfers() {
        var accountId = UUID.randomUUID();
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);
        when(accountRepository.hasTransactions(accountId)).thenReturn(false);
        when(accountRepository.hasTransfers(accountId)).thenReturn(true);

        assertThatThrownBy(() -> accountUseCase.delete(userId, accountId))
            .isInstanceOf(ConflictException.class)
            .hasMessageContaining("transferências");
    }

    @Test
    void delete_succeeds_whenAccountHasNoTransactionsOrTransfers() {
        var accountId = UUID.randomUUID();
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(true);
        when(accountRepository.hasTransactions(accountId)).thenReturn(false);
        when(accountRepository.hasTransfers(accountId)).thenReturn(false);

        accountUseCase.delete(userId, accountId);

        verify(accountRepository).deleteById(accountId, userId);
    }

    @Test
    void delete_throwsNotFound_whenAccountBelongsToOtherUser() {
        var accountId = UUID.randomUUID();
        when(accountRepository.existsByIdAndUserId(accountId, userId)).thenReturn(false);

        assertThatThrownBy(() -> accountUseCase.delete(userId, accountId))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAll_returnsListForUser() {
        var account = buildAccount(BigDecimal.ZERO);
        when(accountRepository.findAllByUserId(userId)).thenReturn(List.of(account));
        when(presentationMapper.toResponse(account)).thenReturn(AccountResponse.builder()
            .id(account.getId()).name(account.getName()).type(account.getType())
            .balance(account.getBalance()).color(account.getColor()).build());

        var result = accountUseCase.findAll(userId);
        assertThat(result).hasSize(1);
    }

    private Account buildAccount(BigDecimal balance) {
        return Account.builder().id(UUID.randomUUID()).userId(userId)
            .name("Test").type("checking").balance(balance).color("#000000")
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
