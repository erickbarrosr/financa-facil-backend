package com.financafacil.application.usecase;

import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.exception.UnauthorizedException;
import com.financafacil.domain.model.User;
import com.financafacil.domain.port.out.*;
import com.financafacil.infrastructure.config.AppProperties;
import com.financafacil.infrastructure.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseImplTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock AuditLogRepository auditLogRepository;
    @Mock EmailService emailService;

    private AuthUseCaseImpl authUseCase;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        var props = new AppProperties();
        props.getJwt().setAccessSecret("test-access-secret-must-be-at-least-256-bits-long-ok");
        props.getJwt().setRefreshSecret("test-refresh-secret-must-be-at-least-256-bits-long-ok");
        props.getJwt().setAccessExpirationMs(900000L);
        props.getJwt().setRefreshExpirationMs(604800000L);
        props.getFrontend().setUrl("http://localhost:5173");
        jwtService = new JwtService(props);
        authUseCase = new AuthUseCaseImpl(userRepository, refreshTokenRepository,
            passwordResetTokenRepository, emailVerificationTokenRepository,
            auditLogRepository, emailService, jwtService, passwordEncoder, props);
    }

    @Test
    void register_throwsConflict_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);
        assertThatThrownBy(() -> authUseCase.register("Name", "existing@test.com", "password123"))
            .isInstanceOf(ConflictException.class);
    }

    @Test
    void register_savesUser_andSendsVerificationEmail() {
        when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
        var savedUser = User.builder().id(UUID.randomUUID()).name("Name").email("new@test.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .emailVerified(false).active(true).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(userRepository.save(any())).thenReturn(savedUser);

        var result = authUseCase.register("Name", "new@test.com", "password123");

        assertThat(result.getAccessToken()).isNotBlank();
        verify(emailVerificationTokenRepository).save(any(), eq(savedUser.getId()), any(), any());
        verify(emailService).sendEmailVerification(eq("new@test.com"), any());
    }

    @Test
    void login_throwsUnauthorized_whenUserNotFound() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authUseCase.login("notfound@test.com", "pass", "127.0.0.1", "agent"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_throwsUnauthorized_whenPasswordWrong() {
        var user = User.builder().id(UUID.randomUUID()).email("user@test.com")
            .passwordHash(passwordEncoder.encode("correct")).active(true)
            .emailVerified(true).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> authUseCase.login("user@test.com", "wrong", "127.0.0.1", "agent"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void forgotPassword_doesNotThrow_whenUserNotFound() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
        assertThatCode(() -> authUseCase.forgotPassword("ghost@test.com")).doesNotThrowAnyException();
        verify(emailService, never()).sendPasswordReset(any(), any());
    }

    @Test
    void login_returnsAuthResponse_whenCredentialsValid() {
        var userId = UUID.randomUUID();
        var encodedPassword = passwordEncoder.encode("password123");
        var user = User.builder().id(userId).name("Test").email("user@test.com")
            .passwordHash(encodedPassword).active(true).emailVerified(true)
            .createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        var result = authUseCase.login("user@test.com", "password123", "127.0.0.1", "agent");

        assertThat(result.getAccessToken()).isNotBlank();
        assertThat(result.getRefreshToken()).isNotBlank();
        verify(auditLogRepository).log(eq(userId), eq("LOGIN"), any(), any(), any(), eq("127.0.0.1"), eq("agent"));
    }

    @Test
    void login_throwsUnauthorized_whenUserInactive() {
        var user = User.builder().id(UUID.randomUUID()).email("inactive@test.com")
            .passwordHash(passwordEncoder.encode("pass")).active(false)
            .emailVerified(true).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(userRepository.findByEmail("inactive@test.com")).thenReturn(Optional.of(user));
        assertThatThrownBy(() -> authUseCase.login("inactive@test.com", "pass", null, null))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refresh_throwsUnauthorized_whenTokenNotFound() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authUseCase.refresh("invalid-token"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refresh_throwsUnauthorized_whenTokenRevoked() {
        var tokenData = new com.financafacil.domain.port.out.RefreshTokenRepository.RefreshTokenData(
            UUID.randomUUID(), UUID.randomUUID(), Instant.now().plusSeconds(3600), true);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(tokenData));
        assertThatThrownBy(() -> authUseCase.refresh("revoked-token"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refresh_throwsUnauthorized_whenTokenExpired() {
        var tokenData = new com.financafacil.domain.port.out.RefreshTokenRepository.RefreshTokenData(
            UUID.randomUUID(), UUID.randomUUID(), Instant.now().minusSeconds(1), false);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(tokenData));
        assertThatThrownBy(() -> authUseCase.refresh("expired-token"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void logout_revokesToken_whenFound() {
        var tokenData = new com.financafacil.domain.port.out.RefreshTokenRepository.RefreshTokenData(
            UUID.randomUUID(), UUID.randomUUID(), Instant.now().plusSeconds(3600), false);
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(tokenData));
        assertThatCode(() -> authUseCase.logout("valid-token")).doesNotThrowAnyException();
        verify(refreshTokenRepository).revokeById(tokenData.id());
    }

    @Test
    void logout_doesNotThrow_whenTokenNotFound() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());
        assertThatCode(() -> authUseCase.logout("unknown-token")).doesNotThrowAnyException();
    }

    @Test
    void resetPassword_throwsUnauthorized_whenTokenNotFound() {
        when(passwordResetTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authUseCase.resetPassword("bad-token", "newpass"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void resetPassword_throwsUnauthorized_whenTokenAlreadyUsed() {
        var tokenData = new com.financafacil.domain.port.out.PasswordResetTokenRepository.TokenData(
            UUID.randomUUID(), UUID.randomUUID(), Instant.now().plusSeconds(3600), true);
        when(passwordResetTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(tokenData));
        assertThatThrownBy(() -> authUseCase.resetPassword("used-token", "newpass"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void verifyEmail_throwsUnauthorized_whenTokenExpired() {
        var tokenData = new com.financafacil.domain.port.out.EmailVerificationTokenRepository.TokenData(
            UUID.randomUUID(), UUID.randomUUID(), Instant.now().minusSeconds(1), false);
        when(emailVerificationTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(tokenData));
        assertThatThrownBy(() -> authUseCase.verifyEmail("expired-token"))
            .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void me_returnsUserResponse_whenUserFound() {
        var userId = UUID.randomUUID();
        var user = User.builder().id(userId).name("Test User").email("me@test.com")
            .emailVerified(true).active(true).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        var result = authUseCase.me(userId);
        assertThat(result.getEmail()).isEqualTo("me@test.com");
        assertThat(result.isEmailVerified()).isTrue();
    }

    @Test
    void me_throwsNotFound_whenUserDoesNotExist() {
        var userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> authUseCase.me(userId))
            .isInstanceOf(com.financafacil.domain.exception.NotFoundException.class);
    }

    @Test
    void forgotPassword_sendsEmail_whenUserExists() {
        var user = User.builder().id(UUID.randomUUID()).email("existing@test.com")
            .active(true).emailVerified(true).createdAt(Instant.now()).updatedAt(Instant.now()).build();
        when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(user));
        authUseCase.forgotPassword("existing@test.com");
        verify(passwordResetTokenRepository).save(any(), eq(user.getId()), any(), any());
        verify(emailService).sendPasswordReset(eq("existing@test.com"), any());
    }
}
