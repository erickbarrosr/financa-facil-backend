package com.financafacil.presentation.controller;

import com.financafacil.application.dto.AuthResponse;
import com.financafacil.application.dto.UserResponse;
import com.financafacil.application.port.in.AuthUseCase;
import com.financafacil.presentation.dto.request.ForgotPasswordRequest;
import com.financafacil.presentation.dto.request.LoginRequest;
import com.financafacil.presentation.dto.request.LogoutRequest;
import com.financafacil.presentation.dto.request.RefreshRequest;
import com.financafacil.presentation.dto.request.RegisterRequest;
import com.financafacil.presentation.dto.request.ResetPasswordRequest;
import com.financafacil.presentation.dto.request.VerifyEmailRequest;
import com.financafacil.presentation.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authUseCase.register(req.getName(), req.getEmail(), req.getPassword()));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest req, HttpServletRequest httpReq) {
        return ApiResponse.ok(authUseCase.login(req.getEmail(), req.getPassword(),
            httpReq.getRemoteAddr(), httpReq.getHeader("User-Agent")));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        return ApiResponse.ok(authUseCase.refresh(req.getRefreshToken()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody LogoutRequest req) {
        authUseCase.logout(req.getRefreshToken());
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        authUseCase.forgotPassword(req.getEmail());
        return ApiResponse.ok(null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authUseCase.resetPassword(req.getToken(), req.getNewPassword());
        return ApiResponse.ok(null);
    }

    @PostMapping("/verify-email")
    public ApiResponse<Void> verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        authUseCase.verifyEmail(req.getToken());
        return ApiResponse.ok(null);
    }

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal Object principal) {
        var userId = (UUID) principal;
        return ApiResponse.ok(authUseCase.me(userId));
    }
}
