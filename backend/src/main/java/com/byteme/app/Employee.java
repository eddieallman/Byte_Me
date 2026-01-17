package com.byteme.app;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employee")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID employeeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organisation organisation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(nullable = false)
    private String displayName;

    // Streak tracking (denormalized for simplicity)
    @Column(nullable = false)
    private Integer currentStreakWeeks = 0;

    @Column(nullable = false)
    private Integer bestStreakWeeks = 0;

    private LocalDate lastRescueWeekStart;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
