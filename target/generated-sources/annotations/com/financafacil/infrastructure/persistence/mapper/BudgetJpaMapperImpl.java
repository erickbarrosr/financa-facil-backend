package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Budget;
import com.financafacil.infrastructure.persistence.entity.BudgetJpaEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:29-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class BudgetJpaMapperImpl implements BudgetJpaMapper {

    @Override
    public BudgetJpaEntity toEntity(Budget budget) {
        if ( budget == null ) {
            return null;
        }

        BudgetJpaEntity.BudgetJpaEntityBuilder budgetJpaEntity = BudgetJpaEntity.builder();

        budgetJpaEntity.id( budget.getId() );
        budgetJpaEntity.userId( budget.getUserId() );
        budgetJpaEntity.categoryId( budget.getCategoryId() );
        budgetJpaEntity.amount( budget.getAmount() );
        budgetJpaEntity.createdAt( budget.getCreatedAt() );
        budgetJpaEntity.updatedAt( budget.getUpdatedAt() );

        budgetJpaEntity.month( (short) budget.getMonth() );
        budgetJpaEntity.year( (short) budget.getYear() );

        return budgetJpaEntity.build();
    }

    @Override
    public Budget toDomain(BudgetJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Budget.BudgetBuilder budget = Budget.builder();

        budget.id( entity.getId() );
        budget.userId( entity.getUserId() );
        budget.categoryId( entity.getCategoryId() );
        budget.amount( entity.getAmount() );
        budget.createdAt( entity.getCreatedAt() );
        budget.updatedAt( entity.getUpdatedAt() );

        budget.month( (int) entity.getMonth() );
        budget.year( (int) entity.getYear() );

        return budget.build();
    }
}
