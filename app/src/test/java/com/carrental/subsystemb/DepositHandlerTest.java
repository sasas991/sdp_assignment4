package com.carrental.subsystemb;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DepositHandlerTest {

    private final DepositHandler handler = new DepositHandler();

    @Test
    void holdsApprovedDeposit() {
        InsuranceDeposit deposit = new InsuranceDeposit(
                "RES-1",
                new BigDecimal("2000"),
                new BigDecimal("100000")
        );
        deposit.approveInsurance();

        handler.hold(deposit);

        assertEquals(InsuranceDeposit.Status.DEPOSIT_HELD, deposit.getStatus());
    }

    @Test
    void rejectsDepositAboveMaximum() {
        InsuranceDeposit deposit = new InsuranceDeposit(
                "RES-2",
                new BigDecimal("2000"),
                new BigDecimal("600000")
        );
        deposit.approveInsurance();

        assertThrows(IllegalStateException.class, () -> handler.hold(deposit));
        assertEquals(InsuranceDeposit.Status.INSURANCE_APPROVED, deposit.getStatus());
    }

    @Test
    void releasesHeldDeposit() {
        InsuranceDeposit deposit = new InsuranceDeposit(
                "RES-3",
                new BigDecimal("2000"),
                new BigDecimal("100000")
        );
        deposit.approveInsurance();
        handler.hold(deposit);

        handler.release(deposit);

        assertEquals(InsuranceDeposit.Status.RELEASED, deposit.getStatus());
    }
}
