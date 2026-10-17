
package com.carrental.facade;

import java.math.BigDecimal;
import java.util.List;

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

public class RentalFacade {

    private final ReservationRepository reservationRepository;
    private final FleetAvailabilityTracker availabilityTracker;
    private final PricingEngine pricingEngine;

    private final ProtectionRepository protectionRepository;
    private final InsuranceCalculator insuranceCalculator;
    private final DepositHandler depositHandler;

    private final HandoverRepository handoverRepository;
    private final DamageInspector damageInspector;
    private final GpsTracker gpsTracker;

    private final WorkflowRepository workflowRepository;

    public RentalFacade() {
        this(
                new ReservationRepository(),
                new FleetAvailabilityTracker(),
                new PricingEngine(),
                new ProtectionRepository(),
                new InsuranceCalculator(),
                new DepositHandler(),
                new HandoverRepository(),
                new DamageInspector(),
                new GpsTracker(),
                new WorkflowRepository()
        );
    }

    public RentalFacade(
            ReservationRepository reservationRepository,
            FleetAvailabilityTracker availabilityTracker,
            PricingEngine pricingEngine,
            ProtectionRepository protectionRepository,
            InsuranceCalculator insuranceCalculator,
            DepositHandler depositHandler,
            HandoverRepository handoverRepository,
            DamageInspector damageInspector,
            GpsTracker gpsTracker,
            WorkflowRepository workflowRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.availabilityTracker = availabilityTracker;
        this.pricingEngine = pricingEngine;
        this.protectionRepository = protectionRepository;
        this.insuranceCalculator = insuranceCalculator;
        this.depositHandler = depositHandler;
        this.handoverRepository = handoverRepository;
        this.damageInspector = damageInspector;
        this.gpsTracker = gpsTracker;
        this.workflowRepository = workflowRepository;
    }

    /**
     * Runs the complete rental process: A -> B -> C.
     */
    public Workflow rent(
            String workflowId,
            String reservationId,
            String vehicleId,
            int rentalDays,
            BigDecimal dailyRate,
            BigDecimal depositAmount,
            DamageInspector.DamageLevel damageLevel
    ) {
        Workflow workflow = startRental(
                workflowId,
                reservationId,
                vehicleId,
                rentalDays,
                dailyRate,
                depositAmount,
                damageLevel
        );

        BigDecimal rentalPrice = pricingEngine.calculateTotalPrice(
                rentalDays,
                dailyRate
        );

        continueWorkflow(
                workflow,
                rentalPrice,
                damageLevel
        );

        return workflow;
    }

    /**
     * Executes only subsystem A and persists the workflow.
     * Subsystems B and C are not executed yet.
     */
    public Workflow startRental(
            String workflowId,
            String reservationId,
            String vehicleId,
            int rentalDays,
            BigDecimal dailyRate,
            BigDecimal depositAmount,
            DamageInspector.DamageLevel damageLevel
    ) {
        if (depositAmount == null || depositAmount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "deposit amount must be positive"
            );
        }

        if (damageLevel == null) {
            throw new IllegalArgumentException(
                    "damage level is required"
            );
        }

        // Validate rental parameters before changing persisted state.
        BigDecimal rentalPrice = pricingEngine.calculateTotalPrice(
                rentalDays,
                dailyRate
        );

        RentalReservation reservation = new RentalReservation(
                reservationId,
                vehicleId,
                rentalDays
        );

        List<RentalReservation> reservations =
                reservationRepository.findAll();

        if (reservations.stream().anyMatch(
                item -> item.getReservationId().equals(reservationId))) {
            throw new IllegalArgumentException(
                    "reservation ID already exists"
            );
        }

        if (workflowRepository.findAll().stream().anyMatch(
                item -> item.getWorkflowId().equals(workflowId))) {
            throw new IllegalArgumentException(
                    "workflow ID already exists"
            );
        }

        if (!availabilityTracker.isAvailable(vehicleId, reservations)) {
            throw new IllegalStateException(
                    "vehicle is not available"
            );
        }

        // Stage A: reserve the vehicle and persist the reservation.
        reservation.reserve();
        reservationRepository.save(reservation);

        // Persist the workflow and all parameters required for resuming.
        Workflow workflow = new Workflow(
                workflowId,
                reservationId
        );

        workflow.setRentalDetails(
                vehicleId,
                rentalDays,
                dailyRate,
                depositAmount,
                damageLevel.name()
        );

        workflowRepository.save(workflow);

        System.out.println(
                "stage A completed. Workflow saved with status: "
                        + workflow.getStatus()
        );

