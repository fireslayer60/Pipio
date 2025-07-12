package com.pipio.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import com.pipio.model.StepStatus;

import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepMessage {
    private Long id;
    private String runCommand;
    private StepStatus status;
}
