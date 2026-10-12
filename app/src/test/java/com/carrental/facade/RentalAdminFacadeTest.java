package com.carrental.facade;

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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RentalAdminFacadeTest {

    @TempDir
    Path tempDir;

    private ReservationRepository reservations;
    private ProtectionRepository protections;
    private HandoverRepository handovers;
    private WorkflowRepository workflows;
    private RentalAdminFacade admin;

    @BeforeEach
    void setUp() {
        reservations = new ReservationRepository(
                tempDir.resolve("reservations.json")
        );
        protections = new ProtectionRepository(
                tempDir.resolve("protections.json")
        );
        handovers = new HandoverRepository(
                tempDir.resolve("handovers.json")
        );
        workflows = new WorkflowRepository(
                tempDir.resolve("workflows.json")
        );

        RentalFacade rental = new RentalFacade(
                reservations,
                new FleetAvailabilityTracker(),
                new PricingEngine(),
                protections,
                new InsuranceCalculator(),
                new DepositHandler(),
                handovers,
                new DamageInspector(),
                new GpsTracker(),
                workflows
        );

        admin = new RentalAdminFacade(
                rental,
                workflows,
                reservations,
                protections,
                handovers
        );
    }

    @Test
    void listsRecordsFromAllSubsystems() {
        reservations.save(
                new RentalReservation("RES-1", "CAR-1", 2)
        );
        protections.save(
                new InsuranceDeposit(
                        "RES-1",
                        new BigDecimal("2000"),
                        new BigDecimal("100000")
                )
        );
        handovers.save(
                new VehicleHandover("RES-1", "CAR-1")
        );
        workflows.save(new Workflow("WF-1", "RES-1"));

        assertEquals(1, admin.listReservations().size());
        assertEquals(1, admin.listProtections().size());
        assertEquals(1, admin.listHandovers().size());
        assertEquals(1, admin.listWorkflows().size());
    }

    @Test
    void inspectsWorkflowById() {
        workflows.save(new Workflow("WF-2", "RES-2"));

        Workflow result = admin.inspectWorkflow("WF-2");

        assertEquals("WF-2", result.getWorkflowId());
        assertEquals("RES-2", result.getReservationId());
        assertEquals(
                Workflow.Status.PENDING_B,
                result.getStatus()
        );
    }

    @Test
    void throwsWhenWorkflowDoesNotExist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> admin.inspectWorkflow("UNKNOWN")
        );
    }

    @Test
    void manuallyCompensatesPendingWorkflow() {
        RentalReservation reservation =
                new RentalReservation("RES-3", "CAR-3", 2);
        reservation.reserve();
        reservations.save(reservation);

        workflows.save(new Workflow("WF-3", "RES-3"));

        Workflow result = admin.manuallyCompensate("WF-3");

        assertEquals(
                Workflow.Status.COMPENSATED,
                result.getStatus()
        );
        assertEquals(
                RentalReservation.Status.RELEASED,
                reservations.findAll().get(0).getStatus()
        );
    }

    @Test
    void cannotManuallyCompensateCompletedWorkflow() {
        Workflow workflow = new Workflow("WF-4", "RES-4");
        workflow.markPendingC();
        workflow.complete();
        workflows.save(workflow);

        assertThrows(
                IllegalStateException.class,
                () -> admin.manuallyCompensate("WF-4")
        );
    }
}
