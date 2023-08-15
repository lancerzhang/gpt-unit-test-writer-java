package com.example.gptunittestwriterjava.service;

import com.example.gptunittestwriterjava.DTO.JobCreationDTO;
import com.example.gptunittestwriterjava.entity.Job;
import com.example.gptunittestwriterjava.entity.JobStatus;
import com.example.gptunittestwriterjava.entity.JobType;
import com.example.gptunittestwriterjava.entity.User;
import com.example.gptunittestwriterjava.exception.ExistingJobException;
import com.example.gptunittestwriterjava.exception.InsufficientBudgetException;
import com.example.gptunittestwriterjava.repository.JobRepository;
import com.example.gptunittestwriterjava.repository.UserRepository;
import com.example.gptunittestwriterjava.worker.CoverageWriter;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
@Setter
@Service
public class JobService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ApplicationContext applicationContext;

    public Optional<Job> getJobById(Long id) {
        return jobRepository.findById(id);
    }

    public Job createJob(JobCreationDTO dto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with userId: " + userId));

        if (user.getBudget() <= 0) {
            throw new InsufficientBudgetException("User has insufficient budget.");
        }

        // Check for existing job
        List<JobStatus> statuses = Arrays.asList(JobStatus.NOT_STARTED, JobStatus.IN_PROGRESS);
        JobType jobType = JobType.valueOf(dto.getJobType().toUpperCase());

        Optional<Job> existingJob = jobRepository.findByGithubRepoAndBranchAndJobTypeAndStatusIn(dto.getGithubRepo(), dto.getBranch(), jobType, statuses);

        if (existingJob.isPresent()) {
            throw new ExistingJobException("A job with the given criteria is already in progress or not started.");
        }

        Job job = new Job();
        job.setGithubRepo(dto.getGithubRepo());
        job.setBranch(dto.getBranch());
        job.setJobType(jobType);
        job.setUser(user);
        job.setStatus(JobStatus.NOT_STARTED);

        return jobRepository.save(job);
    }

    public Page<Job> getAllJobs(Pageable pageable) {
        return jobRepository.findAll(pageable);
    }

    public List<Job> findNotStartedJobs() {
        return jobRepository.findByStatusOrderByIdAsc(JobStatus.NOT_STARTED);
    }

    public void runJob(Job job) {
        // Update the job's status to IN_PROGRESS and persist it
        job.setStatus(JobStatus.IN_PROGRESS);
        jobRepository.save(job);

        // Obtain a new instance of CoverageWriter from the application context
        CoverageWriter coverageWriter = applicationContext.getBean(CoverageWriter.class);
        coverageWriter.configure(job.getId(), job.getGithubRepo(), job.getBranch());
        // Logic to run the job
        try {
            // Simulating job execution. Implement your logic here.
            logger.info("Starting the job " + job.getId());

            coverageWriter.generateUnitTest();

            // If the job completes successfully, you might want to update its status to COMPLETED here
            // Or if there's an error, you can update the status to ERROR

            // job.setStatus(JobStatus.COMPLETED);
            // jobRepository.save(job);
        } catch (Exception e) {
            // Log the exception and potentially update the job's status to ERROR, if needed
            logger.error("Error while executing the job " + job.getId(), e);

            // Uncomment below if you wish to set job status to ERROR upon exceptions
            // job.setStatus(JobStatus.ERROR);
            // jobRepository.save(job);
        }
    }
}
