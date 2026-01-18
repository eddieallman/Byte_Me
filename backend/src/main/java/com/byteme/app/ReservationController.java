package com.byteme.app;

import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.WeekFields;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository reservationRepo;
    private final BundlePostingRepository bundleRepo;
    private final OrganisationRepository orgRepo;
    private final EmployeeRepository employeeRepo;
    private final RescueEventRepository rescueEventRepo;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/org/{orgId}")
    public List<Reservation> getByOrg(@PathVariable UUID orgId) {
        return reservationRepo.findByOrganisation_OrgId(orgId);
    }

    @GetMapping("/employee/{employeeId}")
    public List<Reservation> getByEmployee(@PathVariable UUID employeeId) {
        return reservationRepo.findByEmployee_EmployeeId(employeeId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateReservationRequest req) {
        var bundle = bundleRepo.findById(req.postingId).orElse(null);
        if (bundle == null) return ResponseEntity.notFound().build();

        int qty = req.quantity != null ? req.quantity : 1;
        if (!bundle.canReserve(qty)) {
            return ResponseEntity.badRequest().body("Not enough bundles available");
        }

        var org = orgRepo.findById(req.organisationId).orElse(null);
        var employee = req.employeeId != null ? employeeRepo.findById(req.employeeId).orElse(null) : null;

        // Generate claim code
        String claimCode = generateClaimCode();
        String claimCodeHash = passwordEncoder.encode(claimCode);
        String claimCodeLast4 = claimCode.substring(claimCode.length() - 4);

        var reservation = Reservation.builder()
                .posting(bundle)
                .organisation(org)
                .employee(employee)
                .claimCodeHash(claimCodeHash)
                .claimCodeLast4(claimCodeLast4)
                .build();

        bundle.setQuantityReserved(bundle.getQuantityReserved() + qty);
        bundleRepo.save(bundle);

        var saved = reservationRepo.save(reservation);

        return ResponseEntity.ok(new ReservationResponse(
                saved.getReservationId(),
                claimCode, // Only returned once!
                claimCodeLast4,
                bundle.getPickupStartAt(),
                bundle.getPickupEndAt(),
                bundle.getSeller().getName(),
                bundle.getSeller().getLocationText()
        ));
    }
    

    @PostMapping("/{id}/verify")
    public ResponseEntity<?> verify(@PathVariable UUID id, @RequestBody VerifyRequest req) {
        var reservation = reservationRepo.findById(id).orElse(null);
        if (reservation == null) return ResponseEntity.notFound().build();

        if (reservation.getStatus() != Reservation.Status.RESERVED) {
            return ResponseEntity.badRequest().body("Reservation not in RESERVED status");
        }

        if (!passwordEncoder.matches(req.claimCode, reservation.getClaimCodeHash())) {
            return ResponseEntity.badRequest().body("Invalid claim code");
        }

        // Mark as collected
        reservation.setStatus(Reservation.Status.COLLECTED);
        reservation.setCollectedAt(Instant.now());
        reservationRepo.save(reservation);

        // Create rescue event if employee assigned
        if (reservation.getEmployee() != null) {
            var employee = reservation.getEmployee();
            var bundle = reservation.getPosting();
            
            // Create rescue event
            var event = RescueEvent.builder()
                    .employee(employee)
                    .reservation(reservation)
                    .collectedAt(Instant.now())
                    .mealsEstimate(1) // Simplified
                    .co2eEstimateGrams(bundle.getEstimatedWeightGrams() != null ? 
                            (int)(bundle.getEstimatedWeightGrams() * 2.5) : 500)
                    .build();
            rescueEventRepo.save(event);

            // Update streak
            updateStreak(employee);
        }

        return ResponseEntity.ok(new VerifyResponse(true, "Bundle collected successfully"));
    }

    @PostMapping("/{id}/no-show")
    public ResponseEntity<?> markNoShow(@PathVariable UUID id) {
        var reservation = reservationRepo.findById(id).orElse(null);
        if (reservation == null) return ResponseEntity.notFound().build();

        reservation.setStatus(Reservation.Status.NO_SHOW);
        reservation.setNoShowMarkedAt(Instant.now());
        
        // Release the bundle quantity
        var bundle = reservation.getPosting();
        bundle.setQuantityReserved(bundle.getQuantityReserved() - 1);
        bundleRepo.save(bundle);

        return ResponseEntity.ok(reservationRepo.save(reservation));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable UUID id) {
        var reservation = reservationRepo.findById(id).orElse(null);
        if (reservation == null) return ResponseEntity.notFound().build();

        if (reservation.getStatus() != Reservation.Status.RESERVED) {
            return ResponseEntity.badRequest().body("Can only cancel RESERVED reservations");
        }

        reservation.setStatus(Reservation.Status.CANCELLED);
        
        var bundle = reservation.getPosting();
        bundle.setQuantityReserved(bundle.getQuantityReserved() - 1);
        bundleRepo.save(bundle);

        return ResponseEntity.ok(reservationRepo.save(reservation));
    }

    @PostMapping("/{id}/assign/{employeeId}")
    public ResponseEntity<?> assignEmployee(@PathVariable UUID id, @PathVariable UUID employeeId) {
        var reservation = reservationRepo.findById(id).orElse(null);
        if (reservation == null) return ResponseEntity.notFound().build();

        var employee = employeeRepo.findById(employeeId).orElse(null);
        if (employee == null) return ResponseEntity.badRequest().body("Employee not found");

        reservation.setEmployee(employee);
        return ResponseEntity.ok(reservationRepo.save(reservation));
    }

    private String generateClaimCode() {
        var random = new SecureRandom();
        var code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void updateStreak(Employee employee) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1);

        if (employee.getLastRescueWeekStart() == null) {
            employee.setCurrentStreakWeeks(1);
        } else if (employee.getLastRescueWeekStart().equals(weekStart)) {
            // Same week, no change
        } else if (employee.getLastRescueWeekStart().plusWeeks(1).equals(weekStart)) {
            // Consecutive week
            employee.setCurrentStreakWeeks(employee.getCurrentStreakWeeks() + 1);
        } else {
            // Streak broken
            employee.setCurrentStreakWeeks(1);
        }

        employee.setLastRescueWeekStart(weekStart);
        if (employee.getCurrentStreakWeeks() > employee.getBestStreakWeeks()) {
            employee.setBestStreakWeeks(employee.getCurrentStreakWeeks());
        }
        employeeRepo.save(employee);
    }

    // DTOs
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateReservationRequest {
        UUID postingId;
        UUID organisationId;
        UUID employeeId;
        Integer quantity;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ReservationResponse {
        UUID reservationId;
        String claimCode;
        String claimCodeLast4;
        Instant pickupStartAt;
        Instant pickupEndAt;
        String sellerName;
        String sellerLocation;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class VerifyRequest {
        String claimCode;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class VerifyResponse {
        boolean success;
        String message;
    }
}
