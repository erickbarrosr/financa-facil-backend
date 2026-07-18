package com.financafacil.application.usecase;

import com.financafacil.application.dto.AuthResponse;
import com.financafacil.application.dto.UserResponse;
import com.financafacil.domain.exception.ConflictException;
import com.financafacil.domain.exception.NotFoundException;
import com.financafacil.domain.exception.UnauthorizedException;
import com.financafacil.domain.model.User;
import com.financafacil.application.port.in.AuthUseCase;
import com.financafacil.domain.port.out.*;
import com.financafacil.infrastructure.config.AppProperties;
import com.financafacil.infrastructure.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthUseCaseImpl implements AuthUseCase {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public AuthResponse register(String name, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("E-mail já cadastrado");
        }
        var user = userRepository.save(User.builder()
            .id(UUID.randomUUID())
            .name(name)
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .emailVerified(false)
            .active(true)
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build());

        issueEmailVerificationToken(user);
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(String email, String password, String ip, String userAgent) {
        var user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));
        if (!user.isActive() || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }
        auditLogRepository.log(user.getId(), "LOGIN", "users", user.getId(), Map.of(), ip, userAgent);
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(String rawRefreshToken) {
        var tokenHash = hashToken(rawRefreshToken);
        var tokenData = refreshTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));
        if (tokenData.revoked() || tokenData.expiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Refresh token expirado ou revogado");
        }
        refreshTokenRepository.revokeById(tokenData.id());
        var user = userRepository.findById(tokenData.userId())
            .orElseThrow(() -> new UnauthorizedException("Usuário não encontrado"));
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public void logout(String rawRefreshToken) {
        var tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash)
            .ifPresent(t -> refreshTokenRepository.revokeById(t.id()));
    }

    @Override
    @Transactional
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            var rawToken = UUID.randomUUID().toString();
            var tokenHash = hashToken(rawToken);
            passwordResetTokenRepository.save(UUID.randomUUID(), user.getId(), tokenHash,
                Instant.now().plusSeconds(3600));
            var link = appProperties.getFrontend().getUrl() + "/reset-password?token=" + rawToken;
            emailService.sendPasswordReset(email, link);
        });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        var tokenHash = hashToken(token);
        var tokenData = passwordResetTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Token inválido"));
        if (tokenData.used() || tokenData.expiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Token expirado ou já utilizado");
        }
        var user = userRepository.findById(tokenData.userId())
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        userRepository.save(user.withPasswordHash(passwordEncoder.encode(newPassword)));
        passwordResetTokenRepository.markAsUsed(tokenData.id());
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        var tokenHash = hashToken(token);
        var tokenData = emailVerificationTokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> new UnauthorizedException("Token inválido"));
        if (tokenData.used() || tokenData.expiresAt().isBefore(Instant.now())) {
            throw new UnauthorizedException("Token expirado ou já utilizado");
        }
        var user = userRepository.findById(tokenData.userId())
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        userRepository.save(user.withEmailVerified(true));
        emailVerificationTokenRepository.markAsUsed(tokenData.id());
    }

    @Override
    public UserResponse me(UUID userId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
        return toUserResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        var accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        var rawRefresh = jwtService.generateRefreshToken();
        refreshTokenRepository.save(UUID.randomUUID(), user.getId(), hashToken(rawRefresh),
            Instant.now().plusMillis(appProperties.getJwt().getRefreshExpirationMs()));
        return AuthResponse.of(accessToken,
            appProperties.getJwt().getAccessExpirationMs() / 1000,
            toUserResponse(user));
    }

    private void issueEmailVerificationToken(User user) {
        var rawToken = UUID.randomUUID().toString();
        emailVerificationTokenRepository.save(UUID.randomUUID(), user.getId(), hashToken(rawToken),
            Instant.now().plusSeconds(86400));
        var link = appProperties.getFrontend().getUrl() + "/verify-email?token=" + rawToken;
        emailService.sendEmailVerification(user.getEmail(), link);
    }

    private String hashToken(String rawToken) {
        try {
            var digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .emailVerified(user.isEmailVerified())
            .build();
    }
}
