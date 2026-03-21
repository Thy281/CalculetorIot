package com.calculator.Iot.dto;

public record ConverterResult(
    String originalValue,
    int fromBase,
    String convertedValue,
    int toBase
) {
}
