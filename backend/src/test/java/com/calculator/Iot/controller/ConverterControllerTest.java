package com.calculator.Iot.controller;

import com.calculator.Iot.dto.ConverterRequest;
import com.calculator.Iot.dto.ConverterResult;
import com.calculator.Iot.service.NumberBaseConverterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@DisplayName("ConverterController Unit Tests")
class ConverterControllerTest {

    @Mock
    private NumberBaseConverterService converterService;

    @Mock
    private HttpServletRequest httpRequest;

    private ConverterController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new ConverterController(converterService);
    }

    @Test
    @DisplayName("convert() with valid request → returns ConverterResult")
    void convert_validRequest_returnsResult() {
        when(converterService.convertDirect("FF", 16, 2)).thenReturn("11111111");
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        ConverterRequest request = new ConverterRequest("FF", 16, 2);
        ResponseEntity<ConverterResult> response = controller.convert(request, httpRequest);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        ConverterResult body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.originalValue()).isEqualTo("FF");
        assertThat(body.fromBase()).isEqualTo(16);
        assertThat(body.convertedValue()).isEqualTo("11111111");
        assertThat(body.toBase()).isEqualTo(2);
    }

    @Test
    @DisplayName("convert() with decimal to binary")
    void convert_decimalToBinary_works() {
        when(converterService.convertDirect("15", 10, 2)).thenReturn("1111");

        ConverterRequest request = new ConverterRequest("15", 10, 2);
        ResponseEntity<ConverterResult> response = controller.convert(request, httpRequest);

        assertThat(response.getBody().convertedValue()).isEqualTo("1111");
    }

    @Test
    @DisplayName("convert() when service throws exception → propagates exception")
    void convert_serviceThrowsException_propagates() {
        when(converterService.convertDirect("invalid", 10, 2))
            .thenThrow(new IllegalArgumentException("Invalid value"));

        ConverterRequest request = new ConverterRequest("invalid", 10, 2);

        assertThatThrownBy(() -> controller.convert(request, httpRequest))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid value");
    }

    @Test
    @DisplayName("convert() with various bases")
    void convert_variousBases_works() {
        when(converterService.convertDirect("777", 8, 10)).thenReturn("511");
        when(converterService.convertDirect("Z", 36, 10)).thenReturn("35");

        ConverterRequest octRequest = new ConverterRequest("777", 8, 10);
        ResponseEntity<ConverterResult> octResponse = controller.convert(octRequest, httpRequest);
        assertThat(octResponse.getBody().convertedValue()).isEqualTo("511");

        ConverterRequest base36Request = new ConverterRequest("Z", 36, 10);
        ResponseEntity<ConverterResult> base36Response = controller.convert(base36Request, httpRequest);
        assertThat(base36Response.getBody().convertedValue()).isEqualTo("35");
    }
}
