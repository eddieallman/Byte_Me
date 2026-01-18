package com.byteme.app;

import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final EmployeeRepository employeeRepo;
    private final RescueEventRepository rescueEventRepo;
    private final BadgeRepository badgeRepo;
    private final EmployeeBadgeRepository employeeBadgeRepo;

    @GetMapping("/streak/{employeeId}")
    public ResponseEntity<?> getStreak(@PathVariable UUID employeeId) {
        var employee = employeeRepo.findById(employeeId).orElse(null);
        if (employee == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(new StreakResponse(
                employee.getCurrentStreakWeeks(),
                employee.getBestStreakWeeks(),
                employee.getLastRescueWeekStart()
        ));
    }

    @GetMapping("/impact/{employeeId}")
    public ResponseEntity<?> getImpact(@PathVariable UUID employeeId) {
        var employee = employeeRepo.findById(employeeId).orElse(null);
        if (employee == null) return ResponseEntity.notFound().build();

        long totalRescues = rescueEventRepo.countByEmployee_EmployeeId(employeeId);
        long totalMeals = rescueEventRepo.sumMealsByEmployee(employeeId);
        long totalCo2eGrams = rescueEventRepo.sumCo2eByEmployee(employeeId);
        int badgeCount = employeeBadgeRepo.findByEmployeeId(employeeId).size();

        return ResponseEntity.ok(new ImpactResponse(
                (int) totalRescues,
                (int) totalMeals,
                totalCo2eGrams / 1000.0, // Convert to kg
                employee.getCurrentStreakWeeks(),
                badgeCount
        ));
    }

    @GetMapping("/badges/{employeeId}")
    public List<EmployeeBadge> getEmployeeBadges(@PathVariable UUID employeeId) {
        return employeeBadgeRepo.findByEmployeeId(employeeId);
    }

    @GetMapping("/badges")
    public List<Badge> getAllBadges() {
        return badgeRepo.findAll();
    }

    // DTOs
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class StreakResponse {
        int currentStreakWeeks;
        int bestStreakWeeks;
        java.time.LocalDate lastRescueWeekStart;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class ImpactResponse {
        int totalRescues;
        int totalMealsSaved;
        double totalCo2eSavedKg;
        int currentStreakWeeks;
        int badgesEarned;
    }
}
