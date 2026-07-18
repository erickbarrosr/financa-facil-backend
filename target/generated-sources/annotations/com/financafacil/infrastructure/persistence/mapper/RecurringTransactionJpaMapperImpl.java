package com.financafacil.infrastructure.persistence.mapper;

import com.financafacil.domain.model.RecurringTransaction;
import com.financafacil.infrastructure.persistence.entity.RecurringTransactionJpaEntity;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-07-18T18:18:28-0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Ubuntu)"
)
@Component
public class RecurringTransactionJpaMapperImpl implements RecurringTransactionJpaMapper {

    @Override
    public RecurringTransactionJpaEntity toEntity(RecurringTransaction recurringTransaction) {
        if ( recurringTransaction == null ) {
            return null;
        }

        RecurringTransactionJpaEntity.RecurringTransactionJpaEntityBuilder recurringTransactionJpaEntity = RecurringTransactionJpaEntity.builder();

        recurringTransactionJpaEntity.id( recurringTransaction.getId() );
        recurringTransactionJpaEntity.userId( recurringTransaction.getUserId() );
        recurringTransactionJpaEntity.accountId( recurringTransaction.getAccountId() );
        recurringTransactionJpaEntity.categoryId( recurringTransaction.getCategoryId() );
        recurringTransactionJpaEntity.amount( recurringTransaction.getAmount() );
        recurringTransactionJpaEntity.type( recurringTransaction.getType() );
        recurringTransactionJpaEntity.frequency( recurringTransaction.getFrequency() );
        recurringTransactionJpaEntity.nextExecution( recurringTransaction.getNextExecution() );
        recurringTransactionJpaEntity.active( recurringTransaction.isActive() );
        recurringTransactionJpaEntity.createdAt( recurringTransaction.getCreatedAt() );
        recurringTransactionJpaEntity.updatedAt( recurringTransaction.getUpdatedAt() );

        return recurringTransactionJpaEntity.build();
    }

    @Override
    public RecurringTransaction toDomain(RecurringTransactionJpaEntity entity) {
        if ( entity == null ) {
            return null;
        }

        RecurringTransaction.RecurringTransactionBuilder recurringTransaction = RecurringTransaction.builder();

        recurringTransaction.id( entity.getId() );
        recurringTransaction.userId( entity.getUserId() );
        recurringTransaction.accountId( entity.getAccountId() );
        recurringTransaction.categoryId( entity.getCategoryId() );
        recurringTransaction.amount( entity.getAmount() );
        recurringTransaction.type( entity.getType() );
        recurringTransaction.frequency( entity.getFrequency() );
        recurringTransaction.nextExecution( entity.getNextExecution() );
        recurringTransaction.active( entity.isActive() );
        recurringTransaction.createdAt( entity.getCreatedAt() );
        recurringTransaction.updatedAt( entity.getUpdatedAt() );

        return recurringTransaction.build();
    }
}
