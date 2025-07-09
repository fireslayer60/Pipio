package com.pipio.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipio.dto.JobDTO;
import com.pipio.dto.JobMessage;
import com.pipio.dto.StepMessage;
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

        // Convert to JobMessage DTO
        JobMessage jobMessage = new JobMessage();
        jobMessage.setId(job.getId());
        jobMessage.setPipelineName(pipeline.getName());

        List<StepMessage> steps = pipeline.getStages().stream()
            .flatMap(stage -> stage.getSteps().stream())
            .map(step -> {
                StepMessage dto = new StepMessage();
                dto.setRunCommand(step.getRunCommand());
                return dto;
            }).toList();

        jobMessage.setSteps(steps);

        try {
            ObjectMapper mapper = new ObjectMapper();
            redisTemplate.opsForList().leftPush("jobs", mapper.writeValueAsString(jobMessage));
// worker listens on "jobs"
            //System.out.println("📤 Enqueued job to Redis: {}"+ json);
        } catch (Exception e) {
            System.out.println("❌ Failed to serialize job message"+e);
        }
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
