package com.carrental.subsystema;

import java.util.List;

public class FleetAvailabilityTracker {

    public boolean isAvailable(
            String vehicleId,
            List<RentalReservation> reservations
    ) {
        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException("Vehicle ID is required");
        }

        return reservations.stream()
                .noneMatch(reservation ->
                        reservation.getVehicleId().equals(vehicleId)
                                && reservation.getStatus()
                                == RentalReservation.Status.RESERVED
                );
    }
}
