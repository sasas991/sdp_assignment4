package com.carrental.subsystemb;

import java.math.BigDecimal;

public class InsuranceDeposit {

    public enum Status {
        NEW,
        INSURANCE_APPROVED,
        DEPOSIT_HELD,
        RELEASED,
        REJECTED
    }

    private final String reservationId;
    private final BigDecimal insuranceCost;
    private final BigDecimal depositAmount;

    private Status status = Status.NEW;

    public InsuranceDeposit(
            String reservationId,
            BigDecimal insuranceCost,
            BigDecimal depositAmount
    ) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException(
                    "reservation ID is required"
            );
        }

        if (insuranceCost == null || insuranceCost.signum() < 0) {
            throw new IllegalArgumentException(
                    "insurance cost cannot be negative"
            );
        }

        if (depositAmount == null || depositAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "deposit amount must be positive"
            );
        }

        this.reservationId = reservationId;
        this.insuranceCost = insuranceCost;
        this.depositAmount = depositAmount;
    }

    public void approveInsurance() {
        if (status != Status.NEW) {
            throw new IllegalStateException(
                    "insurance can only be approved for a new record"
            );
        }

        status = Status.INSURANCE_APPROVED;
    }

    public void holdDeposit() {
        if (status != Status.INSURANCE_APPROVED) {
            throw new IllegalStateException(
                    "insurance must be approved before holding deposit"
            );
        }

        status = Status.DEPOSIT_HELD;
    }

    public void releaseDeposit() {
        if (status != Status.DEPOSIT_HELD) {
            throw new IllegalStateException(
                    "only a held deposit can be released"
            );
        }

        status = Status.RELEASED;
    }

    public void reject() {
        if (status != Status.NEW
                && status != Status.INSURANCE_APPROVED) {
            throw new IllegalStateException(
                    "this insurance record cannot be rejected"
            );
        }

        status = Status.REJECTED;
    }

    public String getReservationId() {
        return reservationId;
    }

    public BigDecimal getInsuranceCost() {
        return insuranceCost;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public Status getStatus() {
        return status;
    }
}
