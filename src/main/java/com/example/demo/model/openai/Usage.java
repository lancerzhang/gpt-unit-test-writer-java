package com.example.demo.model.openai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Usage {
    private double completion_tokens;
    private double prompt_tokens;
    private double total_tokens;
}
