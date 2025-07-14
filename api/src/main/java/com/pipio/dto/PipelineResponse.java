package com.pipio.dto;

import java.util.List;
import lombok.Data;

@Data
public class PipelineResponse {
    private Long id;
    private String name;
    private String repoUrl;
    private List<StageResponse> stages;
}
