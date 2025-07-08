package com.pipio.service;

import java.time.LocalDateTime;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.pipio.dto.JobDTO;
import com.pipio.model.Job;
import com.pipio.model.JobStatus;
import com.pipio.model.Pipeline;
import com.pipio.repository.JobRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JobService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final JobRepository jobRepo;

    public void enqueueJob(Pipeline pipeline) {
        Job job = Job.builder()
            .pipeline(pipeline)
            .status(JobStatus.PENDING)
            .attempts(0)
            .createdAt(LocalDateTime.now())
            .build();

        jobRepo.save(job); // persist to DB

        redisTemplate.opsForList().leftPush("job:queue", job.getId());
    }

    public JobDTO getJobById(Long id) {
        Job job = jobRepo.findById(id)
                         .orElseThrow(() -> new RuntimeException("Job not found"));

        JobDTO dto = new JobDTO();
        dto.setId(job.getId());
        dto.setStatus(job.getStatus().name());
        dto.setAttempts(job.getAttempts());
        dto.setPipelineId(job.getPipeline().getId());
        dto.setPipelineName(job.getPipeline().getName());
        return dto;
    }
}
