package com.financafacil.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@With
public class Category {
    private final UUID id;
    private final UUID userId;
    private final String name;
    private final String type;
    private final String icon;
    private final String color;
    private final Instant createdAt;
    private final Instant updatedAt;
}
