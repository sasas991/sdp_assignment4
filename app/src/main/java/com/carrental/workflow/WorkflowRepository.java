package com.carrental.workflow;

import java.io.IOException;
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
                workflows.add(Workflow.restore(
                        record.workflowId,
                        record.reservationId,
                        Workflow.Status.valueOf(record.status)
                ));
            }

            return workflows;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "failed to load workflows", e
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
                    item.getStatus().name()
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
                    "failed to save workflows", e
            );
        }
    }

    public static class StoredWorkflow {
        public String workflowId;
        public String reservationId;
        public String status;

        public StoredWorkflow() {
        }

        public StoredWorkflow(
                String workflowId,
                String reservationId,
                String status
        ) {
            this.workflowId = workflowId;
            this.reservationId = reservationId;
            this.status = status;
        }
    }
}
