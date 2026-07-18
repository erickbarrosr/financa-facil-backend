package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.User;
import com.financafacil.infrastructure.persistence.entity.UserJpaEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:28-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class UserJpaMapperImpl implements UserJpaMapper {

    @Override
    public UserJpaEntity toEntity(User user) {
        if ( user == null ) {
            return null;
        }

        UserJpaEntity.UserJpaEntityBuilder userJpaEntity = UserJpaEntity.builder();

        userJpaEntity.id( user.getId() );
        userJpaEntity.name( user.getName() );
        userJpaEntity.email( user.getEmail() );
        userJpaEntity.passwordHash( user.getPasswordHash() );
        userJpaEntity.createdAt( user.getCreatedAt() );
        userJpaEntity.updatedAt( user.getUpdatedAt() );
        userJpaEntity.lastLoginAt( user.getLastLoginAt() );
        userJpaEntity.emailVerified( user.isEmailVerified() );
        userJpaEntity.active( user.isActive() );

        return userJpaEntity.build();
    }

    @Override
    public User toDomain(UserJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.id( entity.getId() );
        user.name( entity.getName() );
        user.email( entity.getEmail() );
        user.passwordHash( entity.getPasswordHash() );
        user.createdAt( entity.getCreatedAt() );
        user.updatedAt( entity.getUpdatedAt() );
        user.lastLoginAt( entity.getLastLoginAt() );
        user.emailVerified( entity.isEmailVerified() );
        user.active( entity.isActive() );

        return user.build();
    }
}
