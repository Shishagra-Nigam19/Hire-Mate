package com.hiremate.service;

import com.hiremate.dto.application.ApplicationCreateRequest;
import com.hiremate.dto.application.ApplicationResponse;
import com.hiremate.dto.application.ApplicationStatusUpdateRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {

    ApplicationResponse applyForJob(ApplicationCreateRequest request, Long candidateId);

    ApplicationResponse getApplicationById(Long id, Long currentUserId, boolean isAdmin);

    Page<ApplicationResponse> getApplicationsByCandidate(Long candidateId, Pageable pageable);

    Page<ApplicationResponse> getApplicationsByJob(Long jobId, Long recruiterId, boolean isAdmin, Pageable pageable);

    ApplicationResponse updateApplicationStatus(Long id, ApplicationStatusUpdateRequest request, Long recruiterId, boolean isAdmin);
}
