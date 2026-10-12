package com.carrental.subsystemb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProtectionRepositoryTest {

    @TempDir
    Path tempDir;

    private ProtectionRepository newRepository() {
        return new ProtectionRepository(
                tempDir.resolve("protections.json")
        );
    }

    private InsuranceDeposit newDeposit(String id) {
        return new InsuranceDeposit(
                id,
                new BigDecimal("2000.00"),
                new BigDecimal("100000")
        );
    }

    @Test
    void returnsEmptyListWhenFileDoesNotExist() {
        assertTrue(newRepository().findAll().isEmpty());
    }

    @Test
    void savesAndLoadsApprovedInsurance() {
        ProtectionRepository repository = newRepository();
        InsuranceDeposit deposit = newDeposit("RES-1");
        deposit.approveInsurance();

        repository.save(deposit);

        List<InsuranceDeposit> loaded = repository.findAll();

        assertEquals(1, loaded.size());
        assertEquals("RES-1", loaded.get(0).getReservationId());
        assertEquals(
                new BigDecimal("2000.00"),
                loaded.get(0).getInsuranceCost()
        );
        assertEquals(
                InsuranceDeposit.Status.INSURANCE_APPROVED,
                loaded.get(0).getStatus()
        );
    }

    @Test
    void persistsHeldDepositStatus() {
        ProtectionRepository repository = newRepository();
        InsuranceDeposit deposit = newDeposit("RES-2");
        deposit.approveInsurance();
        deposit.holdDeposit();

        repository.save(deposit);

        assertEquals(
                InsuranceDeposit.Status.DEPOSIT_HELD,
                repository.findAll().get(0).getStatus()
        );
    }

    @Test
    void persistsReleasedDepositStatus() {
        ProtectionRepository repository = newRepository();
        InsuranceDeposit deposit = newDeposit("RES-3");
        deposit.approveInsurance();
        deposit.holdDeposit();
        deposit.releaseDeposit();

        repository.save(deposit);

        assertEquals(
                InsuranceDeposit.Status.RELEASED,
                repository.findAll().get(0).getStatus()
        );
    }

    @Test
    void updatesExistingRecordInsteadOfDuplicatingIt() {
        ProtectionRepository repository = newRepository();
        InsuranceDeposit deposit = newDeposit("RES-4");
        deposit.approveInsurance();

        repository.save(deposit);

        deposit.holdDeposit();
        repository.save(deposit);

        List<InsuranceDeposit> loaded = repository.findAll();

        assertEquals(1, loaded.size());
        assertEquals(
                InsuranceDeposit.Status.DEPOSIT_HELD,
                loaded.get(0).getStatus()
        );
    }

    @Test
    void persistsMultipleRecords() {
        ProtectionRepository repository = newRepository();

        repository.save(newDeposit("RES-5"));
        repository.save(newDeposit("RES-6"));

        assertEquals(2, repository.findAll().size());
    }
}
