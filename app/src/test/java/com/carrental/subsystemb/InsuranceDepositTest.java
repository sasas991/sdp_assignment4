
package com.carrental.subsystemb;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class InsuranceDepositTest {

    private InsuranceDeposit createRecord() {
        return new InsuranceDeposit(
                "RES-1",
                new BigDecimal("2000"),
                new BigDecimal("100000")
        );
    }

    @Test
    void newRecordStartsWithNewStatus() {
        assertEquals(
                InsuranceDeposit.Status.NEW,
                createRecord().getStatus()
        );
    }

    @Test
    void approvingInsuranceChangesStatus() {
        InsuranceDeposit record = createRecord();

        record.approveInsurance();

        assertEquals(
                InsuranceDeposit.Status.INSURANCE_APPROVED,
                record.getStatus()
        );
    }

    @Test
    void depositCanBeHeldAfterInsuranceApproval() {
        InsuranceDeposit record = createRecord();

        record.approveInsurance();
        record.holdDeposit();

        assertEquals(
                InsuranceDeposit.Status.DEPOSIT_HELD,
                record.getStatus()
        );
    }

    @Test
    void heldDepositCanBeReleased() {
        InsuranceDeposit record = createRecord();

        record.approveInsurance();
        record.holdDeposit();
        record.releaseDeposit();

        assertEquals(
                InsuranceDeposit.Status.RELEASED,
                record.getStatus()
        );
    }

    @Test
    void cannotHoldDepositBeforeInsuranceApproval() {
        InsuranceDeposit record = createRecord();

        assertThrows(
                IllegalStateException.class,
                record::holdDeposit
        );
    }

    @Test
    void cannotApproveInsuranceTwice() {
        InsuranceDeposit record = createRecord();
        record.approveInsurance();

        assertThrows(
                IllegalStateException.class,
                record::approveInsurance
        );
    }

    @Test
    void canRejectNewInsuranceRecord() {
        InsuranceDeposit record = createRecord();

        record.reject();

        assertEquals(
                InsuranceDeposit.Status.REJECTED,
                record.getStatus()
        );
    }
}
