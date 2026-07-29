package com.hiremate.service.impl;

import com.hiremate.common.exception.BadRequestException;
import com.hiremate.common.exception.ConflictException;
import com.hiremate.common.exception.ResourceNotFoundException;
import com.hiremate.common.exception.UnauthorizedException;
import com.hiremate.common.sanitization.SanitizerUtil;
import com.hiremate.domain.entity.PasswordResetToken;
import com.hiremate.domain.entity.RefreshToken;
import com.hiremate.domain.entity.Role;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.entity.VerificationToken;
import com.hiremate.domain.enums.RoleType;
import com.hiremate.dto.auth.ForgotPasswordRequest;
import com.hiremate.dto.auth.LoginRequest;
import com.hiremate.dto.auth.RefreshTokenRequest;
import com.hiremate.dto.auth.RegisterRequest;
import com.hiremate.dto.auth.ResetPasswordRequest;
import com.hiremate.dto.auth.TokenResponse;
import com.hiremate.dto.user.UserResponse;
import com.hiremate.mapper.UserMapper;
import com.hiremate.module.notification.service.EmailService;
import com.hiremate.repository.PasswordResetTokenRepository;
import com.hiremate.repository.RefreshTokenRepository;
import com.hiremate.repository.RoleRepository;
import com.hiremate.repository.UserRepository;
import com.hiremate.repository.VerificationTokenRepository;
import com.hiremate.security.jwt.JwtTokenProvider;
import com.hiremate.security.services.UserPrincipal;
import com.hiremate.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final UserMapper userMapper;
    private final EmailService emailService;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationInMs;

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered: " + request.getEmail());
        }

        RoleType requestedRole = request.getRole();
        if (requestedRole == RoleType.ROLE_ADMIN) {
            throw new BadRequestException("Direct registration for ROLE_ADMIN is not permitted");
        }

        Role role = roleRepository.findByName(requestedRole)
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .name(requestedRole)
                        .description("System role for " + requestedRole.name())
                        .build()));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(SanitizerUtil.sanitizeStrict(request.getFullName()))
                .companyName(SanitizerUtil.sanitizeStrict(request.getCompanyName()))
                .bio(SanitizerUtil.sanitize(request.getBio()))
                .enabled(true)
                .emailVerified(false)
                .roles(roles)
                .build();

        User savedUser = userRepository.save(user);

        // Generate email verification token
        String tokenStr = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(tokenStr)
                .user(savedUser)
                .expiryDate(Instant.now().plusSeconds(86400)) // 24 hours
                .used(false)
                .build();

        verificationTokenRepository.save(verificationToken);
        emailService.sendVerificationEmail(savedUser.getEmail(), savedUser.getFullName(), tokenStr);

        log.info("User registered successfully with ID: {}", savedUser.getId());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public TokenResponse login(LoginRequest request, String userAgent, String ipAddress) {
        log.info("Authenticating user: {}", request.getEmail());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase().trim(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userPrincipal.getId()));

        String accessToken = tokenProvider.generateToken(authentication);
        RefreshToken refreshToken = createRefreshToken(user, userAgent, ipAddress);

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresInMs(tokenProvider.getJwtExpirationInMs())
                .user(userMapper.toUserSummaryResponse(user))
                .build();
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenRequest request, String userAgent, String ipAddress) {
        String tokenString = request.getRefreshToken();

        RefreshToken refreshToken = refreshTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        User user = refreshToken.getUser();

        if (refreshToken.isRevoked()) {
            log.warn("SECURITY ALERT: Revoked refresh token reuse attempt detected for user: {}. Revoking all active tokens!", user.getEmail());
            refreshTokenRepository.revokeAllUserTokens(user);
            throw new UnauthorizedException("Refresh token was previously revoked. Security compromise detected, all sessions terminated.");
        }

        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new UnauthorizedException("Refresh token has expired. Please log in again.");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String newAccessToken = tokenProvider.generateTokenFromUserPrincipal(userPrincipal);
        RefreshToken newRefreshToken = createRefreshToken(user, userAgent, ipAddress);

        log.info("Successfully refreshed and rotated tokens for user ID: {}", user.getId());

        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresInMs(tokenProvider.getJwtExpirationInMs())
                .user(userMapper.toUserSummaryResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshTokenString) {
        if (refreshTokenString != null && !refreshTokenString.trim().isEmpty()) {
            refreshTokenRepository.findByToken(refreshTokenString)
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                        log.info("Refresh token revoked for user ID: {}", token.getUser().getId());
                    });
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid email verification token"));

        if (verificationToken.isUsed()) {
            throw new BadRequestException("Verification token has already been used");
        }

        if (verificationToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);

        log.info("Email verified successfully for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail().toLowerCase().trim());
        if (userOptional.isEmpty()) {
            log.info("Password reset requested for non-existent email: {}", request.getEmail());
            return; // Prevent user enumeration attacks
        }

        User user = userOptional.get();
        String tokenStr = UUID.randomUUID().toString();

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(tokenStr)
                .user(user)
                .expiryDate(Instant.now().plusSeconds(900)) // 15 minutes TTL
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(user.getEmail(), user.getFullName(), tokenStr);
        log.info("Password reset email sent to user ID: {}", user.getId());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token"));

        if (resetToken.isUsed() || resetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new BadRequestException("Password reset token is invalid or expired");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        // Security best practice: Revoke all active refresh tokens upon password reset
        refreshTokenRepository.revokeAllUserTokens(user);
        log.info("Password reset successfully for user ID: {}", user.getId());
    }

    private RefreshToken createRefreshToken(User user, String userAgent, String ipAddress) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpirationInMs))
                .revoked(false)
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
