package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.Transaction;
import com.financafacil.infrastructure.persistence.entity.TransactionJpaEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:29-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class TransactionJpaMapperImpl implements TransactionJpaMapper {

    @Override
    public TransactionJpaEntity toEntity(Transaction transaction) {
        if ( transaction == null ) {
            return null;
        }

        TransactionJpaEntity.TransactionJpaEntityBuilder transactionJpaEntity = TransactionJpaEntity.builder();

        transactionJpaEntity.id( transaction.getId() );
        transactionJpaEntity.userId( transaction.getUserId() );
        transactionJpaEntity.accountId( transaction.getAccountId() );
        transactionJpaEntity.categoryId( transaction.getCategoryId() );
        transactionJpaEntity.type( transaction.getType() );
        transactionJpaEntity.amount( transaction.getAmount() );
        transactionJpaEntity.description( transaction.getDescription() );
        transactionJpaEntity.transactionDate( transaction.getTransactionDate() );
        transactionJpaEntity.status( transaction.getStatus() );
        transactionJpaEntity.createdAt( transaction.getCreatedAt() );
        transactionJpaEntity.updatedAt( transaction.getUpdatedAt() );

        return transactionJpaEntity.build();
    }

    @Override
    public Transaction toDomain(TransactionJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Transaction.TransactionBuilder transaction = Transaction.builder();

        transaction.id( entity.getId() );
        transaction.userId( entity.getUserId() );
        transaction.accountId( entity.getAccountId() );
        transaction.categoryId( entity.getCategoryId() );
        transaction.type( entity.getType() );
        transaction.amount( entity.getAmount() );
        transaction.description( entity.getDescription() );
        transaction.transactionDate( entity.getTransactionDate() );
        transaction.status( entity.getStatus() );
        transaction.createdAt( entity.getCreatedAt() );
        transaction.updatedAt( entity.getUpdatedAt() );

        return transaction.build();
    }
}
