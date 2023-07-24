package com.example.demo.service;

import com.example.demo.config.OpenAIProperties;
import com.example.demo.exception.FailedGeneratedTestException;
import com.example.demo.model.Step;
import com.example.demo.model.openai.Data;
import com.example.demo.model.openai.OpenAIApiResponse;
import com.example.demo.model.openai.OpenAIResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class OpenAIApiService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final OpenAIProperties openAIProperties;

    @Autowired
    public OpenAIApiService(OpenAIProperties openAIProperties) {
        this.openAIProperties = openAIProperties;
    }

    protected static List<String> extractMarkdownCodeBlocks(String input) {
        List<String> codeBlocks = new ArrayList<>();
        Pattern pattern = Pattern.compile("```java\\s*(.*?)\\s*```", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            codeBlocks.add(matcher.group(1));
        }

        return codeBlocks;
    }

    public OpenAIResult generateUnitTest(Step step, String prompt, boolean hasTestFile) {
        OpenAIApiResponse response;
        OpenAIResult result;

        try {
            if ("dummy".equals(openAIProperties.getApiBase())) {
                // Load the response from the local resource file
                Resource resource;
                if (hasTestFile) {
                    resource = new ClassPathResource("dummy/coverage_exists_1.json");
                } else {
                    resource = new ClassPathResource("dummy/coverage_new_1.json");
                }
                ObjectMapper objectMapper = new ObjectMapper();
                response = objectMapper.readValue(resource.getInputStream(), OpenAIApiResponse.class);
            } else {
                String url = openAIProperties.getApiBase() + "/openai/deployments/" + step.getModel() + "/completions?api-version=" + openAIProperties.getApiVersion();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("api-key", openAIProperties.getApiKey());
                Map<String, Object> body = new HashMap<>();
                body.put("prompt", prompt);
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                response = restTemplate.postForObject(url, entity, OpenAIApiResponse.class);
            }

            result = processResponse(step, response);
        } catch (Exception e) {
            throw new FailedGeneratedTestException("Failed to generate unit test from openAI.");
        }

        return result;
    }

    protected OpenAIResult processResponse(Step step, OpenAIApiResponse response) {
        Data data = response.getData();
        String content = data.getChoices().get(0).getMessage().getContent();

        // Calculate cost
        Map<String, Double> modelPrice = openAIProperties.getPricing().get(step.getModel()).get(step.getContextLength());
        double costPerInputToken = modelPrice.get("input-1k") / 1000;
        double costPerOutputToken = modelPrice.get("output-1k") / 1000;
        double inputTokens = data.getUsage().getPrompt_tokens();
        double outputTokens = data.getUsage().getCompletion_tokens();
        double cost = costPerInputToken * inputTokens + costPerOutputToken * outputTokens;

        List<String> codeBlocks = extractMarkdownCodeBlocks(content);

        if (codeBlocks.size() != 1) {
            throw new FailedGeneratedTestException("Expect one code block in openAI response but it's not.");
        }

        return new OpenAIResult(codeBlocks.get(0), cost);
    }

}