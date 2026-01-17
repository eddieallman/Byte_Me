package com.byteme.rescuebites;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Reservation {

    public enum Status { RESERVED, COLLECTED, NO_SHOW, EXPIRED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private BundlePosting posting;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organisation organisation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(nullable = false)
    private Instant reservedAt = Instant.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.RESERVED;

    @JsonIgnore
    private String claimCodeHash;

    private String claimCodeLast4;

    private Instant collectedAt;
    private Instant noShowMarkedAt;
    private Instant expiredMarkedAt;
}
