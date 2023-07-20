package com.example.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "openai")
public class OpenAIProperties {
    private String apiKey;
    private String apiBase;
    private String apiVersion;
    private String deploymentName;
    private Map<String, Map<String, Map<String, Double>>> pricing;
}
