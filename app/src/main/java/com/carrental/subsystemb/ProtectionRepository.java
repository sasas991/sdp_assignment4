package com.carrental.subsystemb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ProtectionRepository {

    private final Path filePath;

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public ProtectionRepository() {
        this(Path.of("data", "protections.json"));
    }

    public ProtectionRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<InsuranceDeposit> findAll() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            List<StoredProtection> records = mapper.readValue(
                    filePath.toFile(),
                    new TypeReference<List<StoredProtection>>() {}
            );

            List<InsuranceDeposit> protections = new ArrayList<>();

            for (StoredProtection record : records) {
                InsuranceDeposit protection = new InsuranceDeposit(
                        record.reservationId,
                        record.insuranceCost,
                        record.depositAmount
                );

                switch (InsuranceDeposit.Status.valueOf(record.status)) {
                    case NEW -> {
                    }
                    case INSURANCE_APPROVED ->
                            protection.approveInsurance();
                    case DEPOSIT_HELD -> {
                        protection.approveInsurance();
                        protection.holdDeposit();
                    }
                    case RELEASED -> {
                        protection.approveInsurance();
                        protection.holdDeposit();
                        protection.releaseDeposit();
                    }
                    case REJECTED -> protection.reject();
                }

                protections.add(protection);
            }

            return protections;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "failed to load insurance records", e
            );
        }
    }

    public void save(InsuranceDeposit protection) {
        List<InsuranceDeposit> protections =
                new ArrayList<>(findAll());

        boolean updated = false;

        for (int i = 0; i < protections.size(); i++) {
            if (protections.get(i).getReservationId()
                    .equals(protection.getReservationId())) {
                protections.set(i, protection);
                updated = true;
                break;
            }
        }

        if (!updated) {
            protections.add(protection);
        }

        List<StoredProtection> records = new ArrayList<>();

        for (InsuranceDeposit item : protections) {
            records.add(new StoredProtection(
                    item.getReservationId(),
                    item.getInsuranceCost(),
                    item.getDepositAmount(),
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
                    "failed to save insurance records", e
            );
        }
    }

    public static class StoredProtection {
        public String reservationId;
        public java.math.BigDecimal insuranceCost;
        public java.math.BigDecimal depositAmount;
        public String status;

        public StoredProtection() {
        }

        public StoredProtection(
                String reservationId,
                java.math.BigDecimal insuranceCost,
                java.math.BigDecimal depositAmount,
                String status
        ) {
            this.reservationId = reservationId;
            this.insuranceCost = insuranceCost;
            this.depositAmount = depositAmount;
            this.status = status;
        }
    }
}
