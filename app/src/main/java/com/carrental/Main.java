package com.carrental;

import java.math.BigDecimal;
import java.util.Scanner;
import java.util.UUID;

import com.carrental.facade.RentalAdminFacade;
import com.carrental.facade.RentalFacade;
import com.carrental.subsystema.RentalReservation;
import com.carrental.subsystemb.InsuranceDeposit;
import com.carrental.subsystemc.DamageInspector;
import com.carrental.subsystemc.VehicleHandover;
import com.carrental.workflow.Workflow;

public class Main {

    public static void main(String[] args) {
        RentalFacade rentalFacade = new RentalFacade();
        RentalAdminFacade adminFacade = new RentalAdminFacade();

        try (Scanner scanner = new Scanner(System.in)) {
            boolean running = true;

            while (running) {
                System.out.println("\ncar rental system");
                System.out.println("1. rent a car");
                System.out.println("2. demo: B fails after A");
                System.out.println("3. demo: C fails after A and B");
                System.out.println("4. inspect persisted state");
                System.out.println("5. manually compensate a workflow");
                System.out.println("0. exit");
                System.out.print("Choose: ");

                String choice = scanner.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> createRental(
                                scanner, rentalFacade, adminFacade,
                                DamageInspector.DamageLevel.NONE, false
                        );
                        case "2" -> createRental(
                                scanner, rentalFacade, adminFacade,
                                DamageInspector.DamageLevel.NONE, true
                        );
                        case "3" -> createRental(
                                scanner, rentalFacade, adminFacade,
                                DamageInspector.DamageLevel.SERIOUS, false
                        );
                        case "4" -> inspectState(adminFacade);
                        case "5" -> manualCompensation(
                                scanner, adminFacade
                        );
                        case "0" -> running = false;
                        default -> System.out.println("invalid menu option");
                    }
                } catch (RuntimeException e) {
                    System.out.println("operation failed: " + e.getMessage());
                }
            }
        }

        System.out.println("application closed.");
    }

    private static void createRental(
            Scanner scanner,
            RentalFacade facade,
            RentalAdminFacade admin,
            DamageInspector.DamageLevel damageLevel,
            boolean forceBFailure
    ) {
        String vehicleId = readText(scanner, "vehicle ID: ");
        int rentalDays = readPositiveInt(scanner, "rental days: ");
        BigDecimal dailyRate = readPositiveMoney(
                scanner, "daily rate: "
        );

        BigDecimal deposit = forceBFailure
                ? new BigDecimal("600000")
                : new BigDecimal("100000");

        String workflowId = newId("WF");
        String reservationId = newId("RES");

        System.out.println("workflow ID: " + workflowId);
        System.out.println("reservation ID: " + reservationId);

        if (forceBFailure) {
            System.out.println(
                    "demo: deposit exceeds the 500000 limit."
            );
        }

        try {
            Workflow workflow = facade.rent(
                    workflowId,
                    reservationId,
                    vehicleId,
                    rentalDays,
                    dailyRate,
                    deposit,
                    damageLevel
            );

            System.out.println(
                    "rental completed. status: " + workflow.getStatus()
            );

        } catch (RuntimeException e) {
            System.out.println("rental failed: " + e.getMessage());

            if (e.getCause() != null) {
                System.out.println(
                        "reason: " + e.getCause().getMessage()
                );
            }

            try {
                Workflow workflow = admin.inspectWorkflow(workflowId);
                System.out.println(
                        "persisted workflow status: " + workflow.getStatus()
                );
            } catch (RuntimeException ignored) {
                System.out.println("workflow could not be reloaded.");
            }
        }
    }

    private static void inspectState(RentalAdminFacade admin) {
        System.out.println("\nworkflows");
        for (Workflow item : admin.listWorkflows()) {
            System.out.println(
                    item.getWorkflowId() + " | "
                            + item.getReservationId() + " | "
                            + item.getStatus()
            );
        }

        System.out.println("\nreservations");
        for (RentalReservation item : admin.listReservations()) {
            System.out.println(
                    item.getReservationId() + " | "
                            + item.getVehicleId() + " | "
                            + item.getStatus()
            );
        }

        System.out.println("\ninsurance and deposits");
        for (InsuranceDeposit item : admin.listProtections()) {
            System.out.println(
                    item.getReservationId() + " | "
                            + item.getStatus() + " | deposit: "
                            + item.getDepositAmount()
            );
        }

        System.out.println("\nhandovers");
        for (VehicleHandover item : admin.listHandovers()) {
            System.out.println(
                    item.getReservationId() + " | "
                            + item.getVehicleId() + " | "
                            + item.getStatus()
            );
        }
    }

    private static void manualCompensation(
            Scanner scanner,
            RentalAdminFacade admin
    ) {
        String workflowId = readText(
                scanner, "workflow ID to compensate: "
        );

        Workflow workflow = admin.manuallyCompensate(workflowId);

        System.out.println(
                "workflow status: " + workflow.getStatus()
        );
    }

    private static String newId(String prefix) {
        return prefix + "-"
                + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String readText(
            Scanner scanner, String prompt
    ) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static int readPositiveInt(
            Scanner scanner, String prompt
    ) {
        while (true) {
            System.out.print(prompt);

            try {
                int value = Integer.parseInt(scanner.nextLine().trim());

                if (value > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }

            System.out.println("enter a positive whole number.");
        }
    }

    private static BigDecimal readPositiveMoney(
            Scanner scanner, String prompt
    ) {
        while (true) {
            System.out.print(prompt);

            try {
                BigDecimal value = new BigDecimal(
                        scanner.nextLine().trim()
                );

                if (value.signum() > 0) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }

            System.out.println("enter a positive amount");
        }
    }
}
