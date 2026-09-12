package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Budget;
import com.financafacil.infrastructure.persistence.entity.BudgetJpaEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetJpaMapper {
    @Mapping(target = "month", expression = "java((short) budget.getMonth())")
    @Mapping(target = "year", expression = "java((short) budget.getYear())")
    BudgetJpaEntity toEntity(Budget budget);

    @Mapping(target = "month", expression = "java((int) entity.getMonth())")
    @Mapping(target = "year", expression = "java((int) entity.getYear())")
    Budget toDomain(BudgetJpaEntity entity);
}
