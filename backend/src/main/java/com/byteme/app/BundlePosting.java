package com.byteme.app;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bundle_posting")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BundlePosting {

    public enum Status { DRAFT, ACTIVE, CLOSED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID postingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false)
    private Instant pickupStartAt;

    @Column(nullable = false)
    private Instant pickupEndAt;

    @Column(nullable = false)
    private Integer quantityTotal;

    @Column(nullable = false)
    private Integer quantityReserved = 0;

    @Column(nullable = false)
    private Integer priceCents;

    @Column(nullable = false)
    private Integer discountPct;

    @Column(columnDefinition = "TEXT")
    private String contentsText;

    private String allergensText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.DRAFT;

    private Integer estimatedWeightGrams;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // Helper methods
    public boolean canReserve(int qty) {
        return status == Status.ACTIVE && (quantityReserved + qty) <= quantityTotal;
    }

    public int getAvailable() {
        return quantityTotal - quantityReserved;
    }
}
