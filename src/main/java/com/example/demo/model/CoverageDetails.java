package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CoverageDetails {
    private final List<Integer> notCoveredLines;
    private final List<Integer> partlyCoveredLines;

    public CoverageDetails(List<Integer> notCoveredLines, List<Integer> partlyCoveredLines) {
        this.notCoveredLines = notCoveredLines;
        this.partlyCoveredLines = partlyCoveredLines;
    }
}