package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class InitialDataLoader implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Override
    public void run(String... args) throws JsonProcessingException {
        // Create a sample user
        String userJsonStr = "{\n" +
                "  \"displayName\": \"sampleUser\",\n" +
                "  \"employeeId\": \"01234567\",\n" +
                "  \"email\": \"sample.user@example.com\"\n" +
                "}";
        User user = new ObjectMapper().readValue(userJsonStr, User.class);
        user = userService.createUser(user);
    }
}
