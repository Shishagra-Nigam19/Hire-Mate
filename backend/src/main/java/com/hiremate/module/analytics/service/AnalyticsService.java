package com.hiremate.module.analytics.service;

import com.hiremate.module.analytics.dto.CandidateAnalyticsResponse;
import com.hiremate.module.analytics.dto.RecruiterAnalyticsResponse;

public interface AnalyticsService {

    RecruiterAnalyticsResponse getRecruiterAnalytics(Long recruiterId);

    CandidateAnalyticsResponse getCandidateAnalytics(Long candidateId);
}
