package com.carrental.subsystema;

public class RentalReservation {

    public enum Status {
        NEW,
        RESERVED,
        RELEASED
    }

    private final String reservationId;
    private final String vehicleId;
    private final int rentalDays;

    private Status status = Status.NEW;

    public RentalReservation(
            String reservationId,
            String vehicleId,
            int rentalDays
    ) {
        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException("reservation ID is required");
        }

        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException("vehicle ID is required");
        }

        if (rentalDays <= 0) {
            throw new IllegalArgumentException("rental days must be positive");
        }

        this.reservationId = reservationId;
        this.vehicleId = vehicleId;
        this.rentalDays = rentalDays;
    }

    public void reserve() {
        if (status != Status.NEW) {
            throw new IllegalStateException(
                    "only a new reservation can be reserved"
            );
        }

        status = Status.RESERVED;
    }

    public void release() {
        if (status != Status.RESERVED) {
            throw new IllegalStateException(
                    "only a reserved vehicle can be released"
            );
        }

        status = Status.RELEASED;
    }

    public String getReservationId() {
        return reservationId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public Status getStatus() {
        return status;
    }
}
