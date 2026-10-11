package com.carrental.subsystema;

import java.math.BigDecimal;

public class PricingEngine {

    public BigDecimal calculateTotalPrice(
            int rentalDays,
            BigDecimal dailyRate
    ) {
        if (rentalDays <= 0) {
            throw new IllegalArgumentException(
                    "rental days must be positive"
            );
        }

        if (dailyRate == null || dailyRate.signum() <= 0) {
            throw new IllegalArgumentException(
                    "daily rate must be positive"
            );
        }

        return dailyRate.multiply(BigDecimal.valueOf(rentalDays));
    }
}
