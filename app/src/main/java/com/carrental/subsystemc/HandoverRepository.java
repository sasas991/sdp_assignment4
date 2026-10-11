package com.carrental.subsystemc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class HandoverRepository {

    private final Path filePath;

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public HandoverRepository() {
        this(Path.of("data", "handovers.json"));
    }

    public HandoverRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<VehicleHandover> findAll() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            List<StoredHandover> records = mapper.readValue(
                    filePath.toFile(),
                    new TypeReference<List<StoredHandover>>() {}
            );

            List<VehicleHandover> handovers = new ArrayList<>();

            for (StoredHandover record : records) {
                VehicleHandover handover = new VehicleHandover(
                        record.reservationId,
                        record.vehicleId
                );

                switch (VehicleHandover.Status.valueOf(record.status)) {
                    case NEW -> {
                    }
                    case INSPECTED -> handover.markInspected();
                    case COMPLETED -> {
                        handover.markInspected();
                        handover.complete();
                    }
                    case REJECTED -> handover.reject();
                }

                handovers.add(handover);
            }

            return handovers;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "failed to load handovers", e
            );
        }
    }

    public void save(VehicleHandover handover) {
        List<VehicleHandover> handovers =
                new ArrayList<>(findAll());

        boolean updated = false;

        for (int i = 0; i < handovers.size(); i++) {
            if (handovers.get(i).getReservationId()
                    .equals(handover.getReservationId())) {
                handovers.set(i, handover);
                updated = true;
                break;
            }
        }

        if (!updated) {
            handovers.add(handover);
        }

        List<StoredHandover> records = new ArrayList<>();

        for (VehicleHandover item : handovers) {
            records.add(new StoredHandover(
                    item.getReservationId(),
                    item.getVehicleId(),
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
                    "failed to save handovers", e
            );
        }
    }

    public static class StoredHandover {
        public String reservationId;
        public String vehicleId;
        public String status;

        public StoredHandover() {
        }

        public StoredHandover(
                String reservationId,
                String vehicleId,
                String status
        ) {
            this.reservationId = reservationId;
            this.vehicleId = vehicleId;
            this.status = status;
        }
    }
}
