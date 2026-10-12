package com.carrental.subsystemc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class GpsTrackerTest {

    private final GpsTracker tracker = new GpsTracker();

    @Test
    void startsTrackingVehicle() {
        tracker.startTracking("CAR-1");

        assertTrue(tracker.isTracking("CAR-1"));
    }

    @Test
    void stopsTrackingVehicle() {
        tracker.startTracking("CAR-1");

        tracker.stopTracking("CAR-1");

        assertFalse(tracker.isTracking("CAR-1"));
    }

    @Test
    void tracksVehiclesIndependently() {
        tracker.startTracking("CAR-1");
        tracker.startTracking("CAR-2");

        tracker.stopTracking("CAR-1");

        assertFalse(tracker.isTracking("CAR-1"));
        assertTrue(tracker.isTracking("CAR-2"));
    }

    @Test
    void newTrackerHasNoActiveTracking() {
        assertFalse(tracker.isTracking("CAR-1"));
    }

    @Test
    void rejectsDuplicateTracking() {
        tracker.startTracking("CAR-1");

        assertThrows(
                IllegalStateException.class,
                () -> tracker.startTracking("CAR-1")
        );
    }

    @Test
    void rejectsStoppingInactiveTracking() {
        assertThrows(
                IllegalStateException.class,
                () -> tracker.stopTracking("CAR-1")
        );
    }

    @Test
    void rejectsBlankVehicleId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tracker.startTracking(" ")
        );
    }
}