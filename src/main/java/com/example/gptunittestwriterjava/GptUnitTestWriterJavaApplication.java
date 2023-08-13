package com.example.gptunittestwriterjava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GptUnitTestWriterJavaApplication {

    public static void main(String[] args) {
        // Logic to determine app data location
        String appDataLocation = System.getenv("APP_DATA");
        if (appDataLocation == null || appDataLocation.trim().isEmpty()) {
            appDataLocation = System.getProperty("user.home");
        }

        // Set the determined location in system properties so that it can be used in application.properties
        System.setProperty("app.location", appDataLocation);

        SpringApplication.run(GptUnitTestWriterJavaApplication.class, args);
    }
}
