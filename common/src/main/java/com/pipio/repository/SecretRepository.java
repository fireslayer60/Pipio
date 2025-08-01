package com.pipio.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pipio.model.Secret;

@Repository
public interface SecretRepository extends JpaRepository<Secret,Long>{
    List<Secret> findByPipelineId(Long pipelineId);
}
