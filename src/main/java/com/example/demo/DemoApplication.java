package com.example.demo;

import com.example.demo.worker.ProjectUtWriter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ProjectUtWriter writer) {
        return args -> {
            String projectPath = "/Users/lancer/Development/ws/survey-server";
            int limit = 1;
            writer.setProjectPath(projectPath);
            writer.generateUnitTest();
        };
    }
}
