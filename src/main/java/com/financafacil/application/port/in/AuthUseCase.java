package com.financafacil.application.port.in;

import com.financafacil.application.dto.AuthResponse;
import com.financafacil.application.dto.UserResponse;
import java.util.UUID;

public interface AuthUseCase {
    AuthResponse register(String name, String email, String password);
    AuthResponse login(String email, String password, String ip, String userAgent);
    AuthResponse refresh(String rawRefreshToken);
    void logout(String rawRefreshToken);
    void forgotPassword(String email);
    void resetPassword(String token, String newPassword);
    void verifyEmail(String token);
    UserResponse me(UUID userId);
}
