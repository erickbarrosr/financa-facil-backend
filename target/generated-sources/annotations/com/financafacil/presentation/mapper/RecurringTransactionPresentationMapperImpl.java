package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.RecurringTransaction;
import com.financafacil.presentation.dto.response.RecurringTransactionResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:28-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class RecurringTransactionPresentationMapperImpl implements RecurringTransactionPresentationMapper {

    @Override
    public RecurringTransactionResponse toResponse(RecurringTransaction recurringTransaction) {
        if ( recurringTransaction == null ) {
            return null;
        }

        RecurringTransactionResponse.RecurringTransactionResponseBuilder recurringTransactionResponse = RecurringTransactionResponse.builder();

        recurringTransactionResponse.id( recurringTransaction.getId() );
        recurringTransactionResponse.accountId( recurringTransaction.getAccountId() );
        recurringTransactionResponse.categoryId( recurringTransaction.getCategoryId() );
        recurringTransactionResponse.amount( recurringTransaction.getAmount() );
        recurringTransactionResponse.type( recurringTransaction.getType() );
        recurringTransactionResponse.frequency( recurringTransaction.getFrequency() );
        recurringTransactionResponse.nextExecution( recurringTransaction.getNextExecution() );
        recurringTransactionResponse.active( recurringTransaction.isActive() );
        recurringTransactionResponse.createdAt( recurringTransaction.getCreatedAt() );
        recurringTransactionResponse.updatedAt( recurringTransaction.getUpdatedAt() );

        return recurringTransactionResponse.build();
    }
}
