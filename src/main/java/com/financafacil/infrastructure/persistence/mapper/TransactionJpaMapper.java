package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Transaction;
import com.financafacil.infrastructure.persistence.entity.TransactionJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionJpaMapper {
    TransactionJpaEntity toEntity(Transaction transaction);
    Transaction toDomain(TransactionJpaEntity entity);
}
