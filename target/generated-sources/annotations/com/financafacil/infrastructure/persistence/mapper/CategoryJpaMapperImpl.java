package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Category;
import com.financafacil.infrastructure.persistence.entity.CategoryJpaEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:29-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class CategoryJpaMapperImpl implements CategoryJpaMapper {

    @Override
    public CategoryJpaEntity toEntity(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryJpaEntity.CategoryJpaEntityBuilder categoryJpaEntity = CategoryJpaEntity.builder();

        categoryJpaEntity.id( category.getId() );
        categoryJpaEntity.userId( category.getUserId() );
        categoryJpaEntity.name( category.getName() );
        categoryJpaEntity.type( category.getType() );
        categoryJpaEntity.icon( category.getIcon() );
        categoryJpaEntity.color( category.getColor() );
        categoryJpaEntity.createdAt( category.getCreatedAt() );
        categoryJpaEntity.updatedAt( category.getUpdatedAt() );

        return categoryJpaEntity.build();
    }

    @Override
    public Category toDomain(CategoryJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.id( entity.getId() );
        category.userId( entity.getUserId() );
        category.name( entity.getName() );
        category.type( entity.getType() );
        category.icon( entity.getIcon() );
        category.color( entity.getColor() );
        category.createdAt( entity.getCreatedAt() );
        category.updatedAt( entity.getUpdatedAt() );

        return category.build();
    }
}
