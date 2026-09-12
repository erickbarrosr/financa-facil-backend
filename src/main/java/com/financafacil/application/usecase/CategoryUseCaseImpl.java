package com.financafacil.application.usecase;

import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.model.Category;
import com.financafacil.domain.port.in.CategoryUseCase;
import com.financafacil.domain.port.out.CategoryRepository;
import com.financafacil.presentation.dto.response.CategoryResponse;
import com.financafacil.presentation.mapper.CategoryPresentationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryUseCaseImpl implements CategoryUseCase {
    private final CategoryRepository categoryRepository;
    private final CategoryPresentationMapper presentationMapper;

    @Override
    @Transactional
    public CategoryResponse create(UUID userId, String name, String type, String icon, String color) {
        var cat = categoryRepository.save(Category.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .name(name)
            .type(type)
            .icon(icon)
            .color(color)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build());
        return presentationMapper.toResponse(cat);
    }

    @Override
    public List<CategoryResponse> findAll(UUID userId, String type) {
        var list = type != null
            ? categoryRepository.findAllByUserIdAndType(userId, type)
            : categoryRepository.findAllByUserId(userId);
        return list.stream().map(presentationMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse update(UUID userId, UUID categoryId, String name, String icon, String color) {
        var cat = categoryRepository.findById(categoryId, userId)
            .orElseThrow(() -> new NotFoundException("Categoria não encontrada"));
        var updated = categoryRepository.save(cat
            .withName(name)
            .withIcon(icon)
            .withColor(color)
            .withUpdatedAt(Instant.now()));
        return presentationMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID categoryId) {
        if (!categoryRepository.existsByIdAndUserId(categoryId, userId)) {
            throw new NotFoundException("Categoria não encontrada");
        }
        categoryRepository.deleteById(categoryId, userId);
    }
}
