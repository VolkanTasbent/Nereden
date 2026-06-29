package com.nereden.api.application.service;

import com.nereden.api.application.dto.auth.AuthResponse;
import com.nereden.api.application.dto.auth.AuthTokensResponse;
import com.nereden.api.application.dto.auth.ForgotPasswordRequest;
import com.nereden.api.application.dto.auth.LoginRequest;
import com.nereden.api.application.dto.auth.RefreshTokenRequest;
import com.nereden.api.application.dto.auth.RegisterRequest;
import com.nereden.api.application.dto.auth.UserResponse;
import com.nereden.api.application.exception.EmailAlreadyExistsException;
import com.nereden.api.application.exception.InvalidCredentialsException;
import com.nereden.api.application.exception.InvalidTokenException;
import com.nereden.api.application.mapper.UserMapper;
import com.nereden.api.domain.entity.RefreshToken;
import com.nereden.api.domain.entity.User;
import com.nereden.api.domain.repository.RefreshTokenRepository;
import com.nereden.api.domain.repository.UserRepository;
import com.nereden.api.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        final String email = request.email().toLowerCase().trim();

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        final User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .build();

        final User saved = userRepository.save(user);
        return issueTokens(saved);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        final String email = request.email().toLowerCase().trim();
        final User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        refreshTokenRepository.deleteByUserId(user.getId());
        return issueTokens(user);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        log.info("Password reset requested for email: {}", request.email().toLowerCase());
    }

    public UserResponse getCurrentUser(UUID userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("Kullanıcı bulunamadı."));
        return UserMapper.toResponse(user);
    }

    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        final RefreshToken stored = refreshTokenRepository
                .findByTokenAndRevokedFalse(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Geçersiz refresh token."));

        if (stored.isExpired()) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new InvalidTokenException("Refresh token süresi dolmuş.");
        }

        if (!jwtService.isTokenValid(request.refreshToken())) {
            throw new InvalidTokenException("Geçersiz refresh token.");
        }

        final User user = stored.getUser();
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return issueTokens(user);
    }

    private AuthResponse issueTokens(User user) {
        final String accessToken = jwtService.generateAccessToken(
                user.getId(), user.getEmail(), user.getRole().name());
        final String refreshToken = jwtService.generateRefreshToken(user.getId());

        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshToken)
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .build());

        return UserMapper.toAuthResponse(user, accessToken, refreshToken);
    }
}
