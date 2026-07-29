package com.hiremate.controller.v1;

import com.hiremate.common.constant.ApiConstants;
import com.hiremate.common.response.ApiResponse;
import com.hiremate.dto.auth.ForgotPasswordRequest;
import com.hiremate.dto.auth.LoginRequest;
import com.hiremate.dto.auth.RefreshTokenRequest;
import com.hiremate.dto.auth.RegisterRequest;
import com.hiremate.dto.auth.ResetPasswordRequest;
import com.hiremate.dto.auth.TokenResponse;
import com.hiremate.dto.user.UserResponse;
import com.hiremate.module.audit.annotation.Audit;
import com.hiremate.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX + "/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication Management", description = "Endpoints for user registration, email verification, JWT login, password reset, and logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Audit(action = "USER_REGISTER", entityType = "User")
    @Operation(summary = "Register new Candidate or Recruiter account")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("User registered successfully. Please check your email for verification link.", response));
    }

    @PostMapping("/login")
    @Audit(action = "USER_LOGIN", entityType = "User")
    @Operation(summary = "Authenticate user and receive JWT access & refresh tokens")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIp(httpRequest);

        TokenResponse response = authService.login(request, userAgent, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Secure Refresh Token rotation flow")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest httpRequest) {
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = getClientIp(httpRequest);

        TokenResponse response = authService.refreshToken(request, userAgent, ipAddress);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/logout")
    @Audit(action = "USER_LOGOUT", entityType = "User")
    @Operation(summary = "Revoke active refresh token session")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null) {
            authService.logout(request.getRefreshToken());
        }
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/verify-email")
    @Audit(action = "VERIFY_EMAIL", entityType = "User")
    @Operation(summary = "Verify account email address via token link")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified successfully", null));
    }

    @PostMapping("/forgot-password")
    @Audit(action = "FORGOT_PASSWORD_REQUEST", entityType = "User")
    @Operation(summary = "Request password reset email link")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("If an account exists with that email, a password reset link has been sent.", null));
    }

    @PostMapping("/reset-password")
    @Audit(action = "RESET_PASSWORD_EXECUTE", entityType = "User")
    @Operation(summary = "Reset password using reset token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully. You can now log in with your new password.", null));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
