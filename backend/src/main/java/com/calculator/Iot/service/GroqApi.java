package com.calculator.Iot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class GroqApi {

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";

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
        this.restClient = RestClient.create();
    }

    /**
     * Sends a natural-language math question to the Groq API and returns the AI explanation.
     *
     * @param question The user's natural-language problem.
     * @return The AI-generated explanation and result as a Markdown-formatted string.
     */
    @SuppressWarnings("unchecked")
    public String ask(String question) {
        Map<String, Object> body = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", SYSTEM_PROMPT),
                        Map.of("role", "user",   "content", question)
                ),
                "temperature", 0.3
        );

        Map<String, Object> response = restClient.post()
                .uri(GROQ_URL)
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Empty response from Groq API");
        }

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        return (String) message.get("content");
    }
}
