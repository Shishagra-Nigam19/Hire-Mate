package com.hiremate.service.impl;

import com.hiremate.common.exception.BadRequestException;
import com.hiremate.common.exception.ConflictException;
import com.hiremate.common.exception.ForbiddenException;
import com.hiremate.common.exception.ResourceNotFoundException;
import com.hiremate.common.sanitization.SanitizerUtil;
import com.hiremate.domain.entity.Application;
import com.hiremate.domain.entity.Job;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.enums.ApplicationStatus;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.dto.application.ApplicationCreateRequest;
import com.hiremate.dto.application.ApplicationResponse;
import com.hiremate.dto.application.ApplicationStatusUpdateRequest;
import com.hiremate.mapper.ApplicationMapper;
import com.hiremate.module.notification.service.EmailService;
import com.hiremate.module.notification.service.NotificationService;
import com.hiremate.repository.ApplicationRepository;
import com.hiremate.repository.JobRepository;
import com.hiremate.repository.UserRepository;
import com.hiremate.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ApplicationMapper applicationMapper;
    private final NotificationService notificationService;
    private final EmailService emailService;

    @Override
    @Transactional
    public ApplicationResponse applyForJob(ApplicationCreateRequest request, Long candidateId) {
        log.info("Candidate ID: {} applying for Job ID: {}", candidateId, request.getJobId());

        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", candidateId));

        Job job = jobRepository.findById(request.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", request.getJobId()));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BadRequestException("Cannot apply for job posting that is not OPEN");
        }

        if (applicationRepository.existsByJobAndCandidate(job, candidate)) {
            throw new ConflictException("You have already submitted an application for this job posting");
        }

        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.APPLIED)
                .coverLetter(SanitizerUtil.sanitize(request.getCoverLetter()))
                .resumeUrl(SanitizerUtil.sanitizeStrict(request.getResumeUrl()))
                .build();

        Application savedApplication = applicationRepository.save(application);

        // Notify Recruiter in-app
        notificationService.createNotification(
                job.getPostedBy(),
                "New Application Received",
                candidate.getFullName() + " applied for " + job.getTitle(),
                "APPLICATION_RECEIVED"
        );

        log.info("Application created successfully with ID: {}", savedApplication.getId());
        return applicationMapper.toApplicationResponse(savedApplication);
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id, Long currentUserId, boolean isAdmin) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));

        boolean isCandidate = application.getCandidate().getId().equals(currentUserId);
        boolean isJobPoster = application.getJob().getPostedBy().getId().equals(currentUserId);

        if (!isAdmin && !isCandidate && !isJobPoster) {
            throw new ForbiddenException("You are not authorized to view this application");
        }

        return applicationMapper.toApplicationResponse(application);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByCandidate(Long candidateId, Pageable pageable) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", candidateId));

        return applicationRepository.findByCandidate(candidate, pageable)
                .map(applicationMapper::toApplicationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByJob(Long jobId, Long recruiterId, boolean isAdmin, Pageable pageable) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job", "id", jobId));

        if (!isAdmin && !job.getPostedBy().getId().equals(recruiterId)) {
            throw new ForbiddenException("You are not authorized to view applications for this job posting");
        }

        return applicationRepository.findByJob(job, pageable)
                .map(applicationMapper::toApplicationResponse);
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long id, ApplicationStatusUpdateRequest request, Long recruiterId, boolean isAdmin) {
        log.info("Updating status for application ID: {} by user ID: {}", id, recruiterId);

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application", "id", id));

        if (!isAdmin && !application.getJob().getPostedBy().getId().equals(recruiterId)) {
            throw new ForbiddenException("You are not authorized to update applications for this job posting");
        }

        application.setStatus(request.getStatus());
        if (request.getRecruiterNotes() != null) {
            application.setRecruiterNotes(SanitizerUtil.sanitize(request.getRecruiterNotes()));
        }

        Application updated = applicationRepository.save(application);

        // Notify Candidate via in-app & email
        notificationService.createNotification(
                application.getCandidate(),
                "Application Status Update",
                "Your application for " + application.getJob().getTitle() + " has been updated to " + request.getStatus().name(),
                "APPLICATION_STATUS_CHANGED"
        );

        emailService.sendApplicationStatusNotification(
                application.getCandidate().getEmail(),
                application.getCandidate().getFullName(),
                application.getJob().getTitle(),
                request.getStatus().name()
        );

        return applicationMapper.toApplicationResponse(updated);
    }
}
