package com.pipio.dto;

import java.util.List;
import lombok.Data;

@Data
public class PipelineResponse {
    private Long id;
    private String name;
    private List<StageResponse> stages;
}
