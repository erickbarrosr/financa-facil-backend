package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Account;
import com.financafacil.presentation.dto.response.AccountResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccountPresentationMapper {
    AccountResponse toResponse(Account account);
}
