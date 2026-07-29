package com.hiremate.module.analytics.service.impl;

import com.hiremate.common.exception.ResourceNotFoundException;
import com.hiremate.domain.entity.Application;
import com.hiremate.domain.entity.Job;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.enums.ApplicationStatus;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.mapper.JobMapper;
import com.hiremate.module.analytics.dto.CandidateAnalyticsResponse;
import com.hiremate.module.analytics.dto.RecruiterAnalyticsResponse;
import com.hiremate.module.analytics.service.AnalyticsService;
import com.hiremate.repository.ApplicationRepository;
import com.hiremate.repository.JobRepository;
import com.hiremate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsServiceImpl implements AnalyticsService {

    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobMapper jobMapper;

    @Override
    @Transactional(readOnly = true)
    public RecruiterAnalyticsResponse getRecruiterAnalytics(Long recruiterId) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", recruiterId));

        Pageable pageable = PageRequest.of(0, 100);
        List<Job> recruiterJobs = jobRepository.findByPostedBy(recruiter, pageable).getContent();

        long activeJobsCount = recruiterJobs.stream()
                .filter(job -> job.getStatus() == JobStatus.OPEN)
                .count();

        Map<ApplicationStatus, Long> statusMap = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusMap.put(status, 0L);
        }

        long totalApplications = 0;
        for (Job job : recruiterJobs) {
            List<Application> jobApps = applicationRepository.findByJob(job, PageRequest.of(0, 500)).getContent();
            totalApplications += jobApps.size();
            for (Application app : jobApps) {
                statusMap.put(app.getStatus(), statusMap.getOrDefault(app.getStatus(), 0L) + 1);
            }
        }

        double avgApps = recruiterJobs.isEmpty() ? 0.0 : (double) totalApplications / recruiterJobs.size();

        return RecruiterAnalyticsResponse.builder()
                .totalActiveJobs(activeJobsCount)
                .totalApplicationsReceived(totalApplications)
                .applicationsByStatus(statusMap)
                .topPerformingJobs(recruiterJobs.stream().map(jobMapper::toJobResponse).limit(5).toList())
                .avgApplicationsPerJob(Math.round(avgApps * 100.0) / 100.0)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateAnalyticsResponse getCandidateAnalytics(Long candidateId) {
        User candidate = userRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", candidateId));

        List<Application> candidateApps = applicationRepository.findByCandidate(candidate, PageRequest.of(0, 500)).getContent();
        long totalSubmitted = candidateApps.size();

        Map<ApplicationStatus, Long> statusMap = new EnumMap<>(ApplicationStatus.class);
        for (ApplicationStatus status : ApplicationStatus.values()) {
            statusMap.put(status, 0L);
        }

        long shortlistedOrHired = 0;
        long reviewedCount = 0;

        for (Application app : candidateApps) {
            statusMap.put(app.getStatus(), statusMap.getOrDefault(app.getStatus(), 0L) + 1);
            if (app.getStatus() == ApplicationStatus.SHORTLISTED || app.getStatus() == ApplicationStatus.HIRED) {
                shortlistedOrHired++;
            }
            if (app.getStatus() != ApplicationStatus.APPLIED) {
                reviewedCount++;
            }
        }

        double responseRate = totalSubmitted == 0 ? 0.0 : ((double) reviewedCount / totalSubmitted) * 100.0;

        return CandidateAnalyticsResponse.builder()
                .totalApplicationsSubmitted(totalSubmitted)
                .applicationsByStatus(statusMap)
                .shortlistedOrHiredCount(shortlistedOrHired)
                .responseRatePercentage(Math.round(responseRate * 100.0) / 100.0)
                .build();
    }
}
