package com.example.demo.model.openai;

import com.github.javaparser.utils.StringEscapeUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Message {
    private String role;
    private String content;

    public void setRole(String role) {
        this.role = role;
    }

    public void setContent(String content) {
        // Replace newlines with \n escape sequence and escape double quotes
        this.content = StringEscapeUtils.escapeJava(content);
    }
}
