package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Category;
import com.financafacil.presentation.dto.response.CategoryResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:28-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class CategoryPresentationMapperImpl implements CategoryPresentationMapper {

    @Override
    public CategoryResponse toResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.id( category.getId() );
        categoryResponse.name( category.getName() );
        categoryResponse.type( category.getType() );
        categoryResponse.icon( category.getIcon() );
        categoryResponse.color( category.getColor() );

        return categoryResponse.build();
    }
}
