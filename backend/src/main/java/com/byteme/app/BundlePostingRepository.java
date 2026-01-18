package com.byteme.app;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BundlePostingRepository extends JpaRepository<BundlePosting, UUID> {
    
    List<BundlePosting> findBySeller_SellerId(UUID sellerId);
    
    @Query("SELECT b FROM BundlePosting b WHERE b.status = 'ACTIVE' AND b.quantityReserved < b.quantityTotal AND b.pickupEndAt > :now")
    Page<BundlePosting> findAvailable(Instant now, Pageable pageable);
    
    @Query("SELECT b FROM BundlePosting b WHERE b.status = 'ACTIVE' AND b.pickupEndAt < :now")
    List<BundlePosting> findExpired(Instant now);
    
    // For analytics
    @Query("SELECT COUNT(b) FROM BundlePosting b WHERE b.seller.sellerId = :sellerId")
    long countBySeller(UUID sellerId);
}
