package com.pipio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipio.model.Job;
import com.pipio.model.JobMessage;
import com.pipio.model.JobStatus;
import com.pipio.model.StepMessage;
import com.pipio.repository.JobRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobWorkerService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JobRepository jobRepo;

    public JobWorkerService(StringRedisTemplate redisTemplate,JobRepository jobRepo) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.jobRepo = jobRepo;
    }

    public void startWorker() {
        log.info("🚀 Worker started. Waiting for jobs...");

        while (true) {
            try {
                String json = redisTemplate.opsForList().leftPop("jobs");

                if (json != null) {
                    log.info("📦 Raw JSON from Redis: {}", json);
                    String innerJson = objectMapper.readValue(json, String.class);

                    JobMessage job = objectMapper.readValue(innerJson, JobMessage.class);
                    log.info("🔧 Picked job: {}", job.getId());

                    Optional<Job> optionalJob = jobRepo.findById(job.getId());
                    if (optionalJob.isEmpty()) {
                        log.warn("⚠️ No job found in DB with ID: {}", job.getId());
                        continue;
                    }

                    Job dbJob = optionalJob.get();
                    dbJob.setStatus(JobStatus.RUNNING);
                    jobRepo.save(dbJob);

                    boolean failed = false;

                    for (StepMessage step : job.getSteps()) {
                        int code = DockerRunner.runStep(
                            step.getRunCommand(),
                            String.valueOf(job.getId()),
                            step.getRunCommand()
                        );

                        if (code != 0) {
                            log.warn("❌ Step failed. Mark job as FAILED.");
                            dbJob.setStatus(JobStatus.FAILED);
                            jobRepo.save(dbJob);
                            failed = true;
                            break;
                        }
                    }

                    if (!failed) {
                        dbJob.setStatus(JobStatus.SUCCESS);
                        jobRepo.save(dbJob);
                    }

                    log.info("✅ Finished job {}", job.getId());
                    log.info("📦 Final job status: {}", jobRepo.findById(job.getId()).get().getStatus());

                } else {
                    Thread.sleep(1000); // Poll delay
                }

            } catch (Exception e) {
                log.error("❌ Worker error", e);
            }
        }
    }

}
