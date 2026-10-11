package com.carrental.subsystemc;

import java.util.HashSet;
import java.util.Set;

public class GpsTracker {

    private final Set<String> activeVehicles = new HashSet<>();

    public void startTracking(String vehicleId) {
        validateVehicleId(vehicleId);

        if (!activeVehicles.add(vehicleId)) {
            throw new IllegalStateException(
                    "GPS tracking is already active for this vehicle"
            );
        }

        System.out.println("GPS tracking started: " + vehicleId);
    }

    public void stopTracking(String vehicleId) {
        validateVehicleId(vehicleId);

        if (!activeVehicles.remove(vehicleId)) {
            throw new IllegalStateException(
                    "GPS tracking is not active for this vehicle"
            );
        }

        System.out.println("GPS tracking stopped: " + vehicleId);
    }

    public boolean isTracking(String vehicleId) {
        validateVehicleId(vehicleId);
        return activeVehicles.contains(vehicleId);
    }

    private void validateVehicleId(String vehicleId) {
        if (vehicleId == null || vehicleId.isBlank()) {
            throw new IllegalArgumentException(
                    "vehicle ID is required"
            );
        }
    }
}
