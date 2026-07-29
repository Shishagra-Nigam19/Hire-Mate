package com.hiremate.module.analytics.controller;

import com.hiremate.common.constant.ApiConstants;
import com.hiremate.common.response.ApiResponse;
import com.hiremate.module.analytics.dto.CandidateAnalyticsResponse;
import com.hiremate.module.analytics.dto.RecruiterAnalyticsResponse;
import com.hiremate.module.analytics.service.AnalyticsService;
import com.hiremate.security.services.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX + "/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics & Reporting", description = "Endpoints for recruiter pipeline metrics and candidate application analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/recruiter")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    @Operation(summary = "Get recruiter dashboard metrics (Active jobs, application count, pipeline stage breakdown)")
    public ResponseEntity<ApiResponse<RecruiterAnalyticsResponse>> getRecruiterAnalytics(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        RecruiterAnalyticsResponse response = analyticsService.getRecruiterAnalytics(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/candidate")
    @PreAuthorize("hasRole('CANDIDATE') or hasRole('ADMIN')")
    @Operation(summary = "Get candidate dashboard analytics (Submitted count, shortlist rate, response rate)")
    public ResponseEntity<ApiResponse<CandidateAnalyticsResponse>> getCandidateAnalytics(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        CandidateAnalyticsResponse response = analyticsService.getCandidateAnalytics(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
