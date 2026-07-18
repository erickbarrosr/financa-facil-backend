package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.RecurringTransaction;
import com.financafacil.presentation.dto.response.RecurringTransactionResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RecurringTransactionPresentationMapper {
    RecurringTransactionResponse toResponse(RecurringTransaction recurringTransaction);
}
