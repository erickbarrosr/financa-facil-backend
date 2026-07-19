package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.BudgetUseCase;
import com.financafacil.presentation.dto.request.CreateBudgetRequest;
import com.financafacil.presentation.dto.request.UpdateBudgetRequest;
import com.financafacil.presentation.dto.response.ApiResponse;
import com.financafacil.presentation.dto.response.BudgetResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetUseCase budgetUseCase;

    @GetMapping
    public ApiResponse<List<BudgetResponse>> findAll(@AuthenticationPrincipal Object principal) {
        return ApiResponse.ok(budgetUseCase.findAll(toUuid(principal)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<BudgetResponse> create(@AuthenticationPrincipal Object principal,
                                              @Valid @RequestBody CreateBudgetRequest req) {
        return ApiResponse.ok(budgetUseCase.create(
            toUuid(principal),
            req.getCategoryId(),
            req.getAmount(),
            req.getMonth(),
            req.getYear()));
    }

    @PutMapping("/{id}")
    public ApiResponse<BudgetResponse> update(@AuthenticationPrincipal Object principal,
                                              @PathVariable UUID id,
                                              @Valid @RequestBody UpdateBudgetRequest req) {
        return ApiResponse.ok(budgetUseCase.update(toUuid(principal), id, req.getAmount()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Object principal, @PathVariable UUID id) {
        budgetUseCase.delete(toUuid(principal), id);
    }

    private UUID toUuid(Object principal) {
        return UUID.fromString(principal.toString());
    }
}
