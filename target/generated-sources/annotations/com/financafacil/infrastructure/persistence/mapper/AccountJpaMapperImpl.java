package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Account;
import com.financafacil.infrastructure.persistence.entity.AccountJpaEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:29-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class AccountJpaMapperImpl implements AccountJpaMapper {

    @Override
    public AccountJpaEntity toEntity(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountJpaEntity.AccountJpaEntityBuilder accountJpaEntity = AccountJpaEntity.builder();

        accountJpaEntity.id( account.getId() );
        accountJpaEntity.userId( account.getUserId() );
        accountJpaEntity.name( account.getName() );
        accountJpaEntity.type( account.getType() );
        accountJpaEntity.balance( account.getBalance() );
        accountJpaEntity.color( account.getColor() );
        accountJpaEntity.createdAt( account.getCreatedAt() );
        accountJpaEntity.updatedAt( account.getUpdatedAt() );

        return accountJpaEntity.build();
    }

    @Override
    public Account toDomain(AccountJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Account.AccountBuilder account = Account.builder();

        account.id( entity.getId() );
        account.userId( entity.getUserId() );
        account.name( entity.getName() );
        account.type( entity.getType() );
        account.balance( entity.getBalance() );
        account.color( entity.getColor() );
        account.createdAt( entity.getCreatedAt() );
        account.updatedAt( entity.getUpdatedAt() );

        return account.build();
    }
}
