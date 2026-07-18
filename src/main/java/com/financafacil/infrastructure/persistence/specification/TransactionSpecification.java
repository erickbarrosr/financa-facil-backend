package com.financafacil.infrastructure.persistence.specification;

import com.financafacil.domain.port.out.TransactionRepository.TransactionFilter;
import com.financafacil.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.domain.Specification;
import java.util.UUID;

public class TransactionSpecification {

    public static Specification<TransactionJpaEntity> forUser(UUID userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    public static Specification<TransactionJpaEntity> withFilter(UUID userId, TransactionFilter filter) {
        Specification<TransactionJpaEntity> spec = forUser(userId);
        if (filter.startDate() != null) {
            spec = spec.and((r, q, cb) -> cb.greaterThanOrEqualTo(r.get("transactionDate"), filter.startDate()));
        }
        if (filter.endDate() != null) {
            spec = spec.and((r, q, cb) -> cb.lessThanOrEqualTo(r.get("transactionDate"), filter.endDate()));
        }
        if (filter.categoryId() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("categoryId"), filter.categoryId()));
        }
        if (filter.accountId() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("accountId"), filter.accountId()));
        }
        if (filter.type() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("type"), filter.type()));
        }
        if (filter.status() != null) {
            spec = spec.and((r, q, cb) -> cb.equal(r.get("status"), filter.status()));
        }
        return spec;
    }
}
