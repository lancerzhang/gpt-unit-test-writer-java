package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MethodCoverage {
    private String methodName;
    private List<Integer> notCoveredLines;
    private List<Integer> partlyCoveredLines;
}
