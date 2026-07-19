package com.financafacil.presentation.openapi;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Finança Fácil API")
                .description("Backend do clone do Organizze — gerenciamento financeiro pessoal")
                .version("v1")
                .contact(new Contact().name("Finança Fácil").email("contato@financafacil.app"))
                .license(new License().name("MIT")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Insira o access token JWT obtido em POST /auth/login")));
    }
}