        return workflow;
    }

    /**
     * Resumes a saved workflow after an application restart.
     */
    public Workflow resume(String workflowId) {
        Workflow workflow = workflowRepository.findAll()
                .stream()
                .filter(item -> item.getWorkflowId().equals(workflowId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "workflow not found: " + workflowId
                ));

        if (workflow.getStatus() == Workflow.Status.COMPLETED) {
            return workflow;
        }

        if (workflow.getStatus() == Workflow.Status.COMPENSATED) {
            throw new IllegalStateException(
                    "a compensated workflow cannot be resumed"
            );
        }

        if (workflow.getVehicleId() == null
                || workflow.getDailyRate() == null
                || workflow.getDepositAmount() == null
                || workflow.getDamageLevel() == null
                || workflow.getRentalDays() <= 0) {
            throw new IllegalStateException(
                    "workflow does not contain rental details"
            );
        }

        BigDecimal rentalPrice = pricingEngine.calculateTotalPrice(
                workflow.getRentalDays(),
                workflow.getDailyRate()
        );

        DamageInspector.DamageLevel damageLevel =
                DamageInspector.DamageLevel.valueOf(
                        workflow.getDamageLevel()
                );

        continueWorkflow(
                workflow,
                rentalPrice,
                damageLevel
        );

        return workflow;
    }

    private void continueWorkflow(
            Workflow workflow,
            BigDecimal rentalPrice,
            DamageInspector.DamageLevel damageLevel
    ) {
        if (workflow.getStatus() == Workflow.Status.PENDING_B) {
            try {
                executeSubsystemB(
                        workflow,
                        rentalPrice,
                        workflow.getDepositAmount()
                );

                workflow.markPendingC();
                workflowRepository.save(workflow);

            } catch (RuntimeException failure) {
                compensate(workflow.getWorkflowId());

                throw new IllegalStateException(
                        "subsystem B failed. reservation A was compensated.",
                        failure
                );
            }
        }

        if (workflow.getStatus() == Workflow.Status.PENDING_C) {
            try {
                executeSubsystemC(workflow, damageLevel);

                workflow.complete();
                workflowRepository.save(workflow);

            } catch (RuntimeException failure) {
                if (gpsTracker.isTracking(workflow.getVehicleId())) {
                    gpsTracker.stopTracking(workflow.getVehicleId());
                }

                compensate(workflow.getWorkflowId());

                throw new IllegalStateException(
                        "subsystem C failed. subsystems B and A were compensated.",
                        failure
                );
            }
        }
    }

    private void executeSubsystemB(
            Workflow workflow,
            BigDecimal rentalPrice,
            BigDecimal depositAmount
    ) {
        BigDecimal insuranceCost =
                insuranceCalculator.calculateInsurance(rentalPrice);

        InsuranceDeposit protection = new InsuranceDeposit(
                workflow.getReservationId(),
                insuranceCost,
                depositAmount
        );

        protection.approveInsurance();
        depositHandler.hold(protection);
        protectionRepository.save(protection);
    }

    private void executeSubsystemC(
            Workflow workflow,
            DamageInspector.DamageLevel damageLevel
    ) {
        VehicleHandover handover = new VehicleHandover(
                workflow.getReservationId(),
                workflow.getVehicleId()
        );

        if (!damageInspector.canHandOver(damageLevel)) {
            handover.reject();
            handoverRepository.save(handover);

            throw new IllegalStateException(
                    "serious vehicle damage detected"
            );
        }

        handover.markInspected();
        handoverRepository.save(handover);

        gpsTracker.startTracking(workflow.getVehicleId());

        handover.complete();
        handoverRepository.save(handover);
    }

    public Workflow compensate(String workflowId) {
        Workflow workflow = workflowRepository.findAll()
                .stream()
                .filter(item -> item.getWorkflowId().equals(workflowId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "workflow not found: " + workflowId
                ));

        if (workflow.getStatus() == Workflow.Status.COMPENSATED) {
            return workflow;
        }

        if (workflow.getStatus() == Workflow.Status.COMPLETED) {
            throw new IllegalStateException(
                    "a completed workflow cannot be compensated"
            );
        }

        protectionRepository.findAll()
                .stream()
                .filter(item -> item.getReservationId()
                        .equals(workflow.getReservationId()))
                .findFirst()
                .ifPresent(protection -> {
                    if (protection.getStatus()
                            == InsuranceDeposit.Status.DEPOSIT_HELD) {
                        depositHandler.release(protection);
                        protectionRepository.save(protection);
                    }
                });

        reservationRepository.findAll()
                .stream()
                .filter(item -> item.getReservationId()
                        .equals(workflow.getReservationId()))
                .findFirst()
                .ifPresent(reservation -> {
                    if (reservation.getStatus()
                            == RentalReservation.Status.RESERVED) {
                        reservation.release();
                        reservationRepository.save(reservation);
                    }
                });

        workflow.compensate();
        workflowRepository.save(workflow);

        return workflow;
    }
}
