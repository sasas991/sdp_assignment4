
package com.carrental.subsystema;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RentalReservationTest {

    @Test
    void newReservationStartsWithNewStatus() {
        RentalReservation reservation =
                new RentalReservation("RES-1", "CAR-1", 3);

        assertEquals(
                RentalReservation.Status.NEW,
                reservation.getStatus()
        );
    }

    @Test
    void reservingChangesStatusToReserved() {
        RentalReservation reservation =
                new RentalReservation("RES-2", "CAR-2", 2);

        reservation.reserve();

        assertEquals(
                RentalReservation.Status.RESERVED,
                reservation.getStatus()
        );
    }

    @Test
    void releasingReservedCarChangesStatusToReleased() {
        RentalReservation reservation =
                new RentalReservation("RES-3", "CAR-3", 1);

        reservation.reserve();
        reservation.release();

        assertEquals(
                RentalReservation.Status.RELEASED,
                reservation.getStatus()
        );
    }

    @Test
    void cannotReleaseReservationBeforeReserving() {
        RentalReservation reservation =
                new RentalReservation("RES-4", "CAR-4", 1);

        assertThrows(
                IllegalStateException.class,
                reservation::release
        );
    }

    @Test
    void cannotReserveTheSameReservationTwice() {
        RentalReservation reservation =
                new RentalReservation("RES-5", "CAR-5", 1);

        reservation.reserve();

        assertThrows(
                IllegalStateException.class,
                reservation::reserve
        );
    }

    @Test
    void cannotCreateReservationWithInvalidRentalDays() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RentalReservation("RES-6", "CAR-6", 0)
        );
    }
}
