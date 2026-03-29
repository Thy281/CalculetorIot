package com.calculator.Iot.controller;

import com.calculator.Iot.dto.AiCalculationRequest;
import com.calculator.Iot.dto.AiCalculationResponse;
import com.calculator.Iot.dto.CalculationRequest;
import com.calculator.Iot.model.Calculation;
import com.calculator.Iot.service.CalculatorService;
import com.calculator.Iot.service.GroqApi;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calculations")
@RequiredArgsConstructor
public class CalculationController {

    private final CalculatorService calculatorService;
    private final GroqApi groqApi;

    @PostMapping
    public ResponseEntity<Calculation> calculate(@RequestBody CalculationRequest request,
                                                 HttpServletRequest httpRequest) {
        System.out.println("====== INCOMING REQUEST ======");
        System.out.println("Origin IP : " + httpRequest.getRemoteAddr());
        System.out.println("Payload   : " + request);
        System.out.println("==============================");

        Calculation result = calculatorService.calculate(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history")
    public ResponseEntity<List<Calculation>> getHistory() {
        List<Calculation> history = calculatorService.getHistory();
        return ResponseEntity.ok(history);
    }

    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory() {
        calculatorService.clearHistory();
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/calculations/ai
     * Sends a natural-language math problem to the Groq AI and returns a detailed explanation.
     */
    @PostMapping("/ai")
    public ResponseEntity<AiCalculationResponse> aiCalculate(@RequestBody AiCalculationRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            return ResponseEntity.badRequest().body(new AiCalculationResponse(null, "Error: 'question' field is required and cannot be empty"));
        }
        String explanation = groqApi.ask(request.question());
        return ResponseEntity.ok(new AiCalculationResponse(request.question(), explanation));
    }
}



