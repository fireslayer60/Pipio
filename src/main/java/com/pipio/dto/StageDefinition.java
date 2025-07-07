package com.pipio.dto;

import java.util.List;

import lombok.Data;

@Data
public class StageDefinition {
    private String name;
    private List<StepDefinition> steps;
}
