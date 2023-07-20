package com.example.demo.service;

import com.example.demo.config.OpenAIProperties;
import com.example.demo.model.OpenAIResult;
import com.example.demo.model.openai.response.OpenAIApiResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class OpenAIApiService {

    private final OpenAIProperties properties;
    private final RestTemplate restTemplate;

    public OpenAIApiService(OpenAIProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder.build();
    }

    public OpenAIResult generateUnitTest(String prompt) {
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
        double costPerInputToken = properties.getPricing().get("gpt-35-turbo").get("context-4k").get("input-1k");
        double costPerOutputToken = properties.getPricing().get("gpt-35-turbo").get("context-4k").get("output-1k");
        double inputTokens = response.getData().getUsage().getPrompt_tokens();
        double outputTokens = response.getData().getUsage().getCompletion_tokens();
        double cost = costPerInputToken * inputTokens + costPerOutputToken * outputTokens;

        return new OpenAIResult(content, cost);
    }


}
