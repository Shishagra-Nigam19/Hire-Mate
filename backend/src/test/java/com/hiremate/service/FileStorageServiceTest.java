package com.hiremate.service;

import com.hiremate.common.exception.BadRequestException;
import com.hiremate.module.storage.dto.FileUploadResponse;
import com.hiremate.module.storage.service.impl.LocalStorageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private LocalStorageServiceImpl fileStorageService;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        fileStorageService = new LocalStorageServiceImpl(tempDir.toString());
    }

    @Test
    @DisplayName("Should upload valid PDF resume with correct magic bytes")
    void testUploadResumeSuccess() {
        byte[] pdfMagicBytes = "%PDF-1.4 sample content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                pdfMagicBytes
        );

        FileUploadResponse response = fileStorageService.uploadResume(file, 1L);

        assertNotNull(response);
        assertEquals("resume.pdf", response.getFileName());
        assertTrue(response.getFileKey().startsWith("resumes/user_1_"));
    }

    @Test
    @DisplayName("Should reject file with invalid magic bytes (Extension spoofing attack)")
    void testUploadResumeInvalidMagicBytes() {
        byte[] fakePdfBytes = "MALICIOUS SCRIPT CONTENT".getBytes();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "malicious.pdf",
                "application/pdf",
                fakePdfBytes
        );

        assertThrows(BadRequestException.class, () -> fileStorageService.uploadResume(file, 1L));
    }

    @Test
    @DisplayName("Should reject file exceeding 5MB limit")
    void testUploadResumeExceedingSize() {
        byte[] largeBytes = new byte[6 * 1024 * 1024]; // 6MB
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large.pdf",
                "application/pdf",
                largeBytes
        );

        assertThrows(BadRequestException.class, () -> fileStorageService.uploadResume(file, 1L));
    }
}
