package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.RecurringTransaction;
import com.financafacil.infrastructure.persistence.entity.RecurringTransactionJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecurringTransactionJpaMapper {
    RecurringTransactionJpaEntity toEntity(RecurringTransaction recurringTransaction);
    RecurringTransaction toDomain(RecurringTransactionJpaEntity entity);
}
