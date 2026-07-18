package com.financafacil.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final ErrorDetail error;
    private final Instant timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().success(true).data(data).message("OK").timestamp(Instant.now()).build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder().success(false)
            .error(new ErrorDetail(code, message)).timestamp(Instant.now()).build();
    }

    public record ErrorDetail(String code, String message) {}
}
