package com.carrental.subsystemb;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InsuranceCalculator {

    private static final BigDecimal INSURANCE_RATE =
            new BigDecimal("0.10");

    public BigDecimal calculateInsurance(BigDecimal rentalPrice) {
        if (rentalPrice == null || rentalPrice.signum() <= 0) {
            throw new IllegalArgumentException(
                    "rental price must be positive"
            );
        }

        return rentalPrice
                .multiply(INSURANCE_RATE)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
