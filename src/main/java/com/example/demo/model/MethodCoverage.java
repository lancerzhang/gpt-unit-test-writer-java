package com.example.demo.model;

import java.util.List;

public class MethodCoverage {
    private String methodName;
    private List<Integer> notCoveredLines;
    private List<Integer> partlyCoveredLines;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public List<Integer> getNotCoveredLines() {
        return notCoveredLines;
    }

    public void setNotCoveredLines(List<Integer> notCoveredLines) {
        this.notCoveredLines = notCoveredLines;
    }

    public List<Integer> getPartlyCoveredLines() {
        return partlyCoveredLines;
    }

    public void setPartlyCoveredLines(List<Integer> partlyCoveredLines) {
        this.partlyCoveredLines = partlyCoveredLines;
    }
}
