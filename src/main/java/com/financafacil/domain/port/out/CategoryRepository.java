package com.financafacil.domain.port.out;

import com.financafacil.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id, UUID userId);
    List<Category> findAllByUserId(UUID userId);
    List<Category> findAllByUserIdAndType(UUID userId, String type);
    void deleteById(UUID id, UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
}
