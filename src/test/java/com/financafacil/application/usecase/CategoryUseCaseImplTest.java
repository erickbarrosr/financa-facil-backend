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
    void create_savesWithGeneratedId() {
        var cat = buildCategory();
        when(categoryRepository.save(any())).thenReturn(cat);

        categoryUseCase.create(userId, "Food", "expense", "star", "#FF0000");

        var captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("Food");
        assertThat(captor.getValue().getType()).isEqualTo("expense");
        assertThat(captor.getValue().getId()).isNotNull();
    }

    @Test
    void update_throwsNotFound_whenCategoryNotFound() {
        var catId = UUID.randomUUID();
        when(categoryRepository.findById(catId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryUseCase.update(userId, catId, "New Name", "star", "#FF0000"))
            .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findAll_filtersByType_returnsResult() {
        var cat = buildCategory();
        when(categoryRepository.findAllByUserIdAndType(userId, "expense")).thenReturn(List.of(cat));
        when(presentationMapper.toResponse(cat)).thenReturn(
            com.financafacil.presentation.dto.response.CategoryResponse.builder()
                .id(cat.getId()).name(cat.getName()).type(cat.getType())
                .icon(cat.getIcon()).color(cat.getColor()).build());

        var result = categoryUseCase.findAll(userId, "expense");

        assertThat(result).hasSize(1);
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
