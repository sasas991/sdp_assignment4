package com.carrental.subsystemc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HandoverRepositoryTest {

    @TempDir
    Path tempDir;

    private HandoverRepository newRepository() {
        return new HandoverRepository(
                tempDir.resolve("handovers.json")
        );
    }

    @Test
    void returnsEmptyListWhenFileDoesNotExist() {
        assertTrue(newRepository().findAll().isEmpty());
    }

    @Test
    void savesAndLoadsNewHandover() {
        HandoverRepository repository = newRepository();
        VehicleHandover handover =
                new VehicleHandover("RES-1", "CAR-1");

        repository.save(handover);

        List<VehicleHandover> loaded = repository.findAll();

        assertEquals(1, loaded.size());
        assertEquals("RES-1", loaded.get(0).getReservationId());
        assertEquals("CAR-1", loaded.get(0).getVehicleId());
        assertEquals(
                VehicleHandover.Status.NEW,
                loaded.get(0).getStatus()
        );
    }

    @Test
    void persistsInspectedStatus() {
        HandoverRepository repository = newRepository();
        VehicleHandover handover =
                new VehicleHandover("RES-2", "CAR-2");
        handover.markInspected();

        repository.save(handover);

        assertEquals(
                VehicleHandover.Status.INSPECTED,
                repository.findAll().get(0).getStatus()
        );
    }

    @Test
    void persistsCompletedStatus() {
        HandoverRepository repository = newRepository();
        VehicleHandover handover =
                new VehicleHandover("RES-3", "CAR-3");
        handover.markInspected();
        handover.complete();

        repository.save(handover);

        assertEquals(
                VehicleHandover.Status.COMPLETED,
                repository.findAll().get(0).getStatus()
        );
    }

    @Test
    void persistsRejectedStatus() {
        HandoverRepository repository = newRepository();
        VehicleHandover handover =
                new VehicleHandover("RES-4", "CAR-4");
        handover.reject();

        repository.save(handover);

        assertEquals(
                VehicleHandover.Status.REJECTED,
                repository.findAll().get(0).getStatus()
        );
    }

    @Test
    void updatesExistingHandoverInsteadOfDuplicatingIt() {
        HandoverRepository repository = newRepository();
        VehicleHandover handover =
                new VehicleHandover("RES-5", "CAR-5");

        repository.save(handover);

        handover.markInspected();
        repository.save(handover);

        List<VehicleHandover> loaded = repository.findAll();

        assertEquals(1, loaded.size());
        assertEquals(
                VehicleHandover.Status.INSPECTED,
                loaded.get(0).getStatus()
        );
    }

    @Test
    void persistsMultipleHandovers() {
        HandoverRepository repository = newRepository();

        repository.save(
                new VehicleHandover("RES-6", "CAR-6")
        );
        repository.save(
                new VehicleHandover("RES-7", "CAR-7")
        );

        assertEquals(2, repository.findAll().size());
    }
}
