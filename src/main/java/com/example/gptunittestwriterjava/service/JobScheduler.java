package com.example.gptunittestwriterjava.service;

import com.example.gptunittestwriterjava.entity.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

@Component
public class JobScheduler {

    @Autowired
    private JobService jobService; // Assuming you have a JobService to interact with jobs

    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;

    @Scheduled(fixedDelay = 15000) // Runs every 15 seconds. Adjust as needed.
    public void pickupAndRunJobs() {
        List<Job> notStartedJobs = jobService.findNotStartedJobs(); // A method that fetches all NOT_STARTED jobs.
        for (Job job : notStartedJobs) {
            taskExecutor.execute(() -> jobService.runJob(job)); // Assuming runJob method will handle the logic of running the job.
        }
    }
}
