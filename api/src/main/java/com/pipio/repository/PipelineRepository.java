package com.pipio.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pipio.model.Pipeline;

public interface PipelineRepository extends JpaRepository<Pipeline, Long> {
    Optional<Pipeline> findByRepoUrl(String repoUrl);
}
