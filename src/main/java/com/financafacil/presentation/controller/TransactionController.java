package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.TransactionUseCase;
import com.financafacil.domain.port.out.TransactionRepository.TransactionFilter;
import com.financafacil.presentation.dto.request.CreateTransactionRequest;
import com.financafacil.presentation.dto.request.UpdateTransactionRequest;
import com.financafacil.presentation.dto.response.ApiResponse;
import com.financafacil.presentation.dto.response.PageResponse;
import com.financafacil.presentation.dto.response.TransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionUseCase transactionUseCase;

    @GetMapping
    public ApiResponse<PageResponse<TransactionResponse>> findAll(
            @AuthenticationPrincipal Object principal,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID accountId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "transactionDate,desc") String sort) {
        var filter = new TransactionFilter(startDate, endDate, categoryId, accountId, type, status);
        var parts = sort.split(",");
        var pageable = PageRequest.of(page, size,
            Sort.by(parts.length > 1 && "asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC,
                parts[0]));
        return ApiResponse.ok(transactionUseCase.findAll(toUuid(principal), filter, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransactionResponse> create(@AuthenticationPrincipal Object principal,
                                                   @Valid @RequestBody CreateTransactionRequest req) {
        return ApiResponse.ok(transactionUseCase.create(
            toUuid(principal),
            req.getAccountId(),
            req.getCategoryId(),
            req.getType(),
            req.getAmount(),
            req.getDescription(),
            req.getTransactionDate(),
            req.getStatus()));
    }

    @PutMapping("/{id}")
    public ApiResponse<TransactionResponse> update(@AuthenticationPrincipal Object principal,
                                                   @PathVariable UUID id,
                                                   @Valid @RequestBody UpdateTransactionRequest req) {
        return ApiResponse.ok(transactionUseCase.update(
            toUuid(principal),
            id,
            req.getAccountId(),
            req.getCategoryId(),
            req.getType(),
            req.getAmount(),
            req.getDescription(),
            req.getTransactionDate(),
            req.getStatus()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Object principal, @PathVariable UUID id) {
        transactionUseCase.delete(toUuid(principal), id);
    }

    private UUID toUuid(Object principal) {
        return UUID.fromString(principal.toString());
    }
}
