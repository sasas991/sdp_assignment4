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

    public Workflow rent(
            String workflowId,
            String reservationId,
            String vehicleId,
            int rentalDays,
            BigDecimal dailyRate,
            BigDecimal depositAmount,
            DamageInspector.DamageLevel damageLevel
    ) {
        RentalReservation reservation =
                new RentalReservation(
                        reservationId,
                        vehicleId,
                        rentalDays
                );

        Workflow workflow =
                new Workflow(workflowId, reservationId);

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

        List<RentalReservation> reservations =
                reservationRepository.findAll();

        if (reservations.stream().anyMatch(
                item -> item.getReservationId()
                        .equals(reservationId))) {
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

        BigDecimal rentalPrice =
                pricingEngine.calculateTotalPrice(
                        rentalDays,
                        dailyRate
                );

        reservation.reserve();
        reservationRepository.save(reservation);
        workflowRepository.save(workflow);

        try {
            BigDecimal insuranceCost =
                    insuranceCalculator.calculateInsurance(rentalPrice);

            InsuranceDeposit protection =
                    new InsuranceDeposit(
                            reservationId,
                            insuranceCost,
                            depositAmount
                    );

            protection.approveInsurance();
            depositHandler.hold(protection);
            protectionRepository.save(protection);

            workflow.markPendingC();
            workflowRepository.save(workflow);

        } catch (RuntimeException failure) {
            compensate(workflowId);

            throw new IllegalStateException(
                    "subsystem B failed. reservation A was compensated.",
                    failure
            );
        }

        try {
            VehicleHandover handover =
                    new VehicleHandover(reservationId, vehicleId);

            if (!damageInspector.canHandOver(damageLevel)) {
                handover.reject();
                handoverRepository.save(handover);

                throw new IllegalStateException(
                        "serious vehicle damage detected"
                );
            }

            handover.markInspected();
            handoverRepository.save(handover);

            gpsTracker.startTracking(vehicleId);

            handover.complete();
            handoverRepository.save(handover);

            workflow.complete();
            workflowRepository.save(workflow);

            return workflow;

        } catch (RuntimeException failure) {
            if (gpsTracker.isTracking(vehicleId)) {
                gpsTracker.stopTracking(vehicleId);
            }

            compensate(workflowId);

            throw new IllegalStateException(
                    "subsystem C failed. subsystems B and A were compensated.",
                    failure
            );
        }
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
