package com.hiremate.module.storage.service.impl;

import com.hiremate.common.exception.BadRequestException;
import com.hiremate.common.exception.ResourceNotFoundException;
import com.hiremate.module.storage.dto.FileUploadResponse;
import com.hiremate.module.storage.service.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.s3.enabled", havingValue = "false", matchIfMissing = true)
public class LocalStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "doc", "docx");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final Path storageLocation;

    public LocalStorageServiceImpl(@Value("${app.storage.local-dir:./uploads/resumes}") String localDir) {
        this.storageLocation = Paths.get(localDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageLocation);
        } catch (IOException ex) {
            log.error("Could not create local upload directory: {}", this.storageLocation, ex);
        }
    }

    @Override
    public FileUploadResponse uploadResume(MultipartFile file, Long userId) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(originalFilename);
        String fileKey = "resumes/user_" + userId + "_" + UUID.randomUUID() + "." + extension;

        try {
            Path targetLocation = this.storageLocation.resolve(fileKey.replace("resumes/", ""));
            Files.createDirectories(targetLocation.getParent());
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Uploaded resume locally: {} for user ID: {}", fileKey, userId);

            return FileUploadResponse.builder()
                    .fileName(originalFilename)
                    .fileKey(fileKey)
                    .fileUrl("/api/v1/resumes/download/" + fileKey.replace("resumes/", ""))
                    .contentType(file.getContentType())
                    .sizeBytes(file.getSize())
                    .build();

        } catch (IOException ex) {
            log.error("Failed to store file {}", originalFilename, ex);
            throw new BadRequestException("Could not store file " + originalFilename + ". Please try again!");
        }
    }

    @Override
    public Resource loadFileAsResource(String fileKey) {
        try {
            Path filePath = this.storageLocation.resolve(fileKey.replace("resumes/", "")).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Resume file not found: " + fileKey);
            }
        } catch (MalformedURLException ex) {
            throw new ResourceNotFoundException("Resume file not found: " + fileKey);
        }
    }

    @Override
    public void deleteFile(String fileKey) {
        try {
            Path filePath = this.storageLocation.resolve(fileKey.replace("resumes/", "")).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            log.warn("Could not delete resume file: {}", fileKey, ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Failed to upload empty file");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 5 MB");
        }

        String originalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(originalFilename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Invalid file extension: ." + extension + ". Allowed extensions: PDF, DOC, DOCX");
        }

        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Invalid content type: " + contentType);
        }

        // Magic Bytes Validation to prevent file extension spoofing
        verifyMagicBytes(file, extension);
    }

    private void verifyMagicBytes(MultipartFile file, String extension) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int read = is.read(header);

            if (read < 4) {
                throw new BadRequestException("Corrupted or invalid file header");
            }

            if ("pdf".equalsIgnoreCase(extension)) {
                // PDF magic bytes: %PDF (0x25 0x50 0x44 0x46)
                if (header[0] != 0x25 || header[1] != 0x50 || header[2] != 0x44 || header[3] != 0x46) {
                    throw new BadRequestException("File header does not match genuine PDF format (Magic bytes check failed)");
                }
            } else if ("docx".equalsIgnoreCase(extension)) {
                // DOCX magic bytes (ZIP header PK..): 0x50 0x4B 0x03 0x04
                if (header[0] != 0x50 || header[1] != 0x4B || header[2] != 0x03 || header[3] != 0x04) {
                    throw new BadRequestException("File header does not match genuine DOCX format (Magic bytes check failed)");
                }
            }
        } catch (IOException e) {
            throw new BadRequestException("Unable to inspect file headers");
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}
