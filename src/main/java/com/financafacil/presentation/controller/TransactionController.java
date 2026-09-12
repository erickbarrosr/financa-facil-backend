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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.Set;
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
        var allowedSortFields = Set.of("transactionDate", "amount", "createdAt");
        var sortField = allowedSortFields.contains(parts[0]) ? parts[0] : "transactionDate";
        var pageable = PageRequest.of(page, size,
            Sort.by(parts.length > 1 && "asc".equalsIgnoreCase(parts[1]) ? Sort.Direction.ASC : Sort.Direction.DESC,
                sortField));
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
