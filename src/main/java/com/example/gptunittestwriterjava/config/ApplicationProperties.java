package com.example.gptunittestwriterjava.config;

import com.example.gptunittestwriterjava.model.Step;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "")
public class ApplicationProperties {

    private Map<String, List<Step>> steps;

}
