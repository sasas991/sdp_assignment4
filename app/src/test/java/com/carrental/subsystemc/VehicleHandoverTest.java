package com.carrental.subsystemc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VehicleHandoverTest {

    private VehicleHandover newHandover() {
        return new VehicleHandover("RES-1", "CAR-1");
    }

    @Test
    void startsWithNewStatus() {
        assertEquals(
                VehicleHandover.Status.NEW,
                newHandover().getStatus()
        );
    }

    @Test
    void marksHandoverAsInspected() {
        VehicleHandover handover = newHandover();

        handover.markInspected();

        assertEquals(
                VehicleHandover.Status.INSPECTED,
                handover.getStatus()
        );
    }

    @Test
    void completesInspectedHandover() {
        VehicleHandover handover = newHandover();
        handover.markInspected();

        handover.complete();

        assertEquals(
                VehicleHandover.Status.COMPLETED,
                handover.getStatus()
        );
    }

    @Test
    void cannotCompleteWithoutInspection() {
        VehicleHandover handover = newHandover();

        assertThrows(
                IllegalStateException.class,
                handover::complete
        );
    }

    @Test
    void cannotInspectHandoverTwice() {
        VehicleHandover handover = newHandover();
        handover.markInspected();

        assertThrows(
                IllegalStateException.class,
                handover::markInspected
        );
    }

    @Test
    void canRejectNewHandover() {
        VehicleHandover handover = newHandover();

        handover.reject();

        assertEquals(
                VehicleHandover.Status.REJECTED,
                handover.getStatus()
        );
    }

    @Test
    void canRejectInspectedHandover() {
        VehicleHandover handover = newHandover();
        handover.markInspected();

        handover.reject();

        assertEquals(
                VehicleHandover.Status.REJECTED,
                handover.getStatus()
        );
    }

    @Test
    void cannotRejectCompletedHandover() {
        VehicleHandover handover = newHandover();
        handover.markInspected();
        handover.complete();

        assertThrows(
                IllegalStateException.class,
                handover::reject
        );
    }

    @Test
    void rejectsBlankReservationId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VehicleHandover(" ", "CAR-1")
        );
    }

    @Test
    void rejectsBlankVehicleId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new VehicleHandover("RES-1", " ")
        );
    }
}
