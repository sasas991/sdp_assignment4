package com.carrental.subsystema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationRepositoryTest {

    @TempDir
    Path tempDir;

    private ReservationRepository newRepository() {
        return new ReservationRepository(
                tempDir.resolve("reservations.json")
        );
    }

    @Test
    void returnsEmptyListWhenFileDoesNotExist() {
        assertTrue(newRepository().findAll().isEmpty());
    }

    @Test
    void savesAndLoadsReservation() {
        ReservationRepository repository = newRepository();
        RentalReservation reservation =
                new RentalReservation("RES-1", "CAR-1", 3);

        reservation.reserve();
        repository.save(reservation);

        List<RentalReservation> loaded = repository.findAll();

        assertEquals(1, loaded.size());
        assertEquals("RES-1", loaded.get(0).getReservationId());
        assertEquals("CAR-1", loaded.get(0).getVehicleId());
        assertEquals(3, loaded.get(0).getRentalDays());
        assertEquals(
                RentalReservation.Status.RESERVED,
                loaded.get(0).getStatus()
        );
    }

    @Test
    void persistsReleasedStatus() {
        ReservationRepository repository = newRepository();
        RentalReservation reservation =
                new RentalReservation("RES-2", "CAR-2", 2);

        reservation.reserve();
        reservation.release();
        repository.save(reservation);

        assertEquals(
                RentalReservation.Status.RELEASED,
                repository.findAll().get(0).getStatus()
        );
    }

    @Test
    void updatesExistingReservationInsteadOfDuplicatingIt() {
        ReservationRepository repository = newRepository();
        RentalReservation reservation =
                new RentalReservation("RES-3", "CAR-3", 1);

        reservation.reserve();
        repository.save(reservation);

        reservation.release();
        repository.save(reservation);

        List<RentalReservation> loaded = repository.findAll();

        assertEquals(1, loaded.size());
        assertEquals(
                RentalReservation.Status.RELEASED,
                loaded.get(0).getStatus()
        );
    }

    @Test
    void persistsMultipleReservations() {
        ReservationRepository repository = newRepository();

        repository.save(
                new RentalReservation("RES-4", "CAR-4", 2)
        );
        repository.save(
                new RentalReservation("RES-5", "CAR-5", 4)
        );

        assertEquals(2, repository.findAll().size());
    }
}
