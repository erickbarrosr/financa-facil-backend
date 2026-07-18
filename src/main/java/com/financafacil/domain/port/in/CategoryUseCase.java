package com.financafacil.domain.port.in;

import com.financafacil.presentation.dto.response.CategoryResponse;
import java.util.List;
import java.util.UUID;

public interface CategoryUseCase {
    CategoryResponse create(UUID userId, String name, String type, String icon, String color);
    List<CategoryResponse> findAll(UUID userId, String type);
    CategoryResponse update(UUID userId, UUID categoryId, String name, String icon, String color);
    void delete(UUID userId, UUID categoryId);
}
