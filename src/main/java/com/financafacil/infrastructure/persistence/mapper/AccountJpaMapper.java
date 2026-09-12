package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Account;
import com.financafacil.infrastructure.persistence.entity.AccountJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountJpaMapper {
    AccountJpaEntity toEntity(Account account);
    Account toDomain(AccountJpaEntity entity);
}
