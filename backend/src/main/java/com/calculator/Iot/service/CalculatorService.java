package com.calculator.Iot.service;

import com.calculator.Iot.dto.CalculationRequest;
import com.calculator.Iot.model.Calculation;
import com.calculator.Iot.repository.CalculationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalculatorService {

    private final CalculationRepository calculationRepository;

    public Calculation calculate(CalculationRequest request) {
        long decimalValue1 = convertToDecimal(request.value1(), request.base1());
        long decimalValue2 = convertToDecimal(request.value2(), request.base2());
        long result = performOperation(decimalValue1, decimalValue2, request.operator());

        Calculation calculation = Calculation.builder()
                .value1(request.value1())
                .base1(request.base1())
                .value2(request.value2())
                .base2(request.base2())
                .operator(request.operator())
                .result(result)
                .timestamp(LocalDateTime.now())
                .build();

        return calculationRepository.save(calculation);
    }

    public List<Calculation> getHistory() {
        return calculationRepository.findAll();
    }

    public void clearHistory() {
        calculationRepository.deleteAll();
    }

    private long convertToDecimal(String value, int base) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        String s = value.trim();
        if (s.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be empty");
        }

        if (base < 2 || base > 36) {
            throw new IllegalArgumentException("Base must be between 2 and 36");
        }

        final int MAX_LENGTH = 100;
        if (s.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Value is too long (max " + MAX_LENGTH + " characters)");
        }

        if ((s.startsWith("0x") || s.startsWith("0X")) && base == 16) {
            s = s.substring(2);
            if (s.isEmpty()) {
                throw new IllegalArgumentException("Value after 0x prefix is empty");
            }
        }

        try {
            return Long.parseLong(s, base);
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    private long performOperation(long value1, long value2, String operator) {
        return switch (operator) {
            case "+" -> value1 + value2;
            case "-" -> value1 - value2;
            case "*" -> value1 * value2;
            case "/" -> {
                if (value2 == 0) {
                    throw new ArithmeticException("Division by zero is not allowed");
                }
                yield value1 / value2;
            }
            default -> throw new IllegalArgumentException("Invalid operator: " + operator);
        };
    }
}
