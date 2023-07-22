package com.example.demo.config;

import com.example.demo.model.Step;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "")
public class ApplicationProperties {

    private List<Step> steps;

}
