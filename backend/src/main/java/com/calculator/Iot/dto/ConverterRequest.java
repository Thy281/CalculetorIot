package com.calculator.Iot.dto;

public record ConverterRequest(
    String value,
    int fromBase,
    int toBase
) {
    public ConverterRequest {
        if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        }
        if (fromBase < 2 || fromBase > 36) {
            throw new IllegalArgumentException("fromBase must be between 2 and 36");
        }
        if (toBase < 2 || toBase > 36) {
            throw new IllegalArgumentException("toBase must be between 2 and 36");
        }
    }
}
