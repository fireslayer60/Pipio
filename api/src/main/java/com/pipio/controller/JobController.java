package com.pipio.controller;


import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pipio.dto.JobDTO;
import com.pipio.model.JobLog;
import com.pipio.service.JobLogService;
import com.pipio.service.JobService;

import lombok.RequiredArgsConstructor;



@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobLogService logService;
    

    @GetMapping("/{id}")
    public ResponseEntity<JobDTO> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<JobDTO>> getAllJob() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/details/{id}")
    public ResponseEntity<?> getJobDetails(@PathVariable Long id) {
        
        if(jobService.getJobDetails(id)==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(jobService.getJobDetails(id));
    }

    @GetMapping("/logs/{jobId}")
    public ResponseEntity<List<JobLog>> getLogs(@PathVariable Long jobId) {
        if(jobService.getJobDetails(jobId)==null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(logService.getLogsForJob(jobId));
    }

    
    
}
