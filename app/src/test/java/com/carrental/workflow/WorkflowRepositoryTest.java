package com.carrental.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void returnsEmptyListWhenFileDoesNotExist() {
        WorkflowRepository repository =
                new WorkflowRepository(tempDir.resolve("workflows.json"));

        assertTrue(repository.findAll().isEmpty());
    }

    @Test
    void savesAndLoadsWorkflow() {
        WorkflowRepository repository =
                new WorkflowRepository(tempDir.resolve("workflows.json"));

        Workflow workflow = new Workflow("WF-1", "RES-1");
        repository.save(workflow);

        List<Workflow> loaded = repository.findAll();

        assertEquals(1, loaded.size());
        assertEquals("WF-1", loaded.get(0).getWorkflowId());
        assertEquals("RES-1", loaded.get(0).getReservationId());
        assertEquals(
                Workflow.Status.PENDING_B,
                loaded.get(0).getStatus()
        );
    }

    @Test
    void persistsUpdatedWorkflowStatus() {
        WorkflowRepository repository =
                new WorkflowRepository(tempDir.resolve("workflows.json"));

        Workflow workflow = new Workflow("WF-2", "RES-2");
        workflow.markPendingC();
        repository.save(workflow);

        List<Workflow> loaded = repository.findAll();

        assertEquals(
                Workflow.Status.PENDING_C,
                loaded.get(0).getStatus()
        );
    }

    @Test
    void updatesExistingWorkflowInsteadOfDuplicatingIt() {
        WorkflowRepository repository =
                new WorkflowRepository(tempDir.resolve("workflows.json"));

        Workflow workflow = new Workflow("WF-3", "RES-3");
        repository.save(workflow);

        workflow.markPendingC();
        repository.save(workflow);

        assertEquals(1, repository.findAll().size());
        assertEquals(
                Workflow.Status.PENDING_C,
                repository.findAll().get(0).getStatus()
        );
    }

    @Test
    void persistsMultipleWorkflows() {
        WorkflowRepository repository =
                new WorkflowRepository(tempDir.resolve("workflows.json"));

        repository.save(new Workflow("WF-4", "RES-4"));
        repository.save(new Workflow("WF-5", "RES-5"));

        assertEquals(2, repository.findAll().size());
    }
}
