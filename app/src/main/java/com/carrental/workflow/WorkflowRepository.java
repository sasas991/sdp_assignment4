
package com.carrental.workflow;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class WorkflowRepository {

    private final Path filePath;

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public WorkflowRepository() {
        this(Path.of("data", "workflows.json"));
    }

    public WorkflowRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<Workflow> findAll() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            List<StoredWorkflow> records = mapper.readValue(
                    filePath.toFile(),
                    new TypeReference<List<StoredWorkflow>>() {}
            );

            List<Workflow> workflows = new ArrayList<>();

            for (StoredWorkflow record : records) {
                Workflow workflow = Workflow.restore(
                        record.workflowId,
                        record.reservationId,
                        Workflow.Status.valueOf(record.status)
                );

                if (record.vehicleId != null
                        && record.rentalDays > 0
                        && record.dailyRate != null
                        && record.depositAmount != null
                        && record.damageLevel != null) {

                    workflow.setRentalDetails(
                            record.vehicleId,
                            record.rentalDays,
                            record.dailyRate,
                            record.depositAmount,
                            record.damageLevel
                    );
                }

                workflows.add(workflow);
            }

            return workflows;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load workflows",
                    e
            );
        }
    }

    public void save(Workflow workflow) {
        List<Workflow> workflows = new ArrayList<>(findAll());

        boolean updated = false;

        for (int i = 0; i < workflows.size(); i++) {
            if (workflows.get(i).getWorkflowId()
                    .equals(workflow.getWorkflowId())) {

                workflows.set(i, workflow);
                updated = true;
                break;
            }
        }

        if (!updated) {
            workflows.add(workflow);
        }

        List<StoredWorkflow> records = new ArrayList<>();

        for (Workflow item : workflows) {
            records.add(new StoredWorkflow(
                    item.getWorkflowId(),
                    item.getReservationId(),
                    item.getStatus().name(),
                    item.getVehicleId(),
                    item.getRentalDays(),
                    item.getDailyRate(),
                    item.getDepositAmount(),
                    item.getDamageLevel()
            ));
        }

        try {
            Path parent = filePath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            mapper.writeValue(filePath.toFile(), records);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to save workflows",
                    e
            );
        }
    }

    public static class StoredWorkflow {

        public String workflowId;
        public String reservationId;
        public String status;

        public String vehicleId;
        public int rentalDays;
        public BigDecimal dailyRate;
        public BigDecimal depositAmount;
        public String damageLevel;

        public StoredWorkflow() {
        }

        public StoredWorkflow(
                String workflowId,
                String reservationId,
                String status,
                String vehicleId,
                int rentalDays,
                BigDecimal dailyRate,
                BigDecimal depositAmount,
                String damageLevel
        ) {
            this.workflowId = workflowId;
            this.reservationId = reservationId;
            this.status = status;
            this.vehicleId = vehicleId;
            this.rentalDays = rentalDays;
            this.dailyRate = dailyRate;
            this.depositAmount = depositAmount;
            this.damageLevel = damageLevel;
        }
    }
}
