package com.carrental.facade;

import java.util.List;

import com.carrental.subsystema.RentalReservation;
import com.carrental.subsystema.ReservationRepository;
import com.carrental.subsystemb.InsuranceDeposit;
import com.carrental.subsystemb.ProtectionRepository;
import com.carrental.subsystemc.HandoverRepository;
import com.carrental.subsystemc.VehicleHandover;
import com.carrental.workflow.Workflow;
import com.carrental.workflow.WorkflowRepository;

public class RentalAdminFacade {

    private final RentalFacade rentalFacade;
    private final WorkflowRepository workflowRepository;
    private final ReservationRepository reservationRepository;
    private final ProtectionRepository protectionRepository;
    private final HandoverRepository handoverRepository;

    public RentalAdminFacade() {
        this(
                new RentalFacade(),
                new WorkflowRepository(),
                new ReservationRepository(),
                new ProtectionRepository(),
                new HandoverRepository()
        );
    }

    public RentalAdminFacade(
            RentalFacade rentalFacade,
            WorkflowRepository workflowRepository,
            ReservationRepository reservationRepository,
            ProtectionRepository protectionRepository,
            HandoverRepository handoverRepository
    ) {
        this.rentalFacade = rentalFacade;
        this.workflowRepository = workflowRepository;
        this.reservationRepository = reservationRepository;
        this.protectionRepository = protectionRepository;
        this.handoverRepository = handoverRepository;
    }

    public List<Workflow> listWorkflows() {
        return workflowRepository.findAll();
    }

    public List<RentalReservation> listReservations() {
        return reservationRepository.findAll();
    }

    public List<InsuranceDeposit> listProtections() {
        return protectionRepository.findAll();
    }

    public List<VehicleHandover> listHandovers() {
        return handoverRepository.findAll();
    }

    public Workflow inspectWorkflow(String workflowId) {
        return workflowRepository.findAll()
                .stream()
                .filter(workflow ->
                        workflow.getWorkflowId().equals(workflowId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "workflow not found: " + workflowId
                ));
    }

    public Workflow manuallyCompensate(String workflowId) {
        return rentalFacade.compensate(workflowId);
    }
}
