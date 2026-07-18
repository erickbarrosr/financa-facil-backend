package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Budget;
import com.financafacil.presentation.dto.response.BudgetResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:28-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class BudgetPresentationMapperImpl implements BudgetPresentationMapper {

    @Override
    public BudgetResponse toResponse(Budget budget) {
        if ( budget == null ) {
            return null;
        }

        BudgetResponse.BudgetResponseBuilder budgetResponse = BudgetResponse.builder();

        budgetResponse.id( budget.getId() );
        budgetResponse.categoryId( budget.getCategoryId() );
        budgetResponse.amount( budget.getAmount() );
        budgetResponse.month( budget.getMonth() );
        budgetResponse.year( budget.getYear() );
        budgetResponse.createdAt( budget.getCreatedAt() );
        budgetResponse.updatedAt( budget.getUpdatedAt() );

        return budgetResponse.build();
    }
}
