package com.hiremate.repository;

import com.hiremate.domain.entity.Job;
import com.hiremate.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    @Override
    @EntityGraph(attributePaths = {"postedBy"})
    Optional<Job> findById(Long id);

    @EntityGraph(attributePaths = {"postedBy"})
    Page<Job> findByPostedBy(User postedBy, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"postedBy"})
    Page<Job> findAll(Specification<Job> spec, Pageable pageable);
}
