package com.hiremate.repository;

import com.hiremate.domain.entity.Job;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.domain.enums.JobType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class JobRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobRepository jobRepository;

    @Test
    @DisplayName("Should find jobs posted by recruiter")
    void testFindByPostedBy() {
        User recruiter = User.builder()
                .email("recruiter@hiremate.com")
                .password("encoded_pass")
                .fullName("Jane Recruiter")
                .enabled(true)
                .build();
        recruiter = entityManager.persistAndFlush(recruiter);

        Job job = Job.builder()
                .title("Senior Backend Engineer")
                .companyName("TechCorp")
                .location("Remote")
                .description("Build scalable distributed systems")
                .jobType(JobType.FULL_TIME)
                .status(JobStatus.OPEN)
                .minSalary(new BigDecimal("120000"))
                .maxSalary(new BigDecimal("180000"))
                .postedBy(recruiter)
                .build();
        entityManager.persistAndFlush(job);

        Page<Job> jobsPage = jobRepository.findByPostedBy(recruiter, PageRequest.of(0, 10));

        assertNotNull(jobsPage);
        assertEquals(1, jobsPage.getTotalElements());
        assertEquals("Senior Backend Engineer", jobsPage.getContent().get(0).getTitle());
    }
}
