package com.hiremate.service;

import com.hiremate.common.exception.ForbiddenException;
import com.hiremate.common.exception.ResourceNotFoundException;
import com.hiremate.domain.entity.Job;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.domain.enums.JobType;
import com.hiremate.dto.job.JobCreateRequest;
import com.hiremate.dto.job.JobResponse;
import com.hiremate.dto.job.JobUpdateRequest;
import com.hiremate.mapper.JobMapper;
import com.hiremate.repository.JobRepository;
import com.hiremate.repository.UserRepository;
import com.hiremate.service.impl.JobServiceImpl;
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
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobServiceImpl jobService;

    @Test
    @DisplayName("Should create job posting successfully")
    void testCreateJobSuccess() {
        User recruiter = User.builder().email("recruiter@hiremate.com").fullName("Recruiter").build();
        recruiter.setId(10L);

        JobCreateRequest request = JobCreateRequest.builder()
                .title("Software Engineer")
                .companyName("Google")
                .location("Mountain View")
                .description("Build Next-gen Cloud Infrastructure")
                .jobType(JobType.FULL_TIME)
                .build();

        Job job = Job.builder().title("Software Engineer").companyName("Google").build();
        Job savedJob = Job.builder().title("Software Engineer").companyName("Google").status(JobStatus.OPEN).postedBy(recruiter).build();
        savedJob.setId(100L);

        JobResponse expectedResponse = JobResponse.builder().id(100L).title("Software Engineer").status(JobStatus.OPEN).build();

        when(userRepository.findById(10L)).thenReturn(Optional.of(recruiter));
        when(jobMapper.toEntity(request)).thenReturn(job);
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);
        when(jobMapper.toJobResponse(savedJob)).thenReturn(expectedResponse);

        JobResponse result = jobService.createJob(request, 10L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("Software Engineer", result.getTitle());
    }

    @Test
    @DisplayName("Should soft delete job posting")
    void testDeleteJobSoftDelete() {
        User recruiter = User.builder().email("recruiter@hiremate.com").build();
        recruiter.setId(10L);

        Job job = Job.builder().title("Software Engineer").postedBy(recruiter).build();
        job.setId(100L);

        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));

        jobService.deleteJob(100L, 10L, false);

        assertTrue(job.isDeleted());
        assertNotNull(job.getDeletedAt());
        verify(jobRepository, times(1)).save(job);
    }

    @Test
    @DisplayName("Should throw ForbiddenException when updating unowned job")
    void testUpdateJobForbidden() {
        User recruiter = User.builder().email("recruiter1@hiremate.com").build();
        recruiter.setId(10L);

        Job job = Job.builder().title("Software Engineer").postedBy(recruiter).build();
        job.setId(100L);

        JobUpdateRequest updateRequest = JobUpdateRequest.builder().title("Updated Title").build();

        when(jobRepository.findById(100L)).thenReturn(Optional.of(job));

        assertThrows(ForbiddenException.class, () -> jobService.updateJob(100L, updateRequest, 999L, false));
    }
}
