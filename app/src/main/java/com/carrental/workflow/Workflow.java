
package com.carrental.workflow;

import java.math.BigDecimal;

public class Workflow {

    public enum Status {
        PENDING_B,
        PENDING_C,
        COMPLETED,
        COMPENSATED
    }

    private final String workflowId;
    private final String reservationId;
    private Status status;

    private String vehicleId;
    private int rentalDays;
    private BigDecimal dailyRate;
    private BigDecimal depositAmount;
    private String damageLevel;

    public Workflow(String workflowId, String reservationId) {
        this(workflowId, reservationId, Status.PENDING_B);
    }

    private Workflow(
            String workflowId,
            String reservationId,
            Status status
    ) {
        if (workflowId == null || workflowId.isBlank()) {
            throw new IllegalArgumentException(
                    "workflow ID is required"
            );
        }

        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException(
                    "reservation ID is required"
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                    "workflow status is required"
            );
        }

        this.workflowId = workflowId;
        this.reservationId = reservationId;
        this.status = status;
    }

    public static Workflow restore(
            String workflowId,
            String reservationId,
            Status status
    ) {
        return new Workflow(workflowId, reservationId, status);
    }

    public void setRentalDetails(
            String vehicleId,
            int rentalDays,
            BigDecimal dailyRate,
            BigDecimal depositAmount,
            String damageLevel
    ) {
        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException(
                    "vehicle ID is required"
            );
        }

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

        if (depositAmount == null
                || depositAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "deposit amount must be positive"
            );
        }

        if (damageLevel == null || damageLevel.isBlank()) {
            throw new IllegalArgumentException(
                    "damage level is required"
            );
        }

        this.vehicleId = vehicleId;
        this.rentalDays = rentalDays;
        this.dailyRate = dailyRate;
        this.depositAmount = depositAmount;
        this.damageLevel = damageLevel;
    }

    public void markPendingC() {
        requireStatus(Status.PENDING_B);
        status = Status.PENDING_C;
    }

    public void complete() {
        requireStatus(Status.PENDING_C);
        status = Status.COMPLETED;
    }

    public void compensate() {
        if (status != Status.PENDING_B
                && status != Status.PENDING_C) {
            throw new IllegalStateException(
                    "only an unfinished workflow can be compensated"
            );
        }

        status = Status.COMPENSATED;
    }

    private void requireStatus(Status expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "expected status " + expected
                            + ", but was " + status
            );
        }
    }

    public String getWorkflowId() {
        return workflowId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public Status getStatus() {
        return status;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public BigDecimal getDailyRate() {
        return dailyRate;
    }

    public BigDecimal getDepositAmount() {
        return depositAmount;
    }

    public String getDamageLevel() {
        return damageLevel;
    }
}
