package com.hiremate.repository.specification;

import com.hiremate.domain.entity.Job;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.domain.enums.JobType;
import com.hiremate.dto.job.JobSearchFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class JobSpecification {

    private JobSpecification() {
        // Prevent instantiation
    }

    public static Specification<Job> build(JobSearchFilter filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) {
                return cb.conjunction();
            }

            // Keyword search over title, description, and company name
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().trim().toLowerCase() + "%";
                Predicate titleLike = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descLike = cb.like(cb.lower(root.get("description")), searchPattern);
                Predicate companyLike = cb.like(cb.lower(root.get("companyName")), searchPattern);
                predicates.add(cb.or(titleLike, descLike, companyLike));
            }

            // Location filter
            if (filter.getLocation() != null && !filter.getLocation().trim().isEmpty()) {
                String locPattern = "%" + filter.getLocation().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("location")), locPattern));
            }

            // Company Name filter
            if (filter.getCompanyName() != null && !filter.getCompanyName().trim().isEmpty()) {
                String compPattern = "%" + filter.getCompanyName().trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("companyName")), compPattern));
            }

            // Job Type filter
            if (filter.getJobType() != null) {
                predicates.add(cb.equal(root.get("jobType"), filter.getJobType()));
            }

            // Job Status filter
            if (filter.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));
            }

            // Salary Range filters
            if (filter.getMinSalary() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("minSalary"), filter.getMinSalary()));
            }

            if (filter.getMaxSalary() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("maxSalary"), filter.getMaxSalary()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
