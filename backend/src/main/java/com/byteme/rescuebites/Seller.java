package com.byteme.rescuebites;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "seller")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID sellerId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    @Column(nullable = false)
    private String name;

    private String locationText;
    private String openingHoursText;
    private String contactStub;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
