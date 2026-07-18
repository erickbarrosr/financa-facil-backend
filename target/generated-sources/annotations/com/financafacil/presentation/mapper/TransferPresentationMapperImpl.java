package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Transfer;
import com.financafacil.presentation.dto.response.TransferResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:28-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class TransferPresentationMapperImpl implements TransferPresentationMapper {

    @Override
    public TransferResponse toResponse(Transfer transfer) {
        if ( transfer == null ) {
            return null;
        }

        TransferResponse.TransferResponseBuilder transferResponse = TransferResponse.builder();

        transferResponse.id( transfer.getId() );
        transferResponse.fromAccountId( transfer.getFromAccountId() );
        transferResponse.toAccountId( transfer.getToAccountId() );
        transferResponse.amount( transfer.getAmount() );
        transferResponse.transferDate( transfer.getTransferDate() );
        transferResponse.description( transfer.getDescription() );
        transferResponse.createdAt( transfer.getCreatedAt() );

        return transferResponse.build();
    }
}
