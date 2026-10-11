
package com.carrental.subsystema;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FleetAvailabilityTrackerTest {

    private final FleetAvailabilityTracker tracker =
            new FleetAvailabilityTracker();

    @Test
    void availableCarCanBeReserved() {
        assertTrue(
                tracker.isAvailable("CAR-1", List.of())
        );
    }

    @Test
    void reservedCarIsUnavailable() {
        RentalReservation reservation =
                new RentalReservation("RES-1", "CAR-1", 2);
        reservation.reserve();

        assertFalse(
                tracker.isAvailable("CAR-1", List.of(reservation))
        );
    }

    @Test
    void releasedCarBecomesAvailableAgain() {
        RentalReservation reservation =
                new RentalReservation("RES-2", "CAR-1", 2);
        reservation.reserve();
        reservation.release();

        assertTrue(
                tracker.isAvailable("CAR-1", List.of(reservation))
        );
    }

    @Test
    void reservationForAnotherCarDoesNotBlockAvailability() {
        RentalReservation reservation =
                new RentalReservation("RES-3", "CAR-2", 2);
        reservation.reserve();

        assertTrue(
                tracker.isAvailable("CAR-1", List.of(reservation))
        );
    }

    @Test
    void blankVehicleIdIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tracker.isAvailable(" ", List.of())
        );
    }
}
