package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Transfer;
import com.financafacil.infrastructure.persistence.entity.TransferJpaEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:28-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class TransferJpaMapperImpl implements TransferJpaMapper {

    @Override
    public TransferJpaEntity toEntity(Transfer transfer) {
        if ( transfer == null ) {
            return null;
        }

        TransferJpaEntity.TransferJpaEntityBuilder transferJpaEntity = TransferJpaEntity.builder();

        transferJpaEntity.id( transfer.getId() );
        transferJpaEntity.userId( transfer.getUserId() );
        transferJpaEntity.fromAccountId( transfer.getFromAccountId() );
        transferJpaEntity.toAccountId( transfer.getToAccountId() );
        transferJpaEntity.amount( transfer.getAmount() );
        transferJpaEntity.transferDate( transfer.getTransferDate() );
        transferJpaEntity.description( transfer.getDescription() );
        transferJpaEntity.createdAt( transfer.getCreatedAt() );

        return transferJpaEntity.build();
    }

    @Override
    public Transfer toDomain(TransferJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Transfer.TransferBuilder transfer = Transfer.builder();

        transfer.id( entity.getId() );
        transfer.userId( entity.getUserId() );
        transfer.fromAccountId( entity.getFromAccountId() );
        transfer.toAccountId( entity.getToAccountId() );
        transfer.amount( entity.getAmount() );
        transfer.transferDate( entity.getTransferDate() );
        transfer.description( entity.getDescription() );
        transfer.createdAt( entity.getCreatedAt() );

        return transfer.build();
    }
}
