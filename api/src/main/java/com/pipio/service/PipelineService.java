package com.pipio.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.pipio.dto.*;
import com.pipio.model.Pipeline;


@Service
public class PipelineService {

    public PipelineResponse convertToResponseDto(Pipeline pipeline) {
        PipelineResponse dto = new PipelineResponse();
        dto.setId(pipeline.getId());
        dto.setName(pipeline.getName());

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
}

