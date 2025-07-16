package com.pipio.repository;

import com.pipio.model.Job;
import com.pipio.model.Pipeline;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByPipeline(Pipeline pipeline);
}

