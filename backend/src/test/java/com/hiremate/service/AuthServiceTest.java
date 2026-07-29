package com.hiremate.service;

import com.hiremate.domain.entity.RefreshToken;
import com.hiremate.domain.entity.Role;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.enums.RoleType;
import com.hiremate.dto.auth.LoginRequest;
import com.hiremate.dto.auth.RefreshTokenRequest;
import com.hiremate.dto.auth.RegisterRequest;
import com.hiremate.dto.auth.TokenResponse;
import com.hiremate.dto.auth.UserSummaryResponse;
import com.hiremate.dto.user.UserResponse;
import com.hiremate.mapper.UserMapper;
import com.hiremate.repository.RefreshTokenRepository;
import com.hiremate.repository.RoleRepository;
import com.hiremate.repository.UserRepository;
import com.hiremate.security.jwt.JwtTokenProvider;
import com.hiremate.security.services.UserPrincipal;
import com.hiremate.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshExpirationInMs", 604800000L);
    }

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test@hiremate.com")
                .password("Password123!")
                .fullName("John Doe")
                .role(RoleType.ROLE_CANDIDATE)
                .build();

        Role candidateRole = Role.builder().name(RoleType.ROLE_CANDIDATE).build();
        User savedUser = User.builder().id(1L).email("test@hiremate.com").fullName("John Doe").roles(Set.of(candidateRole)).build();
        UserResponse expectedResponse = UserResponse.builder().id(1L).email("test@hiremate.com").fullName("John Doe").roles(Set.of("ROLE_CANDIDATE")).build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(RoleType.ROLE_CANDIDATE)).thenReturn(Optional.of(candidateRole));
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toUserResponse(savedUser)).thenReturn(expectedResponse);

        UserResponse result = authService.register(request);

        assertNotNull(result);
        assertEquals("test@hiremate.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = LoginRequest.builder()
                .email("test@hiremate.com")
                .password("Password123!")
                .build();

        Role candidateRole = Role.builder().name(RoleType.ROLE_CANDIDATE).build();
        User user = User.builder().id(1L).email("test@hiremate.com").fullName("John Doe").roles(Set.of(candidateRole)).build();
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        RefreshToken refreshToken = RefreshToken.builder().id(10L).token("refresh-uuid").user(user).build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken(auth)).thenReturn("jwt-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(refreshToken);
        when(userMapper.toUserSummaryResponse(user)).thenReturn(UserSummaryResponse.builder().id(1L).email("test@hiremate.com").build());

        TokenResponse tokenResponse = authService.login(request, "Mozilla/5.0", "127.0.0.1");

        assertNotNull(tokenResponse);
        assertEquals("jwt-access-token", tokenResponse.getAccessToken());
        assertEquals("refresh-uuid", tokenResponse.getRefreshToken());
    }
}
