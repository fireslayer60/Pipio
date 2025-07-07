package com.pipio.dto;

import java.util.List;

import lombok.Data;

@Data
public class PipelineDefinition {
    private List<StageDefinition> stages;
}
