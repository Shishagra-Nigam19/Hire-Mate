package com.hiremate.module.analytics.dto;

import com.hiremate.domain.enums.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAnalyticsResponse {

    private long totalApplicationsSubmitted;
    private Map<ApplicationStatus, Long> applicationsByStatus;
    private long shortlistedOrHiredCount;
    private double responseRatePercentage;
}
