package com.hiremate.controller.v1;

import com.hiremate.common.constant.ApiConstants;
import com.hiremate.common.response.ApiResponse;
import com.hiremate.common.response.PagedResponse;
import com.hiremate.dto.user.UserResponse;
import com.hiremate.module.audit.entity.AuditLog;
import com.hiremate.module.audit.service.AuditLogService;
import com.hiremate.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX + "/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Tag(name = "Admin Operations", description = "Privileged system administrative management and audit log endpoints")
public class AdminController {

    private final UserService userService;
    private final AuditLogService auditLogService;

    @GetMapping("/users")
    @Operation(summary = "Get paginated list of all system registered users (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size) {

        int clampedSize = Math.min(Math.max(1, size), ApiConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(0, page), clampedSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<UserResponse> usersPage = userService.getAllUsers(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(usersPage)));
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get system security audit logs (Admin only)")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLog>>> getAuditLogs(
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE) int size) {

        int clampedSize = Math.min(Math.max(1, size), ApiConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(Math.max(0, page), clampedSize, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<AuditLog> auditPage = auditLogService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(auditPage)));
    }
}
