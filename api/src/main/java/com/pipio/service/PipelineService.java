package com.pipio.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import com.pipio.dto.*;
import com.pipio.model.Pipeline;
import com.pipio.model.Stage;
import com.pipio.model.Step;
import com.pipio.repository.PipelineRepository;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class PipelineService {

    private PipelineRepository pipelineRepo;
    public PipelineService(PipelineRepository pipelineRepo) {
        this.pipelineRepo = pipelineRepo;
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
            for (StepDefinition stepDef : sdef.getSteps()) {
                Step step = new Step();
                step.setRunCommand(stepDef.getRun());
                step.setStage(stage);
                stepList.add(step);
            }

            stage.setSteps(stepList);
            stageList.add(stage);
        }

        pipeline.setStages(stageList);
        pipelineRepo.save(pipeline);
        return pipeline.getId();
    }
}

