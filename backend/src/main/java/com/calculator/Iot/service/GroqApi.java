package com.calculator.Iot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class GroqApi {

    private static final Logger logger = LoggerFactory.getLogger(GroqApi.class);
    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.1-70b-versatile";
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private static final String SYSTEM_PROMPT = """
            You are a smart financial and mathematical calculator assistant.
            When given a problem in natural language, you must:
            1. Analyze the problem and identify all quantities and operations involved.
            2. Solve it step by step, showing every calculation clearly.
            3. Present the final result prominently with context.

            Format your response exactly like this:

            ## Problem Analysis
            [Brief description of what needs to be calculated]

            ## Step-by-Step Solution
            [Each calculation step on its own line, with a brief explanation]

            ## Final Result
            [The final answer with units and a short practical conclusion]

            Be precise with numbers. Always explain what each calculation means in context.
            """;

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public GroqApi() {
        this.restClient = RestClient.builder()
                .defaultUri(GROQ_URL)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .connectTimeout(TIMEOUT)
                .readTimeout(TIMEOUT)
                .build();
    }

    /**
     * Sends a natural-language math question to the Groq API and returns the AI explanation.
     *
     * @param question The user's natural-language problem.
     * @return The AI-generated explanation and result as a Markdown-formatted string.
     */
    @SuppressWarnings("unchecked")
    public String ask(String question) {
        try {
            logger.info("====== GROQ API REQUEST ======");
            logger.info("Question: {}", question);
            logger.info("API Key configured: {}", apiKey != null && !apiKey.isEmpty());

            // Validate API key
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("Groq API key is not configured. Please set groq.api.key in application.properties or .env file");
            }

            Map<String, Object> body = Map.of(
                    "model", MODEL,
                    "messages", List.of(
                            Map.of("role", "system", "content", SYSTEM_PROMPT),
                            Map.of("role", "user",   "content", question)
                    ),
                    "temperature", 0.3
            );

            logger.info("Sending request to Groq API with model: {}", MODEL);

            Map<String, Object> response = restClient.post()
                    .uri(GROQ_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            logger.info("Received response from Groq API");

            if (response == null) {
                throw new IllegalStateException("Empty response from Groq API - response is null");
            }

            // Check for Groq-specific error in response body
            if (response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                String errorMessage = (String) error.get("message");
                String errorCode = (String) error.get("code");
                throw new RuntimeException("Groq API error (" + errorCode + "): " + errorMessage);
            }

            if (!response.containsKey("choices")) {
                throw new IllegalStateException("Invalid response from Groq API - missing 'choices' field. Response: " + response);
            }

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException("Invalid response from Groq API - choices is null or empty");
            }

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) {
                throw new IllegalStateException("Invalid response from Groq API - message is null in first choice");
            }

            String content = (String) message.get("content");
            if (content == null) {
                throw new IllegalStateException("Invalid response from Groq API - content is null in message");
            }

            logger.info("Successfully extracted AI response (length: {})", content.length());
            return content;

        } catch (HttpClientErrorException e) {
            logger.error("Groq API client error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new RuntimeException("Invalid Groq API key. Please check your GROQ_API_KEY in .env file", e);
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new RuntimeException("Groq API rate limit exceeded. Please try again later", e);
            } else if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new RuntimeException("Bad request to Groq API: " + e.getResponseBodyAsString(), e);
            }
            throw new RuntimeException("HTTP error from Groq API: " + e.getStatusCode() + " - " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            logger.error("Groq API server error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Groq API server error: " + e.getStatusCode() + ". The Groq service may be temporarily unavailable", e);
        } catch (RestClientException e) {
            logger.error("Groq API connection error: {}", e.getMessage());
            throw new RuntimeException("Failed to connect to Groq API. Please check your internet connection and try again", e);
        } catch (Exception e) {
            logger.error("Unexpected error in GroqApi.ask(): {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get AI response: " + e.getMessage(), e);
        }
    }
}
