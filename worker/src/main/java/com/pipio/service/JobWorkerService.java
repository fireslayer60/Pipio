package com.pipio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipio.model.Job;
import com.pipio.model.JobMessage;
import com.pipio.model.JobStatus;
import com.pipio.model.Secret;
import com.pipio.model.Stage;
import com.pipio.model.Step;
import com.pipio.model.StepMessage;
import com.pipio.model.StepStatus;
import com.pipio.repository.JobRepository;
import com.pipio.repository.SecretRepository;
import com.pipio.repository.StepRepository;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobWorkerService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JobRepository jobRepo;
    private final StepRepository stepRepo;
    private final SecretRepository secretRepository;
    private StringEncryptor encryptor;
    @Autowired
    private DockerRunner dockerRunner;
   
    

    public JobWorkerService(StringRedisTemplate redisTemplate,JobRepository jobRepo, StepRepository stepRepo, SecretRepository secretRepository, StringEncryptor encryptor) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
        this.stepRepo = stepRepo;
        this.jobRepo = jobRepo;
        this.secretRepository = secretRepository;
        this.encryptor = encryptor;
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
                    long pipelineId = dbJob.getPipeline().getId();

                    boolean failed = false;

                    for (StepMessage step : job.getSteps()) {
                        List<Stage> stages = dbJob.getPipeline().getStages();

                        Step stepEntity = stages.stream()
                            .flatMap(stage -> stage.getSteps().stream())
                            .filter(s -> s.getRunCommand().equals(step.getRunCommand()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("No matching step found for: " + step.getRunCommand()));
                        stepEntity.setStatus(StepStatus.RUNNING);
                        stepRepo.save(stepEntity);
                        List<Secret> secrets = secretRepository.findByPipelineId(pipelineId);
                           

                            Map<String, String> envSecrets = secrets.stream()
                                .filter(secret -> "env".equals(secret.getType()))
                                .collect(Collectors.toMap(
                                    Secret::getName,
                                    secret -> String.valueOf(encryptor.decrypt(secret.getValue()))
                                ));



                        int code = dockerRunner.runStep(
                            step.getRunCommand(),
                            String.valueOf(job.getId()),
                            step.getRunCommand(),
                            job.getBaseImage(),
                            envSecrets
                        );

                        if (code != 0) {
                            log.warn("❌ Step failed. Mark job as FAILED.");
                            stepEntity.setStatus(StepStatus.FAILURE);
                            stepRepo.save(stepEntity);
                            dbJob.setStatus(JobStatus.FAILED);
                            jobRepo.save(dbJob);
                            failed = true;
                            break;
                        }
                        stepEntity.setStatus(StepStatus.SUCCESS);
                        stepRepo.save(stepEntity);
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
