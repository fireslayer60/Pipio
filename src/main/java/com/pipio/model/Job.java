package com.pipio.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne
    private Pipeline pipeline;

    @Enumerated(EnumType.STRING)
    private JobStatus status;

    private int attempts;
    private LocalDateTime createdAt;
    private LocalDateTime lastTriedAt;
}
