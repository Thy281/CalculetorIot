package com.calculator.Iot.service;

import com.calculator.Iot.dto.CalculationRequest;
import com.calculator.Iot.model.Calculation;
import com.calculator.Iot.repository.CalculationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculatorService Unit Tests")
class CalculatorServiceTest {

    @Mock
    private CalculationRepository calculationRepository;

    @InjectMocks
    private CalculatorService calculatorService;

    private Calculation savedCalculation;

    @BeforeEach
    void setUp() {
        savedCalculation = Calculation.builder()
                .id(1L)
                .result(0L)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADDITION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Binary + Decimal → should return correct decimal sum")
    void calculate_binaryPlusDecimal_returnsDecimalResult() {
        // 1010 (binary) = 10 | 5 (decimal) = 5 → 10 + 5 = 15
        CalculationRequest request = new CalculationRequest("1010", 2, "5", 10, "+");
        savedCalculation.setResult(15L);
        when(calculationRepository.save(any(Calculation.class))).thenReturn(savedCalculation);

        Calculation result = calculatorService.calculate(request);

        assertThat(result.getResult()).isEqualTo(15L);
    }

    @Test
    @DisplayName("Hexadecimal + Hexadecimal → should return correct decimal sum")
    void calculate_hexPlusHex_returnsDecimalResult() {
        // FF (hex) = 255 | A (hex) = 10 → 255 + 10 = 265
        CalculationRequest request = new CalculationRequest("FF", 16, "A", 16, "+");
        savedCalculation.setResult(265L);
        when(calculationRepository.save(any(Calculation.class))).thenReturn(savedCalculation);

        Calculation result = calculatorService.calculate(request);

        assertThat(result.getResult()).isEqualTo(265L);
    }

    @Test
    @DisplayName("Octal + Decimal → should return correct decimal sum")
    void calculate_octalPlusDecimal_returnsDecimalResult() {
        // 17 (octal) = 15 | 5 (decimal) = 5 → 15 + 5 = 20
        CalculationRequest request = new CalculationRequest("17", 8, "5", 10, "+");
        savedCalculation.setResult(20L);
        when(calculationRepository.save(any(Calculation.class))).thenReturn(savedCalculation);

        Calculation result = calculatorService.calculate(request);

        assertThat(result.getResult()).isEqualTo(20L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SUBTRACTION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Decimal - Binary → should return correct decimal subtraction")
    void calculate_decimalMinusBinary_returnsDecimalResult() {
        // 100 (decimal) - 1010 (binary = 10) = 90
        CalculationRequest request = new CalculationRequest("100", 10, "1010", 2, "-");
        savedCalculation.setResult(90L);
        when(calculationRepository.save(any(Calculation.class))).thenReturn(savedCalculation);

        Calculation result = calculatorService.calculate(request);

        assertThat(result.getResult()).isEqualTo(90L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MULTIPLICATION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Hex * Decimal → should return correct decimal multiplication")
    void calculate_hexTimesDecimal_returnsDecimalResult() {
        // F (hex) = 15 | 4 (decimal) = 4 → 15 * 4 = 60
        CalculationRequest request = new CalculationRequest("F", 16, "4", 10, "*");
        savedCalculation.setResult(60L);
        when(calculationRepository.save(any(Calculation.class))).thenReturn(savedCalculation);

        Calculation result = calculatorService.calculate(request);

        assertThat(result.getResult()).isEqualTo(60L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DIVISION TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Binary / Decimal → should return correct decimal division")
    void calculate_binaryDividedByDecimal_returnsDecimalResult() {
        // 1100 (binary) = 12 | 3 (decimal) = 3 → 12 / 3 = 4
        CalculationRequest request = new CalculationRequest("1100", 2, "3", 10, "/");
        savedCalculation.setResult(4L);
        when(calculationRepository.save(any(Calculation.class))).thenReturn(savedCalculation);

        Calculation result = calculatorService.calculate(request);

        assertThat(result.getResult()).isEqualTo(4L);
    }

    @Test
    @DisplayName("Division by zero → should throw ArithmeticException")
    void calculate_divisionByZero_throwsArithmeticException() {
        CalculationRequest request = new CalculationRequest("10", 10, "0", 10, "/");

        assertThatThrownBy(() -> calculatorService.calculate(request))
                .isInstanceOf(ArithmeticException.class)
                .hasMessageContaining("Division by zero is not allowed");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // INVALID INPUT TESTS
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Invalid operator → should throw IllegalArgumentException")
    void calculate_invalidOperator_throwsIllegalArgumentException() {
        CalculationRequest request = new CalculationRequest("10", 10, "5", 10, "%");

        assertThatThrownBy(() -> calculatorService.calculate(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid operator: %");
    }

    @Test
    @DisplayName("Invalid binary value → should throw NumberFormatException")
    void calculate_invalidBinaryValue_throwsNumberFormatException() {
        // "9" não é um valor binário válido
        CalculationRequest request = new CalculationRequest("9", 2, "1", 10, "+");

        assertThatThrownBy(() -> calculatorService.calculate(request))
                .isInstanceOf(NumberFormatException.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HISTORY TEST
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getHistory → should return all saved calculations")
    void getHistory_returnsAllCalculations() {
        List<Calculation> mockHistory = List.of(
                Calculation.builder().id(1L).result(10L).build(),
                Calculation.builder().id(2L).result(265L).build()
        );
        when(calculationRepository.findAll()).thenReturn(mockHistory);

        List<Calculation> history = calculatorService.getHistory();

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getResult()).isEqualTo(10L);
        assertThat(history.get(1).getResult()).isEqualTo(265L);
    }
}

