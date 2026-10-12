package com.carrental.workflow;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WorkflowTest {

    private Workflow newWorkflow() {
        return new Workflow("WF-1", "RES-1");
    }

    @Test
    void startsWithPendingBStatus() {
        assertEquals(
                Workflow.Status.PENDING_B,
                newWorkflow().getStatus()
        );
    }

    @Test
    void movesToPendingC() {
        Workflow workflow = newWorkflow();

        workflow.markPendingC();

        assertEquals(
                Workflow.Status.PENDING_C,
                workflow.getStatus()
        );
    }

    @Test
    void completesWorkflow() {
        Workflow workflow = newWorkflow();
        workflow.markPendingC();

        workflow.complete();

        assertEquals(
                Workflow.Status.COMPLETED,
                workflow.getStatus()
        );
    }

    @Test
    void cannotCompleteBeforePendingC() {
        Workflow workflow = newWorkflow();

        assertThrows(
                IllegalStateException.class,
                workflow::complete
        );
    }

    @Test
    void cannotMarkPendingCTwice() {
        Workflow workflow = newWorkflow();
        workflow.markPendingC();

        assertThrows(
                IllegalStateException.class,
                workflow::markPendingC
        );
    }

    @Test
    void compensatesWorkflowPendingB() {
        Workflow workflow = newWorkflow();

        workflow.compensate();

        assertEquals(
                Workflow.Status.COMPENSATED,
                workflow.getStatus()
        );
    }

    @Test
    void compensatesWorkflowPendingC() {
        Workflow workflow = newWorkflow();
        workflow.markPendingC();

        workflow.compensate();

        assertEquals(
                Workflow.Status.COMPENSATED,
                workflow.getStatus()
        );
    }

    @Test
    void cannotCompensateCompletedWorkflow() {
        Workflow workflow = newWorkflow();
        workflow.markPendingC();
        workflow.complete();

        assertThrows(
                IllegalStateException.class,
                workflow::compensate
        );
    }

    @Test
    void restoresPersistedWorkflow() {
        Workflow workflow = Workflow.restore(
                "WF-2",
                "RES-2",
                Workflow.Status.PENDING_C
        );

        assertEquals("WF-2", workflow.getWorkflowId());
        assertEquals("RES-2", workflow.getReservationId());
        assertEquals(Workflow.Status.PENDING_C, workflow.getStatus());
    }

    @Test
    void rejectsBlankWorkflowId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Workflow(" ", "RES-1")
        );
    }

    @Test
    void rejectsBlankReservationId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Workflow("WF-1", " ")
        );
    }
}
