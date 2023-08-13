package com.example.gptunittestwriterjava.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class Step {
    private String deployment;
    private String name;
    private String model;
    @JsonProperty("context-length")
    private String contextLength;
    private String feedback;
}
