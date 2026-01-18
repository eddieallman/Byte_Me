package com.byteme.app;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organisation")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Organisation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orgId;

    @Column(nullable = false)
    private String name;

    private String locationText;
    private String billingStub;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
