package com.example.gptunittestwriterjava.repository;

import com.example.gptunittestwriterjava.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
    @EntityGraph(attributePaths = {"user"})
    Page<Job> findAll(Pageable pageable);
}
