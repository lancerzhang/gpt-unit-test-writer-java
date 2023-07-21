package com.example.demo.service;

import com.example.demo.config.OpenAIProperties;
import com.example.demo.model.openai.Data;
import com.example.demo.model.openai.OpenAIApiResponse;
import com.example.demo.model.openai.OpenAIResult;
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
        OpenAIApiResponse response;

        if ("dummy".equals(properties.getApiBase())) {
            // Load the response from the local resource file
            Resource resource = new ClassPathResource("dummy/utResp.json");
            ObjectMapper objectMapper = new ObjectMapper();
            response = objectMapper.readValue(resource.getInputStream(), OpenAIApiResponse.class);
        } else {
            // The rest of your code...
            String url = properties.getApiBase() + "/openai/deployments/" + properties.getDeploymentName() + "/completions?api-version=" + properties.getApiVersion();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", properties.getApiKey());

            Map<String, Object> body = new HashMap<>();
            body.put("prompt", prompt);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            response = restTemplate.postForObject(url, entity, OpenAIApiResponse.class);
        }

        return processResponse(response);
    }

    public OpenAIResult processResponse(OpenAIApiResponse response) {
        Data data = response.getData();
        String content = data.getChoices().get(0).getMessage().getContent();

        // Calculate cost
        Map<String, Double> modelPrice = properties.getPricing().get(properties.getDeploymentName()).get(properties.getContextLength());
        double costPerInputToken = modelPrice.get("input-1k") / 1000;
        double costPerOutputToken = modelPrice.get("output-1k") / 1000;
        double inputTokens = data.getUsage().getPrompt_tokens();
        double outputTokens = data.getUsage().getCompletion_tokens();
        double cost = costPerInputToken * inputTokens + costPerOutputToken * outputTokens;

        return new OpenAIResult(content, cost);
    }


}