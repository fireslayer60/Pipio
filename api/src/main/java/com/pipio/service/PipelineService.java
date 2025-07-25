package com.pipio.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jasypt.encryption.StringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.pipio.dto.*;
import com.pipio.model.Pipeline;
import com.pipio.model.Secret;
import com.pipio.model.Stage;
import com.pipio.model.Step;
import com.pipio.repository.PipelineRepository;
import com.pipio.repository.SecretRepository;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class PipelineService {
    
    private final StringEncryptor encryptor;
    private final PipelineRepository pipelineRepo;
    private final SecretRepository secretRepository;

    public PipelineService(PipelineRepository pipelineRepo, StringEncryptor encryptor, SecretRepository secretRepository) {
        this.pipelineRepo = pipelineRepo;
        this.encryptor = encryptor;
        this.secretRepository = secretRepository;
    }

    public PipelineResponse convertToResponseDto(Pipeline pipeline) {
        PipelineResponse dto = new PipelineResponse();
        dto.setId(pipeline.getId());
        dto.setName(pipeline.getName());
        dto.setRepoUrl(pipeline.getRepoUrl());

        List<StageResponse> stageDtos = pipeline.getStages().stream().map(stage -> {
            StageResponse stageDto = new StageResponse();
            stageDto.setName(stage.getName());

            List<StepResponse> stepDtos = stage.getSteps().stream().map(step -> {
                StepResponse stepDto = new StepResponse();
                stepDto.setRunCommand(step.getRunCommand());
                return stepDto;
            }).toList();

            stageDto.setSteps(stepDtos);
            return stageDto;
        }).toList();

        dto.setStages(stageDtos);
        return dto;
    }

    public Long createPipelineFromYaml(MultipartFile file, String name, String repoUrl) throws Exception {
        LoaderOptions options = new LoaderOptions();
        Constructor constructor = new Constructor(PipelineDefinition.class, options);
        Yaml yaml = new Yaml(constructor);
        PipelineDefinition def = yaml.load(file.getInputStream());

        Pipeline pipeline = new Pipeline();
        pipeline.setName(name);
        if (repoUrl != null && !repoUrl.isBlank()) {
            pipeline.setRepoUrl(repoUrl);
        }
        String baseImage = def.getBaseImage();
        if (baseImage == null || baseImage.isBlank()) {
            baseImage = "alpine"; 
        }
        pipeline.setBaseImage(baseImage);
       
        System.out.println(baseImage +" sss");

        List<Stage> stageList = new ArrayList<>();
        for (StageDefinition sdef : def.getStages()) {
            Stage stage = new Stage();
            stage.setName(sdef.getName());
            stage.setPipeline(pipeline);

            List<Step> stepList = new ArrayList<>();
            int stepIndex = 0;
            for (StepDefinition stepDef : sdef.getSteps()) {
                Step step = new Step();
                step.setRunCommand(stepDef.getRun());
                step.setStage(stage);
                step.setStepOrder(stepIndex++);
                stepList.add(step);
            }

            stage.setSteps(stepList);
            stageList.add(stage);
        }

        pipeline.setStages(stageList);
        pipelineRepo.save(pipeline);
        return pipeline.getId();
    }
    public void saveSecret(SecretDto dto, Pipeline pipeline) {
        Secret secret = new Secret();
        secret.setName(dto.getName());
        secret.setValue(encryptor.encrypt(dto.getValue()));
        secret.setType(dto.getType());
        secret.setPipeline(pipeline);
        secretRepository.save(secret);
    }
    public void saveFileSecret(MultipartFile file, Pipeline pipeline) {
        try {
            String baseDir = System.getProperty("user.home") + "/pipio/secrets/" + pipeline.getId();

            Files.createDirectories(Paths.get(baseDir));

            Path destination = Paths.get(baseDir, file.getOriginalFilename());
            System.out.println("Saving file to: " + destination.toAbsolutePath());
            file.transferTo(destination.toFile()); // Save the file physically
            System.out.println("File saved!");

            Secret secret = new Secret();
            secret.setName(file.getOriginalFilename());
            secret.setType("file");
            secret.setPipeline(pipeline);
            secret.setFilePath(destination.toAbsolutePath().toString()); // This will be used for docker mount
            secret.setValue(null); // No need to store content in DB

            secretRepository.save(secret);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save secret file", e);
        }
    }

}

