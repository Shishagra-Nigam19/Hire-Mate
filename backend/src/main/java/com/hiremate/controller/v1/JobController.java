package com.hiremate.controller.v1;

import com.hiremate.common.constant.ApiConstants;
import com.hiremate.common.response.ApiResponse;
import com.hiremate.common.response.PagedResponse;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.domain.enums.JobType;
import com.hiremate.dto.job.JobCreateRequest;
import com.hiremate.dto.job.JobResponse;
import com.hiremate.dto.job.JobSearchFilter;
import com.hiremate.dto.job.JobUpdateRequest;
import com.hiremate.module.audit.annotation.Audit;
import com.hiremate.security.services.UserPrincipal;
import com.hiremate.service.JobService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Set;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX + "/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Posting Management", description = "Endpoints for creating, searching, updating, and deleting recruitment job postings")
public class JobController {

    private static final Set<String> ALLOWED_SORT_PROPERTIES = Set.of(
            "id", "title", "companyName", "location", "minSalary", "maxSalary", "createdAt", "updatedAt", "jobType", "status"
    );

    private final JobService jobService;

    @PostMapping
    @Audit(action = "JOB_CREATE", entityType = "Job")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    @Operation(summary = "Create a new job posting (Recruiter/Admin)")
    public ResponseEntity<ApiResponse<JobResponse>> createJob(
            @Valid @RequestBody JobCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        JobResponse response = jobService.createJob(request, userPrincipal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Job created successfully", response));
    }

    @GetMapping
    @Operation(summary = "Search job postings with dynamic criteria filtering, pagination, and sorting")
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> searchJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String companyName,
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) JobStatus status,
            @RequestParam(required = false) BigDecimal minSalary,
            @RequestParam(required = false) BigDecimal maxSalary,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_BY) String sortBy,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_DIRECTION) String sortDir) {

        int clampedSize = Math.min(Math.max(1, size), ApiConstants.MAX_PAGE_SIZE);
        int clampedPage = Math.max(0, page);

        Sort sort = buildSort(sortBy, sortDir);
        Pageable pageable = PageRequest.of(clampedPage, clampedSize, sort);

        JobSearchFilter filter = JobSearchFilter.builder()
                .search(search)
                .location(location)
                .companyName(companyName)
                .jobType(jobType)
                .status(status)
                .minSalary(minSalary)
                .maxSalary(maxSalary)
                .build();

        Page<JobResponse> jobPage = jobService.searchJobs(filter, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(jobPage)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get detailed job posting by ID")
    public ResponseEntity<ApiResponse<JobResponse>> getJobById(@PathVariable Long id) {
        JobResponse response = jobService.getJobById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-jobs")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    @Operation(summary = "Get jobs posted by currently logged in recruiter")
    public ResponseEntity<ApiResponse<PagedResponse<JobResponse>>> getMyJobs(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size) {

        int clampedSize = Math.min(Math.max(1, size), ApiConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(0, page), clampedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<JobResponse> jobPage = jobService.getJobsByRecruiter(userPrincipal.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(jobPage)));
    }

    @PutMapping("/{id}")
    @Audit(action = "JOB_UPDATE", entityType = "Job")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    @Operation(summary = "Update job posting details")
    public ResponseEntity<ApiResponse<JobResponse>> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        JobResponse response = jobService.updateJob(id, request, userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Job updated successfully", response));
    }

    @DeleteMapping("/{id}")
    @Audit(action = "JOB_DELETE", entityType = "Job")
    @PreAuthorize("hasRole('RECRUITER') or hasRole('ADMIN')")
    @Operation(summary = "Delete job posting by ID (Soft delete)")
    public ResponseEntity<ApiResponse<Void>> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        boolean isAdmin = userPrincipal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        jobService.deleteJob(id, userPrincipal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Job deleted successfully", null));
    }

    private Sort buildSort(String sortBy, String sortDir) {
        String safeSortBy = ALLOWED_SORT_PROPERTIES.contains(sortBy) ? sortBy : ApiConstants.DEFAULT_SORT_BY;
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, safeSortBy);
    }
}
