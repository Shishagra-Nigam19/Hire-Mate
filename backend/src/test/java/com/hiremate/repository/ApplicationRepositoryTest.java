package com.hiremate.repository;

import com.hiremate.domain.entity.Application;
import com.hiremate.domain.entity.Job;
import com.hiremate.domain.entity.User;
import com.hiremate.domain.enums.ApplicationStatus;
import com.hiremate.domain.enums.JobStatus;
import com.hiremate.domain.enums.JobType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ApplicationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Test
    @DisplayName("Should check if application exists by job and candidate")
    void testExistsByJobAndCandidate() {
        User recruiter = User.builder().email("recruiter2@hiremate.com").password("pass").fullName("Recruiter").enabled(true).build();
        User candidate = User.builder().email("candidate2@hiremate.com").password("pass").fullName("Candidate").enabled(true).build();
        recruiter = entityManager.persistAndFlush(recruiter);
        candidate = entityManager.persistAndFlush(candidate);

        Job job = Job.builder()
                .title("Staff Engineer")
                .companyName("Meta")
                .location("Menlo Park")
                .description("Desc")
                .jobType(JobType.FULL_TIME)
                .status(JobStatus.OPEN)
                .postedBy(recruiter)
                .build();
        job = entityManager.persistAndFlush(job);

        Application application = Application.builder()
                .job(job)
                .candidate(candidate)
                .status(ApplicationStatus.APPLIED)
                .build();
        entityManager.persistAndFlush(application);

        assertTrue(applicationRepository.existsByJobAndCandidate(job, candidate));

        Page<Application> appPage = applicationRepository.findByCandidate(candidate, PageRequest.of(0, 10));
        assertEquals(1, appPage.getTotalElements());
    }
}
