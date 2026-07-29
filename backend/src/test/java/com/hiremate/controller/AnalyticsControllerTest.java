package com.hiremate.controller;

import com.hiremate.common.logging.RateLimitingFilter;
import com.hiremate.module.analytics.controller.AnalyticsController;
import com.hiremate.module.analytics.dto.CandidateAnalyticsResponse;
import com.hiremate.module.analytics.dto.RecruiterAnalyticsResponse;
import com.hiremate.module.analytics.service.AnalyticsService;
import com.hiremate.security.jwt.JwtAccessDeniedHandler;
import com.hiremate.security.jwt.JwtAuthenticationEntryPoint;
import com.hiremate.security.jwt.JwtAuthenticationFilter;
import com.hiremate.security.jwt.JwtTokenProvider;
import com.hiremate.security.services.UserDetailsServiceImpl;
import com.hiremate.security.services.UserPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnalyticsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AnalyticsService analyticsService;

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
    @DisplayName("GET /api/v1/analytics/recruiter - Should return recruiter dashboard analytics")
    void testGetRecruiterAnalytics() throws Exception {
        UserPrincipal principal = UserPrincipal.builder()
                .id(1L)
                .email("recruiter@hiremate.com")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_RECRUITER")))
                .build();

        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));

        RecruiterAnalyticsResponse response = RecruiterAnalyticsResponse.builder()
                .totalActiveJobs(5)
                .totalApplicationsReceived(42)
                .build();

        when(analyticsService.getRecruiterAnalytics(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/analytics/recruiter")
                        .principal(new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalActiveJobs").value(5))
                .andExpect(jsonPath("$.data.totalApplicationsReceived").value(42));
    }
}
