package com.financafacil.presentation.controller;

import com.financafacil.domain.port.in.CategoryUseCase;
import com.financafacil.presentation.dto.request.CreateCategoryRequest;
import com.financafacil.presentation.dto.request.UpdateCategoryRequest;
import com.financafacil.presentation.dto.response.ApiResponse;
import com.financafacil.presentation.dto.response.CategoryResponse;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryUseCase categoryUseCase;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> findAll(@AuthenticationPrincipal Object principal,
                                                       @RequestParam(required = false) String type) {
        return ApiResponse.ok(categoryUseCase.findAll(toUuid(principal), type));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> create(@AuthenticationPrincipal Object principal,
                                                @Valid @RequestBody CreateCategoryRequest req) {
        return ApiResponse.ok(categoryUseCase.create(
            toUuid(principal),
            req.getName(),
            req.getType(),
            req.getIcon(),
            req.getColor()));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@AuthenticationPrincipal Object principal,
                                                @PathVariable UUID id,
                                                @Valid @RequestBody UpdateCategoryRequest req) {
        return ApiResponse.ok(categoryUseCase.update(
            toUuid(principal),
            id,
            req.getName(),
            req.getIcon(),
            req.getColor()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Object principal, @PathVariable UUID id) {
        categoryUseCase.delete(toUuid(principal), id);
    }

    private UUID toUuid(Object principal) {
        return UUID.fromString(principal.toString());
    }
}
