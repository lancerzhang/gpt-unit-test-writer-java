package com.example.demo;

import com.example.demo.worker.CoverageWriter;
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
    public CommandLineRunner commandLineRunner(CoverageWriter writer) {
        return args -> {
            String projectPath = "/Users/lancer/Development/ws/survey-server";
            writer.setProjectPath(projectPath);
            writer.generateUnitTest();
        };
    }
}
