package com.example.demo.model.openai;

import com.github.javaparser.utils.StringEscapeUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private String role;
    private String content;
}
