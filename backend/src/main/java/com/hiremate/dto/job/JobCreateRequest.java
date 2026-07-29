package com.hiremate.dto.job;

import com.hiremate.domain.enums.JobType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class JobCreateRequest {

    @NotBlank(message = "Job title is required")
    @Size(min = 3, max = 200, message = "Job title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Company name is required")
    private String companyName;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Job description is required")
    private String description;

    private String requirements;

    @DecimalMin(value = "0.0", message = "Minimum salary must be non-negative")
    private BigDecimal minSalary;

    @DecimalMin(value = "0.0", message = "Maximum salary must be non-negative")
    private BigDecimal maxSalary;

    @NotNull(message = "Job type is required")
    private JobType jobType;
}
