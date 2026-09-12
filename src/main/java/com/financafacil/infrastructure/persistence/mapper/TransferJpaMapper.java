package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Transfer;
import com.financafacil.infrastructure.persistence.entity.TransferJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferJpaMapper {
    TransferJpaEntity toEntity(Transfer transfer);
    Transfer toDomain(TransferJpaEntity entity);
}
