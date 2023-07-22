package com.example.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "openai")
public class OpenAIProperties {
    private String apiKey;
    private String apiBase;
    private String apiVersion;
    private String deploymentName;
    private String contextLength;
    private double projectBudget;
    private Map<String, Map<String, Map<String, Double>>> pricing;
}
