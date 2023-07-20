package com.example.demo.service;

import com.example.demo.config.OpenAIProperties;
import com.example.demo.model.OpenAIResult;
import com.example.demo.model.openai.OpenAIApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAIApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    @Autowired
    private OpenAIProperties properties;
    @Autowired
    private ResourceLoader resourceLoader;

    public OpenAIResult generateUnitTest(String prompt) throws IOException {
        if ("dummy".equals(properties.getApiBase())) {
            // Load the response from the local resource file
            Resource resource = new ClassPathResource("dummy/utResp.json");
            ObjectMapper objectMapper = new ObjectMapper();
            OpenAIApiResponse response = objectMapper.readValue(resource.getInputStream(), OpenAIApiResponse.class);

            String content = response.getData().getChoices().get(0).getMessage().getContent();

            // Here you should also provide dummy values for tokens if you want to calculate cost
            return new OpenAIResult(content, 0);
        } else {
            String url = properties.getApiBase() + "/openai/deployments/" + properties.getDeploymentName() + "/completions?api-version=" + properties.getApiVersion();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", properties.getApiKey());

            Map<String, Object> body = new HashMap<>();
            body.put("prompt", prompt);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            OpenAIApiResponse response = restTemplate.postForObject(url, entity, OpenAIApiResponse.class);

            // Extract the content
            String content = response.getData().getChoices().get(0).getMessage().getContent();

            // Calculate cost
            double costPerInputToken = properties.getPricing().get("gpt-35-turbo").get(properties.getContextLength()).get("input-1k");
            double costPerOutputToken = properties.getPricing().get("gpt-35-turbo").get(properties.getContextLength()).get("output-1k");
            double inputTokens = response.getData().getUsage().getPrompt_tokens();
            double outputTokens = response.getData().getUsage().getCompletion_tokens();
            double cost = costPerInputToken * inputTokens + costPerOutputToken * outputTokens;

            return new OpenAIResult(content, cost);
        }
    }
}