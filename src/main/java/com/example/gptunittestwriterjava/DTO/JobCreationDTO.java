package com.example.gptunittestwriterjava.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobCreationDTO {
    private String githubRepo;
    private String branch;
    private String jobType;
}
