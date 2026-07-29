package com.hiremate.dto.application;

import com.hiremate.domain.enums.ApplicationStatus;
import com.hiremate.dto.auth.UserSummaryResponse;
import com.hiremate.dto.job.JobResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private Long id;
    private JobResponse job;
    private UserSummaryResponse candidate;
    private ApplicationStatus status;
    private String coverLetter;
    private String resumeUrl;
    private String recruiterNotes;
    private Instant createdAt;
    private Instant updatedAt;
}
