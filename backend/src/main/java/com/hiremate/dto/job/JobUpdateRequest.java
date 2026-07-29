package com.hiremate.dto.job;

import com.hiremate.domain.enums.JobStatus;
import com.hiremate.domain.enums.JobType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobUpdateRequest {

    @Size(min = 3, max = 200, message = "Job title must be between 3 and 200 characters")
    private String title;

    private String companyName;
    private String location;
    private String description;
    private String requirements;

    @DecimalMin(value = "0.0", message = "Minimum salary must be non-negative")
    private BigDecimal minSalary;

    @DecimalMin(value = "0.0", message = "Maximum salary must be non-negative")
    private BigDecimal maxSalary;

    private JobType jobType;
    private JobStatus status;
}
