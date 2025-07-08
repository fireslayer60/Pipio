package com.pipio.dto;

import lombok.Data;

@Data
public class JobDTO {
    private Long id;
    private String status;
    private int attempts;
    private Long pipelineId;
    private String pipelineName;
}
