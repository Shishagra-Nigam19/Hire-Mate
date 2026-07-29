package com.hiremate.service;

import com.hiremate.domain.entity.Application;
import com.hiremate.domain.entity.Job;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.enums.ApplicationStatus;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.mapper.JobMapper;
import com.hiremate.module.analytics.dto.CandidateAnalyticsResponse;
import com.hiremate.module.analytics.dto.RecruiterAnalyticsResponse;
import com.hiremate.module.analytics.service.impl.AnalyticsServiceImpl;
import com.hiremate.repository.ApplicationRepository;
import com.hiremate.repository.JobRepository;
import com.hiremate.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    @Test
    @DisplayName("Should compute recruiter analytics metrics correctly")
    void testGetRecruiterAnalytics() {
        User recruiter = User.builder().email("rec@hiremate.com").build();
        recruiter.setId(1L);

        Job job1 = Job.builder().title("Job 1").status(JobStatus.OPEN).postedBy(recruiter).build();
        job1.setId(10L);

        Application app1 = Application.builder().job(job1).status(ApplicationStatus.SHORTLISTED).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findByPostedBy(eq(recruiter), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(job1)));
        when(applicationRepository.findByJob(eq(job1), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(app1)));

        RecruiterAnalyticsResponse response = analyticsService.getRecruiterAnalytics(1L);

        assertNotNull(response);
        assertEquals(1, response.getTotalActiveJobs());
        assertEquals(1, response.getTotalApplicationsReceived());
        assertEquals(1L, response.getApplicationsByStatus().get(ApplicationStatus.SHORTLISTED));
    }

    @Test
    @DisplayName("Should compute candidate analytics metrics correctly")
    void testGetCandidateAnalytics() {
        User candidate = User.builder().email("cand@hiremate.com").build();
        candidate.setId(2L);

        Application app1 = Application.builder().candidate(candidate).status(ApplicationStatus.HIRED).build();

        when(userRepository.findById(2L)).thenReturn(Optional.of(candidate));
        when(applicationRepository.findByCandidate(eq(candidate), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(app1)));

        CandidateAnalyticsResponse response = analyticsService.getCandidateAnalytics(2L);

        assertNotNull(response);
        assertEquals(1, response.getTotalApplicationsSubmitted());
        assertEquals(1, response.getShortlistedOrHiredCount());
        assertEquals(100.0, response.getResponseRatePercentage());
    }
}
