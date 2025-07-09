package com.pipio.dto;

import java.util.List;

import lombok.Data;

@Data
public
class StageResponse {
    private String name;
    private List<StepResponse> steps;
}
