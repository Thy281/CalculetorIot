package com.calculator.Iot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class NumberBaseConverterService {

    private static final int MAX_LENGTH = 100;
    private static final String VALID_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public String convertDirect(String value, int fromBase, int toBase) {
        // Validação de parâmetros
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }

        String normalizedValue = value.trim();
        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("Value cannot be empty");
        }

        if (fromBase < 2 || fromBase > 36) {
            throw new IllegalArgumentException("From base must be between 2 and 36");
        }

        if (toBase < 2 || toBase > 36) {
            throw new IllegalArgumentException("To base must be between 2 and 36");
        }

        if (normalizedValue.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Value is too long (max " + MAX_LENGTH + " characters)");
        }

        // Remover prefixo 0x para hexadecimal se presente
        if ((normalizedValue.startsWith("0x") || normalizedValue.startsWith("0X")) && fromBase == 16) {
            normalizedValue = normalizedValue.substring(2);
            if (normalizedValue.isEmpty()) {
                throw new IllegalArgumentException("Value after 0x prefix is empty");
            }
        }

        // Validar caracteres
        validateCharacters(normalizedValue, fromBase);

        // Converter usando BigInteger (suporta números grandes)
        try {
            BigInteger bigInt = new BigInteger(normalizedValue, fromBase);
            String result = bigInt.toString(toBase).toUpperCase();
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid value for base " + fromBase + ": " + value, e);
        }
    }

    private void validateCharacters(String value, int base) {
        Set<Character> allowedChars = getAllowedCharacters(base);
        for (char c : value.toCharArray()) {
            char upper = Character.toUpperCase(c);
            if (!allowedChars.contains(upper)) {
                throw new IllegalArgumentException(
                    String.format("Character '%c' is not valid for base %d", c, base)
                );
            }
        }
    }

    private Set<Character> getAllowedCharacters(int base) {
        Set<Character> chars = new HashSet<>();
        for (int i = 0; i < base; i++) {
            chars.add(VALID_CHARS.charAt(i));
        }
        return chars;
    }
}
