package com.pipio.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pipio.model.JobLog;
import com.pipio.repository.JobLogRepository;

@Service
public class JobLogService {

    @Autowired
    private JobLogRepository repository;

    public void saveLog(Long jobId, String message) {
        JobLog log = new JobLog();
        log.setJobId(jobId);
        log.setLogMessage(message);
        log.setTimestamp(LocalDateTime.now());
        repository.save(log);
    }

    public List<JobLog> getLogsForJob(Long jobId) {
        return repository.findByJobIdOrderByTimestampAsc(jobId);
    }
}