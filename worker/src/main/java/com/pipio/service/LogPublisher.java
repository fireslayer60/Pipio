package com.pipio.service;
import org.springframework.stereotype.Service;

import com.pipio.model.JobLog;
import com.pipio.repository.JobLogRepository;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;


@Service
public class LogPublisher {

    private  final String LOG_CHANNEL_PREFIX = "job-logs:";

 
    @Autowired
    private JobLogRepository repository;

   
    public void saveLog(Long jobId, String message) {
        JobLog log = new JobLog();
        log.setJobId(jobId);
        log.setLogMessage(message);
        log.setTimestamp(LocalDateTime.now());
        repository.save(log);
    }
}