package com.carrental.subsystemb;

import java.math.BigDecimal;

public class DepositHandler {

    private static final BigDecimal MAX_DEPOSIT =
            new BigDecimal("500000");

    public void hold(InsuranceDeposit record) {
        if (record == null) {
            throw new IllegalArgumentException(
                    "insurance record is required"
            );
        }

        if (record.getDepositAmount().compareTo(MAX_DEPOSIT) > 0) {
            throw new IllegalStateException(
                    "deposit exceeds the allowed limit"
            );
        }

        record.holdDeposit();
    }

    public void release(InsuranceDeposit record) {
        if (record == null) {
            throw new IllegalArgumentException(
                    "insurance record is required"
            );
        }

        record.releaseDeposit();
    }
}
