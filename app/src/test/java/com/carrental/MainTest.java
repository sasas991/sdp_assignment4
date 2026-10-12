package com.carrental;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class MainTest {

    private String runMain(String input) throws Exception {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        try {
            System.setIn(new ByteArrayInputStream(
                    input.getBytes(StandardCharsets.UTF_8)
            ));
            System.setOut(new PrintStream(
                    output, true, StandardCharsets.UTF_8
            ));

            Main.main(new String[0]);
        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }

        return output.toString(StandardCharsets.UTF_8);
    }

    @Test
    void displaysMenuAndExits() throws Exception {
        String output = runMain("0\n");

        assertTrue(output.contains("car rental system"));
        assertTrue(output.contains("1. rent a car"));
        assertTrue(output.contains("application closed."));
    }

    @Test
    void handlesInvalidMenuOption() throws Exception {
        String output = runMain("invalid\n0\n");

        assertTrue(output.contains("invalid menu option"));
    }

    @Test
    void handlesRentalWithBlankVehicleId() throws Exception {
        String output = runMain("1\n\n1\n1000\n0\n");

        assertTrue(output.contains("rental failed:"));
        assertTrue(output.contains("vehicle ID is required"));
    }

    @Test
    void displaysSubsystemBFailureDemo() throws Exception {
        String output = runMain("2\n\n1\n1000\n0\n");

        assertTrue(output.contains(
                "demo: deposit exceeds the 500000 limit."
        ));
        assertTrue(output.contains("rental failed:"));
    }

    @Test
    void displaysPersistedStateSections() throws Exception {
        String output = runMain("4\n0\n");

        assertTrue(output.contains("workflows"));
        assertTrue(output.contains("reservations"));
        assertTrue(output.contains("insurance and deposits"));
        assertTrue(output.contains("handovers"));
    }

    @Test
    void handlesManualCompensationOfUnknownWorkflow()
            throws Exception {
        String output = runMain(
                "5\nWF-DOES-NOT-EXIST-TEST\n0\n"
        );

        assertTrue(output.contains("operation failed:"));
        assertTrue(output.contains("workflow not found"));
    }
}
