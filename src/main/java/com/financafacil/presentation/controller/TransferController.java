package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.TransferUseCase;
import com.financafacil.presentation.dto.request.CreateTransferRequest;
import com.financafacil.presentation.dto.response.ApiResponse;
import com.financafacil.presentation.dto.response.TransferResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transfers")
@RequiredArgsConstructor
public class TransferController {
    private final TransferUseCase transferUseCase;

    @GetMapping
    public ApiResponse<List<TransferResponse>> findAll(@AuthenticationPrincipal Object principal) {
        return ApiResponse.ok(transferUseCase.findAll(toUuid(principal)));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TransferResponse> create(@AuthenticationPrincipal Object principal,
                                                @Valid @RequestBody CreateTransferRequest req) {
        return ApiResponse.ok(transferUseCase.create(
            toUuid(principal),
            req.getFromAccountId(),
            req.getToAccountId(),
            req.getAmount(),
            req.getTransferDate(),
            req.getDescription()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Object principal, @PathVariable UUID id) {
        transferUseCase.delete(toUuid(principal), id);
    }

    private UUID toUuid(Object principal) {
        return UUID.fromString(principal.toString());
    }
}
