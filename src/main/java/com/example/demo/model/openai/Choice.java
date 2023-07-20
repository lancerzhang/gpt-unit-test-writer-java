package com.example.demo.model.openai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Choice {
    private double index;
    private String finish_reason;
    private Message message;
}
