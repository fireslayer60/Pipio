package com.pipio.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipio.model.JobMessage;
import com.pipio.model.StepMessage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobWorkerService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public JobWorkerService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public void startWorker() {
        log.info("🚀 Worker started. Waiting for jobs...");

        while (true) {
            try {
                String json = redisTemplate.opsForList().leftPop("jobs");

                if (json != null) {
                    log.info("📦 Raw JSON from Redis: {}", json);
                    String innerJson = objectMapper.readValue(json, String.class);
    
                    // Now, parse the inner JSON into your JobMessage object
                    JobMessage job = objectMapper.readValue(innerJson, JobMessage.class);
                    log.info("🔧 Picked job: {}", job.getId());
                    for (StepMessage step : job.getSteps()) {
                        int code = DockerRunner.runStep(
                            step.getRunCommand(),
                            String.valueOf(job.getId()),
                            step.getRunCommand()
                        );

                        if (code != 0) {
                            log.warn("❌ Step failed. Mark job as failed.");
                            // TODO: update DB job status here
                            break;
                        }
                    }



                    
                    log.info("✅ Finished job {}", job.getId());
                } else {
                    Thread.sleep(1000); // Poll delay
                }

            } catch (Exception e) {
                log.error("❌ Worker error", e);
            }
        }
    }
}
