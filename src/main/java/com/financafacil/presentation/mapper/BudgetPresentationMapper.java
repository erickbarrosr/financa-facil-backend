package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Budget;
import com.financafacil.presentation.dto.response.BudgetResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BudgetPresentationMapper {
    BudgetResponse toResponse(Budget budget);
}
