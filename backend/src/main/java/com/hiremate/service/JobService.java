package com.hiremate.service;

import com.hiremate.dto.job.JobCreateRequest;
import com.hiremate.dto.job.JobResponse;
import com.hiremate.dto.job.JobSearchFilter;
import com.hiremate.dto.job.JobUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JobService {

    JobResponse createJob(JobCreateRequest request, Long recruiterId);

    JobResponse getJobById(Long id);

    Page<JobResponse> searchJobs(JobSearchFilter filter, Pageable pageable);

    Page<JobResponse> getJobsByRecruiter(Long recruiterId, Pageable pageable);

    JobResponse updateJob(Long id, JobUpdateRequest request, Long recruiterId, boolean isAdmin);

    void deleteJob(Long id, Long recruiterId, boolean isAdmin);
}
