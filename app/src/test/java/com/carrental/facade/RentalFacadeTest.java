package com.carrental.facade;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.carrental.subsystema.FleetAvailabilityTracker;
import com.carrental.subsystema.PricingEngine;
import com.carrental.subsystema.RentalReservation;
import com.carrental.subsystema.ReservationRepository;
import com.carrental.subsystemb.DepositHandler;
import com.carrental.subsystemb.InsuranceCalculator;
import com.carrental.subsystemb.InsuranceDeposit;
import com.carrental.subsystemb.ProtectionRepository;
import com.carrental.subsystemc.DamageInspector;
import com.carrental.subsystemc.GpsTracker;
import com.carrental.subsystemc.HandoverRepository;
import com.carrental.subsystemc.VehicleHandover;
import com.carrental.workflow.Workflow;
import com.carrental.workflow.WorkflowRepository;

class RentalFacadeTest {

    @TempDir
    Path tempDir;

    private RentalFacade newFacade() {
        return new RentalFacade(
                reservations(),
                new FleetAvailabilityTracker(),
                new PricingEngine(),
                protections(),
                new InsuranceCalculator(),
                new DepositHandler(),
                handovers(),
                new DamageInspector(),
                new GpsTracker(),
                workflows()
        );
    }

    private ReservationRepository reservations() {
        return new ReservationRepository(
                tempDir.resolve("reservations.json")
        );
    }

    private ProtectionRepository protections() {
        return new ProtectionRepository(
                tempDir.resolve("protections.json")
        );
    }

    private HandoverRepository handovers() {
        return new HandoverRepository(
                tempDir.resolve("handovers.json")
        );
    }

    private WorkflowRepository workflows() {
        return new WorkflowRepository(
                tempDir.resolve("workflows.json")
        );
    }

    @Test
    void aRentalFailingAtBCompensatesReservationA() {
        RentalFacade facade = newFacade();

        aRentalFailingAtBIsCompensated(facade);

        assertEquals(
                Workflow.Status.COMPENSATED,
                workflows().findAll().get(0).getStatus()
        );

        assertEquals(
                RentalReservation.Status.RELEASED,
                reservations().findAll().get(0).getStatus()
        );

        assertTrue(protections().findAll().isEmpty());
    }

    private void aRentalFailingAtBIsCompensated(
            RentalFacade facade
    ) {
        assertThrows(
                IllegalStateException.class,
                () -> facade.rent(
                        "WF-B",
                        "RES-B",
                        "CAR-B",
                        1,
                        new BigDecimal("20000"),
                        new BigDecimal("600000"),
                        DamageInspector.DamageLevel.NONE
                )
        );
    }

    @Test
    void aRentalFailingAtCCompensatesBThenA() {
        RentalFacade facade = newFacade();

        assertThrows(
                IllegalStateException.class,
                () -> facade.rent(
                        "WF-C",
                        "RES-C",
                        "CAR-C",
                        1,
                        new BigDecimal("20000"),
                        new BigDecimal("100000"),
                        DamageInspector.DamageLevel.SERIOUS
                )
        );

        assertEquals(
                Workflow.Status.COMPENSATED,
                workflows().findAll().get(0).getStatus()
        );

        assertEquals(
                RentalReservation.Status.RELEASED,
                reservations().findAll().get(0).getStatus()
        );

        assertEquals(
                InsuranceDeposit.Status.RELEASED,
                protections().findAll().get(0).getStatus()
        );

        assertEquals(
                VehicleHandover.Status.REJECTED,
                handovers().findAll().get(0).getStatus()
        );
    }

    @Test
    void aRestartedFacadeCompensatesFromPersistedWorkflowState() {
        RentalReservation reservation = new RentalReservation(
                "RES-RESTART", "CAR-RESTART", 2
        );
        reservation.reserve();
        reservations().save(reservation);

        Workflow workflow = new Workflow(
                "WF-RESTART", "RES-RESTART"
        );
        workflows().save(workflow);

        RentalFacade restartedFacade = newFacade();

        Workflow result = restartedFacade.compensate("WF-RESTART");

        assertEquals(
                Workflow.Status.COMPENSATED,
                result.getStatus()
        );

        assertEquals(
                RentalReservation.Status.RELEASED,
                reservations().findAll().get(0).getStatus()
        );

        assertEquals(
                Workflow.Status.COMPENSATED,
                workflows().findAll().get(0).getStatus()
        );
    }
}
