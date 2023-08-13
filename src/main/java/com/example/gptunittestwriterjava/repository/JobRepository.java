package com.example.gptunittestwriterjava.repository;

import com.example.gptunittestwriterjava.entity.Job;
import com.example.gptunittestwriterjava.entity.JobStatus;
import com.example.gptunittestwriterjava.entity.JobType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends JpaRepository<Job, Long> {
    @EntityGraph(attributePaths = {"user"})
    Page<Job> findAll(Pageable pageable);

    Optional<Job> findByGithubRepoAndBranchAndJobTypeAndStatusIn(String githubRepo, String branch, JobType jobType, List<JobStatus> statuses);

}
