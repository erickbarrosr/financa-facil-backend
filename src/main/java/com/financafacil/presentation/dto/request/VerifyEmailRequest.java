package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class VerifyEmailRequest {
    @NotBlank private String token;
}
