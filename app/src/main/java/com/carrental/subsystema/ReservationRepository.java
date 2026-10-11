package com.carrental.subsystema;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ReservationRepository {

    private final Path filePath;

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    public ReservationRepository() {
        this(Path.of("data", "reservations.json"));
    }

    public ReservationRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<RentalReservation> findAll() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try {
            List<StoredReservation> records = mapper.readValue(
                    filePath.toFile(),
                    new TypeReference<List<StoredReservation>>() {}
            );

            List<RentalReservation> reservations = new ArrayList<>();

            for (StoredReservation record : records) {
                RentalReservation reservation = new RentalReservation(
                        record.reservationId,
                        record.vehicleId,
                        record.rentalDays
                );

                if (record.status.equals("RESERVED")
                        || record.status.equals("RELEASED")) {
                    reservation.reserve();
                }

                if (record.status.equals("RELEASED")) {
                    reservation.release();
                }

                reservations.add(reservation);
            }

            return reservations;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "failed to load reservations", e
            );
        }
    }

    public void save(RentalReservation reservation) {
        List<RentalReservation> reservations =
                new ArrayList<>(findAll());

        boolean updated = false;

        for (int i = 0; i < reservations.size(); i++) {
            if (reservations.get(i).getReservationId()
                    .equals(reservation.getReservationId())) {
                reservations.set(i, reservation);
                updated = true;
                break;
            }
        }

        if (!updated) {
            reservations.add(reservation);
        }

        List<StoredReservation> records = new ArrayList<>();

        for (RentalReservation item : reservations) {
            records.add(new StoredReservation(
                    item.getReservationId(),
                    item.getVehicleId(),
                    item.getRentalDays(),
                    item.getStatus().name()
            ));
        }

        try {
            Files.createDirectories(filePath.getParent());

            mapper.writeValue(filePath.toFile(), records);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "failed to save reservations", e
            );
        }
    }

    public static class StoredReservation {
        public String reservationId;
        public String vehicleId;
        public int rentalDays;
        public String status;

        public StoredReservation() {
        }

        public StoredReservation(
                String reservationId,
                String vehicleId,
                int rentalDays,
                String status
        ) {
            this.reservationId = reservationId;
            this.vehicleId = vehicleId;
            this.rentalDays = rentalDays;
            this.status = status;
        }
    }
}
