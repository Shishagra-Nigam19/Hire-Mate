package com.hiremate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hiremate.common.logging.RateLimitingFilter;
import com.hiremate.controller.v1.JobController;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.domain.enums.JobType;
import com.hiremate.dto.job.JobResponse;
import com.hiremate.dto.job.JobSearchFilter;
import com.hiremate.security.jwt.JwtAccessDeniedHandler;
import com.hiremate.security.jwt.JwtAuthenticationEntryPoint;
import com.hiremate.security.jwt.JwtAuthenticationFilter;
import com.hiremate.security.jwt.JwtTokenProvider;
import com.hiremate.security.services.UserDetailsServiceImpl;
import com.hiremate.service.JobService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JobController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JobService jobService;

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
    @DisplayName("GET /api/v1/jobs - Should search and return paginated jobs")
    void testSearchJobs() throws Exception {
        JobResponse job = JobResponse.builder()
                .id(1L)
                .title("Senior Cloud Architect")
                .companyName("Amazon")
                .location("Seattle")
                .jobType(JobType.FULL_TIME)
                .status(JobStatus.OPEN)
                .build();

        when(jobService.searchJobs(any(JobSearchFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(job)));

        mockMvc.perform(get("/api/v1/jobs")
                        .param("search", "Cloud")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].title").value("Senior Cloud Architect"));
    }

    @Test
    @DisplayName("GET /api/v1/jobs/{id} - Should return job by ID")
    void testGetJobById() throws Exception {
        JobResponse job = JobResponse.builder()
                .id(1L)
                .title("Senior Cloud Architect")
                .companyName("Amazon")
                .build();

        when(jobService.getJobById(1L)).thenReturn(job);

        mockMvc.perform(get("/api/v1/jobs/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1));
    }
}
