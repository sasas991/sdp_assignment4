package com.carrental.subsystemc;

public class VehicleHandover {

    public enum Status {
        NEW,
        INSPECTED,
        COMPLETED,
        REJECTED
    }

    private final String reservationId;
    private final String vehicleId;

    private Status status = Status.NEW;

    public VehicleHandover(String reservationId, String vehicleId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException(
                    "reservation ID is required"
            );
        }

        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException(
                    "vehicle ID is required"
            );
        }

        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
    }

    public void markInspected() {
        if (status != Status.NEW) {
            throw new IllegalStateException(
                    "only a new handover can be inspected"
            );
        }

        status = Status.INSPECTED;
    }

    public void complete() {
        if (status != Status.INSPECTED) {
            throw new IllegalStateException(
                    "vehicle must be inspected before handover"
            );
        }

        status = Status.COMPLETED;
    }

    public void reject() {
        if (status != Status.NEW && status != Status.INSPECTED) {
            throw new IllegalStateException(
                    "this handover cannot be rejected"
            );
        }

        status = Status.REJECTED;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public Status getStatus() {
        return status;
    }
}
