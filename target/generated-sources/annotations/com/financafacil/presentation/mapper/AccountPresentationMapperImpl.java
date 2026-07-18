package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Account;
import com.financafacil.presentation.dto.response.AccountResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:29-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class AccountPresentationMapperImpl implements AccountPresentationMapper {

    @Override
    public AccountResponse toResponse(Account account) {
        if ( account == null ) {
            return null;
        }

        AccountResponse.AccountResponseBuilder accountResponse = AccountResponse.builder();

        accountResponse.id( account.getId() );
        accountResponse.name( account.getName() );
        accountResponse.type( account.getType() );
        accountResponse.balance( account.getBalance() );
        accountResponse.color( account.getColor() );
        accountResponse.createdAt( account.getCreatedAt() );

        return accountResponse.build();
    }
}
