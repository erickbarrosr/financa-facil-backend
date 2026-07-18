package com.financafacil.presentation.mapper;

import com.financafacil.domain.model.Transfer;
import com.financafacil.presentation.dto.response.TransferResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransferPresentationMapper {
    TransferResponse toResponse(Transfer transfer);
}
