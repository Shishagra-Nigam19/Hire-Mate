package com.hiremate.dto.job;

import com.hiremate.domain.enums.JobStatus;
import com.hiremate.domain.enums.JobType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSearchFilter {

    private String search;
    private String location;
    private String companyName;
    private JobType jobType;
    private JobStatus status;
    private BigDecimal minSalary;
    private BigDecimal maxSalary;
}
