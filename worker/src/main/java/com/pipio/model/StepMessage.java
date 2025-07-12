package com.pipio.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StepMessage {
    private Long id;
    private String runCommand;
    private StepStatus status;
}
