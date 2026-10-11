
package com.carrental.subsystema;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PricingEngineTest {

    private final PricingEngine engine = new PricingEngine();

    @Test
    void calculatesTotalPriceForMultipleDays() {
        BigDecimal result = engine.calculateTotalPrice(
                3, new BigDecimal("20000")
        );

        assertEquals(new BigDecimal("60000"), result);
    }

    @Test
    void calculatesPriceForOneDay() {
        BigDecimal result = engine.calculateTotalPrice(
                1, new BigDecimal("15000")
        );

        assertEquals(new BigDecimal("15000"), result);
    }

    @Test
    void rejectsZeroRentalDays() {
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.calculateTotalPrice(
                        0, new BigDecimal("20000")
                )
        );
    }

    @Test
    void rejectsNegativeRentalDays() {
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.calculateTotalPrice(
                        -1, new BigDecimal("20000")
                )
        );
    }

    @Test
    void rejectsZeroDailyRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.calculateTotalPrice(
                        2, BigDecimal.ZERO
                )
        );
    }

    @Test
    void rejectsNullDailyRate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> engine.calculateTotalPrice(2, null)
        );
    }
}
