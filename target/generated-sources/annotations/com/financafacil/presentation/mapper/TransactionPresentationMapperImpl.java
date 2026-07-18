package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Transaction;
import com.financafacil.presentation.dto.response.TransactionResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:29-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class TransactionPresentationMapperImpl implements TransactionPresentationMapper {

    @Override
    public TransactionResponse toResponse(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        TransactionResponse.TransactionResponseBuilder transactionResponse = TransactionResponse.builder();

        transactionResponse.id( transaction.getId() );
        transactionResponse.accountId( transaction.getAccountId() );
        transactionResponse.categoryId( transaction.getCategoryId() );
        transactionResponse.type( transaction.getType() );
        transactionResponse.amount( transaction.getAmount() );
        transactionResponse.description( transaction.getDescription() );
        transactionResponse.transactionDate( transaction.getTransactionDate() );
        transactionResponse.status( transaction.getStatus() );

        return transactionResponse.build();
    }
}
