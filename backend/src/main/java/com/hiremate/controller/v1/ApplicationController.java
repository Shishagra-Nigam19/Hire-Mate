package com.hiremate.controller.v1;

import com.hiremate.common.constant.ApiConstants;
import com.hiremate.common.response.ApiResponse;
import com.hiremate.common.response.PagedResponse;
import com.hiremate.dto.application.ApplicationCreateRequest;
import com.hiremate.dto.application.ApplicationResponse;
import com.hiremate.dto.application.ApplicationStatusUpdateRequest;
import com.hiremate.module.audit.annotation.Audit;
import com.hiremate.security.services.UserPrincipal;
import com.hiremate.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX + "/applications")
@RequiredArgsConstructor
@Tag(name = "Application Management", description = "Endpoints for candidates to apply for jobs and recruiters to manage job applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @Audit(action = "APPLICATION_SUBMIT", entityType = "Application")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Submit a job application (Candidate only)")
    public ResponseEntity<ApiResponse<ApplicationResponse>> applyForJob(
            @Valid @RequestBody ApplicationCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        ApplicationResponse response = applicationService.applyForJob(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Application submitted successfully", response));
    }

    @GetMapping("/my-applications")
    @PreAuthorize("hasRole('CANDIDATE')")
    @Operation(summary = "Get list of applications submitted by candidate")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationResponse>>> getMyApplications(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size) {

        int clampedSize = Math.min(Math.max(1, size), ApiConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(0, page), clampedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ApplicationResponse> appPage = applicationService.getApplicationsByCandidate(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(appPage)));
    }

    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    @Operation(summary = "Get applications submitted for a specific job posting (Recruiter/Admin)")
    public ResponseEntity<ApiResponse<PagedResponse<ApplicationResponse>>> getApplicationsByJob(
            @PathVariable Long jobId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size) {

        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        int clampedSize = Math.min(Math.max(1, size), ApiConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(0, page), clampedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ApplicationResponse> appPage = applicationService.getApplicationsByJob(jobId, userPrincipal.getId(), isAdmin, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(appPage)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application details by ID")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        ApplicationResponse response = applicationService.getApplicationById(id, userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}/status")
    @Audit(action = "APPLICATION_STATUS_UPDATE", entityType = "Application")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    @Operation(summary = "Update candidate application status and recruiter notes")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationStatusUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        ApplicationResponse response = applicationService.updateApplicationStatus(id, request, userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Application status updated successfully", response));
    }
}
