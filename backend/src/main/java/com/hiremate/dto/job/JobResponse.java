package com.hiremate.dto.job;

import com.hiremate.domain.enums.JobStatus;
import com.hiremate.domain.enums.JobType;
import com.hiremate.dto.auth.UserSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResponse {

    private Long id;
    private String title;
    private String companyName;
    private String location;
    private String description;
    private String requirements;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
    private JobType jobType;
    private JobStatus status;
    private UserSummaryResponse postedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
