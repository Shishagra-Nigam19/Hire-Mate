package com.hiremate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiremate.common.logging.RateLimitingFilter;
import com.hiremate.controller.v1.AuthController;
import com.hiremate.domain.enums.RoleType;
import com.hiremate.dto.auth.LoginRequest;
import com.hiremate.dto.auth.RegisterRequest;
import com.hiremate.dto.auth.TokenResponse;
import com.hiremate.dto.auth.UserSummaryResponse;
import com.hiremate.dto.user.UserResponse;
import com.hiremate.security.jwt.JwtAccessDeniedHandler;
import com.hiremate.security.jwt.JwtAuthenticationEntryPoint;
import com.hiremate.security.jwt.JwtAuthenticationFilter;
import com.hiremate.security.jwt.JwtTokenProvider;
import com.hiremate.security.services.UserDetailsServiceImpl;
import com.hiremate.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private RateLimitingFilter rateLimitingFilter;

    @Test
    @DisplayName("POST /api/v1/auth/register - Should register user successfully")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("newcandidate@hiremate.com")
                .password("Password123!")
                .fullName("New Candidate")
                .role(RoleType.ROLE_CANDIDATE)
                .build();

        UserResponse userResponse = UserResponse.builder()
                .id(1L)
                .email("newcandidate@hiremate.com")
                .fullName("New Candidate")
                .roles(Set.of("ROLE_CANDIDATE"))
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(userResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("newcandidate@hiremate.com"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return JWT tokens on login")
    void testLoginSuccess() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("user@hiremate.com")
                .password("Password123!")
                .build();

        TokenResponse tokenResponse = TokenResponse.builder()
                .accessToken("mock-access-token")
                .refreshToken("mock-refresh-token")
                .tokenType("Bearer")
                .expiresInMs(900000L)
                .user(UserSummaryResponse.builder().id(1L).email("user@hiremate.com").build())
                .build();

        when(authService.login(any(LoginRequest.class), anyString(), anyString())).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-access-token"));
    }
}
