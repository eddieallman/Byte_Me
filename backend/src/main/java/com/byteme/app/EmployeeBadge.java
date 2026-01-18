package com.byteme.app;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "employee_badge")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(EmployeeBadge.Key.class)
public class EmployeeBadge {

    @Id
    private UUID employeeId;

    @Id
    private UUID badgeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "badge_id", insertable = false, updatable = false)
    private Badge badge;

    @Column(nullable = false)
    private Instant awardedAt = Instant.now();

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class Key implements java.io.Serializable {
        private UUID employeeId;
        private UUID badgeId;
    }
}
