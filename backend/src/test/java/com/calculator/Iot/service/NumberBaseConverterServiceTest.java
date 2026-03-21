package com.calculator.Iot.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("NumberBaseConverterService Unit Tests")
class NumberBaseConverterServiceTest {

    private NumberBaseConverterService converterService;

    @BeforeEach
    void setUp() {
        converterService = new NumberBaseConverterService();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VALID CONVERSION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Decimal 255 → Hexadecimal FF")
    void convert_decimal255ToHex_shouldReturnFF() {
        String result = converterService.convertDirect("255", 10, 16);
        assertThat(result).isEqualTo("FF");
    }

    @Test
    @DisplayName("Hexadecimal FF → Decimal 255")
    void convert_hexFFToDecimal_shouldReturn255() {
        String result = converterService.convertDirect("FF", 16, 10);
        assertThat(result).isEqualTo("255");
    }

    @Test
    @DisplayName("Hexadecimal FF → Binary 11111111")
    void convert_hexFFToBinary_shouldReturn11111111() {
        String result = converterService.convertDirect("FF", 16, 2);
        assertThat(result).isEqualTo("11111111");
    }

    @Test
    @DisplayName("Binary 11111111 → Hexadecimal FF")
    void convert_binary11111111ToHex_shouldReturnFF() {
        String result = converterService.convertDirect("11111111", 2, 16);
        assertThat(result).isEqualTo("FF");
    }

    @Test
    @DisplayName("Octal 777 → Decimal 511")
    void convert_octal777ToDecimal_shouldReturn511() {
        String result = converterService.convertDirect("777", 8, 10);
        assertThat(result).isEqualTo("511");
    }

    @Test
    @DisplayName("Binary 1010 → Octal 12")
    void convert_binary1010ToOctal_shouldReturn12() {
        String result = converterService.convertDirect("1010", 2, 8);
        assertThat(result).isEqualTo("12");
    }

    @Test
    @DisplayName("Octal 12 → Binary 1010")
    void convert_octal12ToBinary_shouldReturn1010() {
        String result = converterService.convertDirect("12", 8, 2);
        assertThat(result).isEqualTo("1010");
    }

    @Test
    @DisplayName("Decimal 0 → Any base → 0")
    void convert_zeroToAnyBase_shouldReturnZero() {
        assertThat(converterService.convertDirect("0", 10, 2)).isEqualTo("0");
        assertThat(converterService.convertDirect("0", 10, 8)).isEqualTo("0");
        assertThat(converterService.convertDirect("0", 10, 16)).isEqualTo("0");
        assertThat(converterService.convertDirect("0", 2, 10)).isEqualTo("0");
    }

    @Test
    @DisplayName("Case insensitive hexadecimal input")
    void convert_lowercaseHex_shouldWork() {
        String resultUpper = converterService.convertDirect("FF", 16, 10);
        String resultLower = converterService.convertDirect("ff", 16, 10);
        assertThat(resultUpper).isEqualTo(resultLower);
        assertThat(resultLower).isEqualTo("255");
    }

    @Test
    @DisplayName("Hex with 0x prefix (base 16) should strip prefix")
    void convert_hexWith0xPrefix_shouldStripAndConvert() {
        String result = converterService.convertDirect("0xFF", 16, 10);
        assertThat(result).isEqualTo("255");
    }

    @Test
    @DisplayName("Base 36 conversion (Z = 35)")
    void convert_base36Z_shouldReturn35() {
        String result = converterService.convertDirect("Z", 36, 10);
        assertThat(result).isEqualTo("35");
    }

    @Test
    @DisplayName("Decimal to base 36")
    void convert_decimal35ToBase36_shouldReturnZ() {
        String result = converterService.convertDirect("35", 10, 36);
        assertThat(result).isEqualTo("Z");
    }

    @Test
    @DisplayName("Base 2 to base 36")
    void convert_binaryToBase36_shouldConvertCorrectly() {
        String result = converterService.convertDirect("11111", 2, 36);
        assertThat(result).isEqualTo("V"); // 11111 (binary) = 31 (decimal) = V (base36)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LARGE NUMBERS (BigInteger)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Large hexadecimal number (beyond long)")
    void convert_largeHexNumber_shouldConvertSuccessfully() {
        // 2^100 em hex
        String hex = "10000000000000000000000000";
        String result = converterService.convertDirect(hex, 16, 10);
        assertThat(result).isEqualTo("1267650600228229401496703205376");
    }

    @Test
    @DisplayName("Large decimal number to binary")
    void convert_largeDecimalToBinary_shouldConvertSuccessfully() {
        // 2^63 = 9223372036854775808, binary is 1 followed by 63 zeros (64 bits)
        String decimal = "9223372036854775808";
        String result = converterService.convertDirect(decimal, 10, 2);
        assertThat(result).isEqualTo("1000000000000000000000000000000000000000000000000000000000000000"); // 64 bits
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VALIDATION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Null value → should throw IllegalArgumentException")
    void convert_nullValue_throwsException() {
        assertThatThrownBy(() -> converterService.convertDirect(null, 10, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Value cannot be null");
    }

    @Test
    @DisplayName("Empty value → should throw IllegalArgumentException")
    void convert_emptyValue_throwsException() {
        assertThatThrownBy(() -> converterService.convertDirect("   ", 10, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Value cannot be empty");
    }

    @Test
    @DisplayName("Invalid fromBase (1) → should throw IllegalArgumentException")
    void convert_invalidFromBase_throwsException() {
        assertThatThrownBy(() -> converterService.convertDirect("10", 1, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("From base must be between 2 and 36");
    }

    @Test
    @DisplayName("Invalid toBase (37) → should throw IllegalArgumentException")
    void convert_invalidToBase_throwsException() {
        assertThatThrownBy(() -> converterService.convertDirect("10", 10, 37))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("To base must be between 2 and 36");
    }

    @Test
    @DisplayName("Value too long (>100 chars) → should throw IllegalArgumentException")
    void convert_valueTooLong_throwsException() {
        String longValue = "1".repeat(101);
        assertThatThrownBy(() -> converterService.convertDirect(longValue, 2, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Value is too long");
    }

    @Test
    @DisplayName("Invalid character for binary (e.g., '2') → should throw IllegalArgumentException")
    void convert_invalidBinaryChar_throwsException() {
        assertThatThrownBy(() -> converterService.convertDirect("102", 2, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not valid for base 2");
    }

    @Test
    @DisplayName("Invalid character for octal (e.g., '8') → should throw IllegalArgumentException")
    void convert_invalidOctalChar_throwsException() {
        assertThatThrownBy(() -> converterService.convertDirect("89", 8, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not valid for base 8");
    }

    @Test
    @DisplayName("Invalid character for decimal (e.g., 'A') → should throw IllegalArgumentException")
    void convert_invalidDecimalChar_throwsException() {
        assertThatThrownBy(() -> converterService.convertDirect("12A", 10, 2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not valid for base 10");
    }

    @Test
    @DisplayName("Invalid character for hex (e.g., 'G') → should throw IllegalArgumentException")
    void convert_invalidHexChar_throwsException() {
        assertThatThrownBy(() -> converterService.convertDirect("FG", 16, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not valid for base 16");
    }

    @Test
    @DisplayName("0x prefix with empty value")
    void convert_0xPrefixOnly_throwsException() {
        assertThatThrownBy(() -> converterService.convertDirect("0x", 16, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Value after 0x prefix is empty");
    }

    @Test
    @DisplayName("Invalid numeric format → should throw during character validation")
    void convert_invalidCharacterForBase_throwsException() {
        assertThatThrownBy(() -> converterService.convertDirect("ZZZ", 16, 10))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("is not valid for base 16");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAME BASE CONVERSION
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Same base conversion should return original (normalized)")
    void convert_sameBase_shouldReturnSameValue() {
        assertThat(converterService.convertDirect("FF", 16, 16)).isEqualTo("FF");
        assertThat(converterService.convertDirect("1010", 2, 2)).isEqualTo("1010");
        assertThat(converterService.convertDirect("255", 10, 10)).isEqualTo("255");
    }
}
