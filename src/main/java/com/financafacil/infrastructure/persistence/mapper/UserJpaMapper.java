package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.User;
import com.financafacil.infrastructure.persistence.entity.UserJpaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserJpaMapper {
    UserJpaEntity toEntity(User user);
    User toDomain(UserJpaEntity entity);
}
