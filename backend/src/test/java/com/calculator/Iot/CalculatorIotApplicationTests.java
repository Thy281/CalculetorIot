package com.calculator.Iot;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Application Sanity Test")
class CalculatorIotApplicationTests {

    @Test
    @DisplayName("Application main class exists")
    void applicationClassExists() {
        assertThat(CalculatorIotApplication.class).isNotNull();
    }

}
