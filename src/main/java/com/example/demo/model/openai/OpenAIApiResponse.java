package com.example.demo.model.openai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenAIApiResponse {
    private String msg;
    private String code;
    private Data data;
    private long timestamp;
    private String requestId;
}

