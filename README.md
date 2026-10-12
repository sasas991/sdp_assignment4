# Car Rental System - Facade Pattern

## Run

Requirements: Java 21.

```bash
./gradlew build
./gradlew run --console=plain
```

Run tests and generate the JaCoCo report:

```bash
./gradlew clean test jacocoTestReport
```

Report: `app/build/reports/jacoco/test/html/index.html`

## Architecture

**Subsystem A — Reservation and Pricing**
- `RentalReservation` - reservation status and transitions.
- `FleetAvailabilityTracker` - checks vehicle availability.
- `PricingEngine` - calculates rental cost.
- `ReservationRepository` - JSON persistence.

**Subsystem B — Insurance and Deposit**
- `InsuranceDeposit` - insurance and deposit status.
- `InsuranceCalculator` - calculates insurance cost.
- `DepositHandler` - holds and releases deposits.
- `ProtectionRepository` - JSON persistence.

**Subsystem C — Inspection and Handover**
- `VehicleHandover` - handover status and transitions.
- `DamageInspector` - rejects vehicles with serious damage.
- `GpsTracker` - starts and stops GPS tracking.
- `HandoverRepository` - JSON persistence.

`RentalFacade` coordinates A -> B -> C.
`RentalAdminFacade` provides inspection and manual compensation.
`WorkflowRepository` persists workflow progress.

## Persistence

JSON files are stored in `data/`:
- `reservations.json`
- `protections.json`
- `handovers.json`
- `workflows.json`

The repositories reload saved state after application restarts. GPS tracking is held in memory.

To reset the demo data before a fresh run:

```bash
rm -f data/reservations.json data/protections.json data/handovers.json data/workflows.json
```

## Compensation

**B fails after A:** an excessive deposit causes subsystem B to fail. The facade releases the reservation from A and marks the workflow `COMPENSATED`.

**C fails after A and B:** serious vehicle damage causes subsystem C to reject the handover. The facade releases the deposit from B first, then releases the reservation from A.

Both scenarios can be demonstrated using CLI menu options 2 and 3. Option 4 displays persisted state; option 5 triggers manual compensation.

## Principle of Least Knowledge

**Before Facade:** the client needs to know the internal classes and call them in the correct order:

```java
reservation.reserve();
reservationRepository.save(reservation);

protection.approveInsurance();
depositHandler.hold(protection);
protectionRepository.save(protection);

handover.markInspected();
handoverRepository.save(handover);
gpsTracker.startTracking(vehicleId);
handover.complete();
handoverRepository.save(handover);
```

The client has to know about many internal components and their required sequence.

**After Facade:** the client calls one high-level operation:

```java
rentalFacade.rent(
    workflowId, reservationId, vehicleId,
    rentalDays, dailyRate, depositAmount, damageLevel
);
```

The facade hides the coordination details, reducing dependencies on internal classes.

## Facade vs. Adapter

A Facade provides a simpler, unified interface to a group of subsystems. Here, `RentalFacade` coordinates reservations, insurance, deposits, vehicle inspection, GPS tracking and compensation.

An Adapter has a different purpose: it makes an existing interface compatible with another interface expected by a client. This application needs coordination of several business steps, not interface conversion, so Facade is the appropriate pattern.

## Testing

JUnit 5 tests cover business rules, status transitions, persistence, workflow recovery and both compensation scenarios. Each persistence test uses temporary storage to remain independent.

JaCoCo is used to measure coverage. The observed report reached approximately 89% instruction coverage overall; the three subsystems and both facades exceed 80% line coverage.
