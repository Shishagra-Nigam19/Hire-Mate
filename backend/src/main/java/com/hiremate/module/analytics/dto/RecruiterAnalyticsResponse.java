package com.hiremate.module.analytics.dto;

import com.hiremate.domain.enums.ApplicationStatus;
import com.hiremate.dto.job.JobResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecruiterAnalyticsResponse {

    private long totalActiveJobs;
    private long totalApplicationsReceived;
    private Map<ApplicationStatus, Long> applicationsByStatus;
    private List<JobResponse> topPerformingJobs;
    private double avgApplicationsPerJob;
}
