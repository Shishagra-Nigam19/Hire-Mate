package com.hiremate.service;

import com.hiremate.dto.auth.ForgotPasswordRequest;
import com.hiremate.dto.auth.LoginRequest;
import com.hiremate.dto.auth.RefreshTokenRequest;
import com.hiremate.dto.auth.RegisterRequest;
import com.hiremate.dto.auth.ResetPasswordRequest;
import com.hiremate.dto.auth.TokenResponse;
import com.hiremate.dto.user.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request, String userAgent, String ipAddress);

    TokenResponse refreshToken(RefreshTokenRequest request, String userAgent, String ipAddress);

    void logout(String refreshToken);

    void verifyEmail(String token);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
