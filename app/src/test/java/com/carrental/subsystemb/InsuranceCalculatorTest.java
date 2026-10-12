
package com.carrental.subsystemb;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InsuranceCalculatorTest {

    private final InsuranceCalculator calculator =
            new InsuranceCalculator();

    @Test
    void calculatesTenPercentOfRentalPrice() {
        BigDecimal result = calculator.calculateInsurance(
                new BigDecimal("60000")
        );

        assertEquals(new BigDecimal("6000.00"), result);
    }

    @Test
    void roundsInsuranceCostToTwoDecimalPlaces() {
        BigDecimal result = calculator.calculateInsurance(
                new BigDecimal("100.05")
        );

        assertEquals(new BigDecimal("10.01"), result);
    }

    @Test
    void rejectsZeroRentalPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateInsurance(BigDecimal.ZERO)
        );
    }

    @Test
    void rejectsNegativeRentalPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateInsurance(
                        new BigDecimal("-100")
                )
        );
    }

    @Test
    void rejectsNullRentalPrice() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateInsurance(null)
        );
    }
}
