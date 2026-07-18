package com.financafacil.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateAccountRequest {
    @NotBlank
    @Size(max = 150)
    private String name;

    @NotBlank
    @Pattern(regexp = "checking|wallet|credit_card|investment")
    private String type;

    @NotNull
    private BigDecimal initialBalance;

    @NotBlank
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    private String color;
}
