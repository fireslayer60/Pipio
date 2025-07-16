package com.pipio.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pipio.dto.JobDTO;
import com.pipio.dto.JobMessage;
import com.pipio.dto.StepMessage;
import com.pipio.model.Job;
import com.pipio.model.JobStatus;
import com.pipio.model.Pipeline;
import com.pipio.model.Stage;
import com.pipio.model.Step;
import com.pipio.model.StepStatus;
import com.pipio.repository.JobRepository;
import com.pipio.repository.StepRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class JobService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final JobRepository jobRepo;
    private final StepRepository stepRepo;

    public void enqueueJob(Pipeline pipeline) {
        Job job = Job.builder()
            .pipeline(pipeline)
            .status(JobStatus.PENDING)
            .attempts(0)
            .createdAt(LocalDateTime.now())
            .build();

        jobRepo.save(job); // Persist job

        List<StepMessage> stepMessages = new ArrayList<>();

        for (Stage stage : pipeline.getStages()) {
            for (Step step : stage.getSteps()) {
                // Set initial step status
                step.setStatus(StepStatus.PENDING);
                // Save to generate ID
                stepRepo.save(step);

                StepMessage sm = new StepMessage();
                sm.setId(step.getId()); // Needed by worker
                sm.setRunCommand(step.getRunCommand());

                stepMessages.add(sm);
            }
        }

        JobMessage jobMessage = new JobMessage();
        jobMessage.setId(job.getId());
        jobMessage.setPipelineName(pipeline.getName());
        jobMessage.setSteps(stepMessages);
        jobMessage.setBaseImage(pipeline.getBaseImage());

        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(jobMessage);
            redisTemplate.opsForList().leftPush("jobs", json);
            log.info("📤 Enqueued job {} to Redis", job.getId());
        } catch (Exception e) {
            log.error("❌ Failed to serialize job message", e);
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

    public List<JobDTO> getAllJobs(){
        List<Job> allJobs = jobRepo.findAll();
        List<JobDTO> allJobDTO = new ArrayList<>();

        for(Job job : allJobs){
            JobDTO dto = new JobDTO();
            dto.setId(job.getId());
            dto.setStatus(job.getStatus().name());
            dto.setAttempts(job.getAttempts());
            dto.setPipelineId(job.getPipeline().getId());
            dto.setPipelineName(job.getPipeline().getName());
            allJobDTO.add(dto);
        }
        return allJobDTO;
    }
    public Job getJobDetails(Long id){
        Optional<Job> jobOpt = jobRepo.findById(id);
        if (jobOpt.isEmpty()) {
            return null;
        }

        Job job = jobOpt.get();

        // Manually include stages + steps if they’re lazily fetched
        //job.getPipeline().getStages().forEach(stage -> stage.getSteps().size());

        return job;
    }
}
