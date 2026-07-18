package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class CategoryResponse {
    private final UUID id;
    private final String name;
    private final String type;
    private final String icon;
    private final String color;
}
