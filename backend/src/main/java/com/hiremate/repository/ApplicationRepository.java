package com.hiremate.repository;

import com.hiremate.domain.entity.Application;
import com.hiremate.domain.entity.Job;
import com.hiremate.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    @Override
    @EntityGraph(attributePaths = {"job", "job.postedBy", "candidate"})
    Optional<Application> findById(Long id);

    @EntityGraph(attributePaths = {"job", "job.postedBy", "candidate"})
    Page<Application> findByCandidate(User candidate, Pageable pageable);

    @EntityGraph(attributePaths = {"job", "job.postedBy", "candidate"})
    Page<Application> findByJob(Job job, Pageable pageable);

    boolean existsByJobAndCandidate(Job job, User candidate);

    @EntityGraph(attributePaths = {"job", "job.postedBy", "candidate"})
    Optional<Application> findByJobAndCandidate(Job job, User candidate);
}
