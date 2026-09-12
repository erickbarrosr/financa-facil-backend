package com.financafacil.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter @Builder
public class CategoryBreakdownResponse {
    private final UUID categoryId;
    private final String categoryName;
    private final String color;
    private final BigDecimal total;
}
