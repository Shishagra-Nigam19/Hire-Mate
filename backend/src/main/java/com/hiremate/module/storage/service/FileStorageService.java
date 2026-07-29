package com.hiremate.module.storage.service;

import com.hiremate.module.storage.dto.FileUploadResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    FileUploadResponse uploadResume(MultipartFile file, Long userId);

    Resource loadFileAsResource(String fileKey);

    void deleteFile(String fileKey);
}
