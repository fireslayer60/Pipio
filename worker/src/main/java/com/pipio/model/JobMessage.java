package com.pipio.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobMessage {
    private Long id;
    private String pipelineName;
    private List<StepMessage> steps;
    private String baseImage;
}
