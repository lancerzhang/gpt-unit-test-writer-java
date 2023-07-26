package com.example.demo.service;

import com.example.demo.config.OpenAIProperties;
import com.example.demo.model.Step;
import com.example.demo.model.openai.Message;
import com.example.demo.model.openai.OpenAIApiRequest;
import com.example.demo.model.openai.OpenAIApiResponse;
import com.example.demo.model.openai.OpenAIResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

@Service
public class OpenAIApiService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RestTemplate restTemplate = new RestTemplate();
    private final OpenAIProperties openAIProperties;

    @Autowired
    public OpenAIApiService(OpenAIProperties openAIProperties) {
        this.openAIProperties = openAIProperties;
    }

    public OpenAIResult generateUnitTest(Step step, ArrayList<Message> messages, boolean hasTestFile) throws IOException {
        OpenAIApiResponse response;
        OpenAIResult result;

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
            String url = openAIProperties.getApiBase() + "openai/deployments/" + step.getDeployment()
                    + "/chat/completions?api-version=" + openAIProperties.getApiVersion();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", openAIProperties.getApiKey());
            OpenAIApiRequest openAIApiRequest = new OpenAIApiRequest();
            openAIApiRequest.setMessages(messages);
            HttpEntity<OpenAIApiRequest> entity = new HttpEntity<>(openAIApiRequest, headers);

            ObjectMapper objectMapper = new ObjectMapper();
            logger.info("Sending POST request to: " + url);
            logger.debug("Headers: " + headers);
            logger.debug("Request body: " + objectMapper.writeValueAsString(openAIApiRequest));
            response = restTemplate.postForObject(url, entity, OpenAIApiResponse.class);
            logger.debug("Received response: " + objectMapper.writeValueAsString(response));

        }

        messages.add(response.getChoices().get(0).getMessage());
        result = processResponse(step, response);

        return result;
    }

    protected OpenAIResult processResponse(Step step, OpenAIApiResponse response) {
        String content = response.getChoices().get(0).getMessage().getContent();

        // Calculate cost
        Map<String, Double> modelPrice = openAIProperties.getPricing().get(step.getModel()).get(step.getContextLength());
        double costPerInputToken = modelPrice.get("input-1k") / 1000;
        double costPerOutputToken = modelPrice.get("output-1k") / 1000;
        double inputTokens = response.getUsage().getPrompt_tokens();
        double outputTokens = response.getUsage().getCompletion_tokens();
        double cost = costPerInputToken * inputTokens + costPerOutputToken * outputTokens;

        return new OpenAIResult(content, cost);
    }

}