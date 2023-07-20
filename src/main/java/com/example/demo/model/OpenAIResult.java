package com.example.demo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenAIResult {
    private String content;
    private double cost;

    public OpenAIResult(String content, double cost) {
        this.content = content;
        this.cost = cost;
    }
}
