package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Category;
import com.financafacil.infrastructure.persistence.entity.CategoryJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryJpaMapper {
    CategoryJpaEntity toEntity(Category category);
    Category toDomain(CategoryJpaEntity entity);
}
