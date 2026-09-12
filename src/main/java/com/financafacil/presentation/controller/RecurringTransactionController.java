package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.RecurringTransactionUseCase;
import com.financafacil.presentation.dto.request.CreateRecurringTransactionRequest;
import com.financafacil.presentation.dto.request.UpdateRecurringTransactionRequest;
import com.financafacil.presentation.dto.response.ApiResponse;
import com.financafacil.presentation.dto.response.RecurringTransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/recurring-transactions")
@RequiredArgsConstructor
public class RecurringTransactionController {
    private final RecurringTransactionUseCase recurringTransactionUseCase;

    @GetMapping
    public ApiResponse<List<RecurringTransactionResponse>> findAll(@AuthenticationPrincipal Object principal) {
        return ApiResponse.ok(recurringTransactionUseCase.findAll(toUuid(principal)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<RecurringTransactionResponse> create(@AuthenticationPrincipal Object principal,
                                                            @Valid @RequestBody CreateRecurringTransactionRequest req) {
        return ApiResponse.ok(recurringTransactionUseCase.create(
            toUuid(principal),
            req.getAccountId(),
            req.getCategoryId(),
            req.getAmount(),
            req.getType(),
            req.getFrequency(),
            req.getNextExecution()));
    }

    @PutMapping("/{id}")
    public ApiResponse<RecurringTransactionResponse> update(@AuthenticationPrincipal Object principal,
                                                            @PathVariable UUID id,
                                                            @Valid @RequestBody UpdateRecurringTransactionRequest req) {
        return ApiResponse.ok(recurringTransactionUseCase.update(
            toUuid(principal),
            id,
            req.getAccountId(),
            req.getCategoryId(),
            req.getAmount(),
            req.getType(),
            req.getFrequency(),
            req.getNextExecution()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Object principal, @PathVariable UUID id) {
        recurringTransactionUseCase.delete(toUuid(principal), id);
    }

    @PatchMapping("/{id}/toggle")
    public ApiResponse<RecurringTransactionResponse> toggleActive(@AuthenticationPrincipal Object principal,
                                                                   @PathVariable UUID id) {
        return ApiResponse.ok(recurringTransactionUseCase.toggleActive(toUuid(principal), id));
    }

    private UUID toUuid(Object principal) {
        return UUID.fromString(principal.toString());
    }
}
