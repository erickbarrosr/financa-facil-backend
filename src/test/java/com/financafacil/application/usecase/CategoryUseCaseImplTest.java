package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Category;
import com.financafacil.domain.port.out.CategoryRepository;
import com.financafacil.presentation.mapper.CategoryPresentationMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryUseCaseImplTest {
    @Mock CategoryRepository categoryRepository;
    @Mock CategoryPresentationMapper presentationMapper;
    @InjectMocks CategoryUseCaseImpl categoryUseCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void findAll_returnsAllForUser() {
        var cat = buildCategory();
        when(categoryRepository.findAllByUserId(userId)).thenReturn(List.of(cat));
        var result = categoryUseCase.findAll(userId, null);
        assertThat(result).hasSize(1);
    }

    @Test
    void findAll_filtersByType_whenProvided() {
        var cat = buildCategory();
        when(categoryRepository.findAllByUserIdAndType(userId, "expense")).thenReturn(List.of(cat));
        categoryUseCase.findAll(userId, "expense");
        verify(categoryRepository).findAllByUserIdAndType(userId, "expense");
        verify(categoryRepository, never()).findAllByUserId(any());
    }

    @Test
    void delete_throwsNotFound_whenCategoryBelongsToOtherUser() {
        var catId = UUID.randomUUID();
        when(categoryRepository.existsByIdAndUserId(catId, userId)).thenReturn(false);
        assertThatThrownBy(() -> categoryUseCase.delete(userId, catId)).isInstanceOf(NotFoundException.class);
    }

    private Category buildCategory() {
        return Category.builder().id(UUID.randomUUID()).userId(userId).name("Test")
            .type("expense").icon("Star").color("#FF0000").createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }
}
