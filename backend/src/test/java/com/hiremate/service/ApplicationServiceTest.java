package com.hiremate.service;

import com.hiremate.common.exception.ConflictException;
import com.hiremate.domain.entity.Application;
import com.hiremate.domain.entity.Job;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.enums.ApplicationStatus;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.dto.application.ApplicationCreateRequest;
import com.hiremate.dto.application.ApplicationResponse;
import com.hiremate.mapper.ApplicationMapper;
import com.hiremate.module.notification.service.EmailService;
import com.hiremate.module.notification.service.NotificationService;
import com.hiremate.repository.ApplicationRepository;
import com.hiremate.repository.JobRepository;
import com.hiremate.repository.UserRepository;
import com.hiremate.service.impl.ApplicationServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private NotificationService notificationService;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Test
    @DisplayName("Should submit application successfully")
    void testApplyForJobSuccess() {
        User candidate = User.builder().email("candidate@hiremate.com").fullName("Candidate").build();
        candidate.setId(5L);

        User recruiter = User.builder().email("recruiter@hiremate.com").fullName("Recruiter").build();
        recruiter.setId(10L);

        Job job = Job.builder().title("Staff Engineer").status(JobStatus.OPEN).postedBy(recruiter).build();
        job.setId(50L);

        ApplicationCreateRequest request = ApplicationCreateRequest.builder().jobId(50L).coverLetter("Cover Letter").build();

        Application savedApp = Application.builder().job(job).candidate(candidate).status(ApplicationStatus.APPLIED).build();
        savedApp.setId(500L);

        ApplicationResponse expectedResponse = ApplicationResponse.builder().id(500L).status(ApplicationStatus.APPLIED).build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(50L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobAndCandidate(job, candidate)).thenReturn(false);
        when(applicationRepository.save(any(Application.class))).thenReturn(savedApp);
        when(applicationMapper.toApplicationResponse(savedApp)).thenReturn(expectedResponse);

        ApplicationResponse result = applicationService.applyForJob(request, 5L);

        assertNotNull(result);
        assertEquals(500L, result.getId());
        verify(notificationService, times(1)).createNotification(eq(recruiter), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw ConflictException if already applied")
    void testApplyForJobConflict() {
        User candidate = User.builder().email("candidate@hiremate.com").build();
        candidate.setId(5L);
        Job job = Job.builder().title("Staff Engineer").status(JobStatus.OPEN).build();
        job.setId(50L);

        ApplicationCreateRequest request = ApplicationCreateRequest.builder().jobId(50L).build();

        when(userRepository.findById(5L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(50L)).thenReturn(Optional.of(job));
        when(applicationRepository.existsByJobAndCandidate(job, candidate)).thenReturn(true);

        assertThrows(ConflictException.class, () -> applicationService.applyForJob(request, 5L));
    }
}
