package com.pipio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SecretDto {
    @NotBlank
    private String name;

    @NotBlank
    private String value;

    @NotBlank
    private String type;
}
