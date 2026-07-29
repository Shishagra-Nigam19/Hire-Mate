package com.hiremate.service.impl;

import com.hiremate.common.exception.ForbiddenException;
import com.hiremate.common.exception.ResourceNotFoundException;
import com.hiremate.common.sanitization.SanitizerUtil;
import com.hiremate.domain.entity.Job;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.dto.job.JobCreateRequest;
import com.hiremate.dto.job.JobResponse;
import com.hiremate.dto.job.JobSearchFilter;
import com.hiremate.dto.job.JobUpdateRequest;
import com.hiremate.mapper.JobMapper;
import com.hiremate.repository.JobRepository;
import com.hiremate.repository.UserRepository;
import com.hiremate.repository.specification.JobSpecification;
import com.hiremate.service.JobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional
    public JobResponse createJob(JobCreateRequest request, Long recruiterId) {
        log.info("Creating job posting titled: '{}' by user ID: {}", request.getTitle(), recruiterId);

        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recruiterId));

        Job job = jobMapper.toEntity(request);
        job.setTitle(SanitizerUtil.sanitizeStrict(request.getTitle()));
        job.setCompanyName(SanitizerUtil.sanitizeStrict(request.getCompanyName()));
        job.setLocation(SanitizerUtil.sanitizeStrict(request.getLocation()));
        job.setDescription(SanitizerUtil.sanitize(request.getDescription()));
        job.setRequirements(SanitizerUtil.sanitize(request.getRequirements()));
        job.setStatus(JobStatus.OPEN);
        job.setPostedBy(recruiter);

        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully with ID: {}", savedJob.getId());
        return jobMapper.toJobResponse(savedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));
        return jobMapper.toJobResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(JobSearchFilter filter, Pageable pageable) {
        Specification<Job> spec = JobSpecification.build(filter);
        return jobRepository.findAll(spec, pageable)
                .map(jobMapper::toJobResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getJobsByRecruiter(Long recruiterId, Pageable pageable) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recruiterId));
        return jobRepository.findByPostedBy(recruiter, pageable)
                .map(jobMapper::toJobResponse);
    }

    @Override
    @Transactional
    public JobResponse updateJob(Long id, JobUpdateRequest request, Long recruiterId, boolean isAdmin) {
        log.info("Updating job ID: {} by user ID: {}", id, recruiterId);

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));

        if (!isAdmin && !job.getPostedBy().getId().equals(recruiterId)) {
            throw new ForbiddenException("You are not authorized to update this job posting");
        }

        if (request.getTitle() != null) {
            job.setTitle(SanitizerUtil.sanitizeStrict(request.getTitle()));
        }
        if (request.getCompanyName() != null) {
            job.setCompanyName(SanitizerUtil.sanitizeStrict(request.getCompanyName()));
        }
        if (request.getLocation() != null) {
            job.setLocation(SanitizerUtil.sanitizeStrict(request.getLocation()));
        }
        if (request.getDescription() != null) {
            job.setDescription(SanitizerUtil.sanitize(request.getDescription()));
        }
        if (request.getRequirements() != null) {
            job.setRequirements(SanitizerUtil.sanitize(request.getRequirements()));
        }
        if (request.getMinSalary() != null) {
            job.setMinSalary(request.getMinSalary());
        }
        if (request.getMaxSalary() != null) {
            job.setMaxSalary(request.getMaxSalary());
        }
        if (request.getJobType() != null) {
            job.setJobType(request.getJobType());
        }
        if (request.getStatus() != null) {
            job.setStatus(request.getStatus());
        }

        Job updatedJob = jobRepository.save(job);
        return jobMapper.toJobResponse(updatedJob);
    }

    @Override
    @Transactional
    public void deleteJob(Long id, Long recruiterId, boolean isAdmin) {
        log.info("Soft deleting job ID: {} by user ID: {}", id, recruiterId);

        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", id));

        if (!isAdmin && !job.getPostedBy().getId().equals(recruiterId)) {
            throw new ForbiddenException("You are not authorized to delete this job posting");
        }

        // Soft Delete implementation
        job.setDeleted(true);
        job.setDeletedAt(Instant.now());
        jobRepository.save(job);

        log.info("Job ID: {} soft deleted successfully", id);
    }
}
