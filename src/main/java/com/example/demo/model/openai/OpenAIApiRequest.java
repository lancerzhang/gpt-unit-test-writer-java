package com.example.demo.model.openai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenAIApiRequest {
    private Message[] messages;
}
