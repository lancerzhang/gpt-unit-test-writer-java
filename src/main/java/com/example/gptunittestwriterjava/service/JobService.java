package com.example.gptunittestwriterjava.service;

import com.example.gptunittestwriterjava.DTO.JobCreationDTO;
import com.example.gptunittestwriterjava.entity.Job;
import com.example.gptunittestwriterjava.entity.JobStatus;
import com.example.gptunittestwriterjava.entity.User;
import com.example.gptunittestwriterjava.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JobService {

    @Autowired
    private JobRepository jobRepository;

    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public Job createJob(JobCreationDTO dto, Long userId) {
        Job job = new Job();
        job.setProjectId(dto.getProjectId());
        job.setJobType(dto.getJobType());
        User user = new User();
        user.setId(userId);
        job.setUser(user);
        job.setStatus(JobStatus.NOT_STARTED);

        return jobRepository.save(job);
    }

    public Page<Job> getAllJobs(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }

}
