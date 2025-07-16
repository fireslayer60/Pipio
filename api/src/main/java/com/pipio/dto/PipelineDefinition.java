package com.pipio.dto;

import java.util.List;

import lombok.Data;

@Data
public class PipelineDefinition {
    private String baseImage;
    private List<StageDefinition> stages;
}
