package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Transaction;
import com.financafacil.presentation.dto.response.TransactionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionPresentationMapper {
    TransactionResponse toResponse(Transaction transaction);
}
