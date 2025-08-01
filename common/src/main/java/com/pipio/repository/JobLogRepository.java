package com.pipio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pipio.model.JobLog;

public interface JobLogRepository extends JpaRepository<JobLog, Long> {
    List<JobLog> findByJobIdOrderByTimestampAsc(Long jobId);
}
