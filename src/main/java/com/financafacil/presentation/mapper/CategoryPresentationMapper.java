package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Category;
import com.financafacil.presentation.dto.response.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryPresentationMapper {
    CategoryResponse toResponse(Category category);
}
