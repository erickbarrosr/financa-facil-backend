package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.AccountUseCase;
import com.financafacil.presentation.dto.request.CreateAccountRequest;
import com.financafacil.presentation.dto.request.UpdateAccountRequest;
import com.financafacil.presentation.dto.response.AccountResponse;
import com.financafacil.presentation.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {
    private final AccountUseCase accountUseCase;

    @GetMapping
    public ApiResponse<List<AccountResponse>> findAll(@AuthenticationPrincipal Object principal) {
        return ApiResponse.ok(accountUseCase.findAll(toUuid(principal)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AccountResponse> create(@AuthenticationPrincipal Object principal,
                                               @Valid @RequestBody CreateAccountRequest req) {
        return ApiResponse.ok(accountUseCase.create(
            toUuid(principal),
            req.getName(),
            req.getType(),
            req.getInitialBalance(),
            req.getColor()));
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountResponse> update(@AuthenticationPrincipal Object principal,
                                               @PathVariable UUID id,
                                               @Valid @RequestBody UpdateAccountRequest req) {
        return ApiResponse.ok(accountUseCase.update(
            toUuid(principal),
            id,
            req.getName(),
            req.getType(),
            req.getColor()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Object principal, @PathVariable UUID id) {
        accountUseCase.delete(toUuid(principal), id);
    }

    @GetMapping("/{id}/balance")
    public ApiResponse<BigDecimal> getBalance(@AuthenticationPrincipal Object principal,
                                              @PathVariable UUID id) {
        return ApiResponse.ok(accountUseCase.getBalance(toUuid(principal), id));
    }

    private UUID toUuid(Object principal) {
        return UUID.fromString(principal.toString());
    }
}
