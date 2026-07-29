package com.hiremate.module.storage.controller;

import com.hiremate.common.constant.ApiConstants;
import com.hiremate.common.response.ApiResponse;
import com.hiremate.module.storage.dto.FileUploadResponse;
import com.hiremate.module.storage.service.FileStorageService;
import com.hiremate.security.services.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(ApiConstants.API_V1_PREFIX + "/resumes")
@RequiredArgsConstructor
@Tag(name = "Resume Storage & File Management", description = "Endpoints for uploading, validating, and downloading candidate resumes")
public class StorageController {

    private final FileStorageService fileStorageService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('CANDIDATE') or hasRole('ADMIN')")
    @Operation(summary = "Upload and validate candidate resume (PDF/DOCX max 5MB)")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        FileUploadResponse response = fileStorageService.uploadResume(file, userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success("Resume uploaded successfully", response));
    }

    @GetMapping("/download/{filename:.+}")
    @Operation(summary = "Download resume file by filename key")
    public ResponseEntity<Resource> downloadResume(@PathVariable String filename) {
        String fileKey = "resumes/" + filename;
        Resource resource = fileStorageService.loadFileAsResource(fileKey);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
