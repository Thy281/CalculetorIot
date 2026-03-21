package com.calculator.Iot.dto;

public record CalculationRequest(
        String value1,
        int base1,
        String value2,
        int base2,
        String operator
) {
}

