package com.financafacil.infrastructure.persistence.specification;

import com.financafacil.domain.port.out.TransactionRepository.TransactionFilter;
import com.financafacil.infrastructure.persistence.entity.TransactionJpaEntity;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
class TransactionSpecificationTest {

    @Test
    void forUser_returnsNonNullSpecification() {
        var userId = UUID.randomUUID();
        Specification<TransactionJpaEntity> spec = TransactionSpecification.forUser(userId);
        assertThat(spec).isNotNull();
    }

    @Test
    void forUser_toPredicate_callsEqualWithUserId() {
        var userId = UUID.randomUUID();
        var spec = TransactionSpecification.forUser(userId);

        Root<TransactionJpaEntity> root = mock(Root.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get("userId")).thenReturn(path);
        when(cb.equal(path, userId)).thenReturn(predicate);

        var result = spec.toPredicate(root, query, cb);

        verify(cb).equal(path, userId);
        assertThat(result).isEqualTo(predicate);
    }

    @Test
    void withFilter_empty_returnsNonNullSpec() {
        var userId = UUID.randomUUID();
        var filter = new TransactionFilter(null, null, null, null, null, null);
        var spec = TransactionSpecification.withFilter(userId, filter);
        assertThat(spec).isNotNull();
    }

    @Test
    void withFilter_empty_toPredicate_callsEqualWithUserId() {
        var userId = UUID.randomUUID();
        var filter = new TransactionFilter(null, null, null, null, null, null);
        var spec = TransactionSpecification.withFilter(userId, filter);

        Root<TransactionJpaEntity> root = mock(Root.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(predicate);

        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, userId);
    }

    @Test
    void withFilter_withStartAndEndDate_appliesDatePredicates() {
        var userId = UUID.randomUUID();
        var startDate = LocalDate.now().minusDays(7);
        var endDate = LocalDate.now();
        var filter = new TransactionFilter(startDate, endDate, null, null, null, null);
        var spec = TransactionSpecification.withFilter(userId, filter);

        Root<TransactionJpaEntity> root = mock(Root.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);
        when(cb.greaterThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(predicate);
        when(cb.lessThanOrEqualTo(any(Expression.class), any(LocalDate.class))).thenReturn(predicate);

        var result = spec.toPredicate(root, query, cb);

        verify(cb).greaterThanOrEqualTo(any(Expression.class), eq(startDate));
        verify(cb).lessThanOrEqualTo(any(Expression.class), eq(endDate));
        assertThat(result).isNotNull();
    }

    @Test
    void withFilter_withCategoryId_appliesCategoryFilter() {
        var userId = UUID.randomUUID();
        var categoryId = UUID.randomUUID();
        var filter = new TransactionFilter(null, null, categoryId, null, null, null);
        var spec = TransactionSpecification.withFilter(userId, filter);

        Root<TransactionJpaEntity> root = mock(Root.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, categoryId);
    }

    @Test
    void withFilter_withAccountId_appliesAccountFilter() {
        var userId = UUID.randomUUID();
        var accountId = UUID.randomUUID();
        var filter = new TransactionFilter(null, null, null, accountId, null, null);
        var spec = TransactionSpecification.withFilter(userId, filter);

        Root<TransactionJpaEntity> root = mock(Root.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, accountId);
    }

    @Test
    void withFilter_withType_appliesTypeFilter() {
        var userId = UUID.randomUUID();
        var filter = new TransactionFilter(null, null, null, null, "EXPENSE", null);
        var spec = TransactionSpecification.withFilter(userId, filter);

        Root<TransactionJpaEntity> root = mock(Root.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, "EXPENSE");
    }

    @Test
    void withFilter_withStatus_appliesStatusFilter() {
        var userId = UUID.randomUUID();
        var filter = new TransactionFilter(null, null, null, null, null, "CONFIRMED");
        var spec = TransactionSpecification.withFilter(userId, filter);

        Root<TransactionJpaEntity> root = mock(Root.class);
        CriteriaQuery query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path path = mock(Path.class);
        Predicate predicate = mock(Predicate.class);

        when(root.get(anyString())).thenReturn(path);
        when(cb.equal(any(), any())).thenReturn(predicate);
        when(cb.and(any(Predicate.class), any(Predicate.class))).thenReturn(predicate);

        spec.toPredicate(root, query, cb);

        verify(cb).equal(path, "CONFIRMED");
    }
}
