package com.byteme.app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {
    
    List<Reservation> findByPosting_PostingId(UUID postingId);
    List<Reservation> findByOrganisation_OrgId(UUID orgId);
    List<Reservation> findByEmployee_EmployeeId(UUID employeeId);
    
    Optional<Reservation> findByClaimCodeHash(String hash);
    
    @Query("SELECT r FROM Reservation r WHERE r.status = 'RESERVED' AND r.posting.pickupEndAt < :now")
    List<Reservation> findExpired(Instant now);
    
    // For analytics
    long countByPosting_PostingIdAndStatus(UUID postingId, Reservation.Status status);
    
    @Query("SELECT r FROM Reservation r WHERE r.posting.seller.sellerId = :sellerId AND r.status = :status")
    List<Reservation> findBySellerAndStatus(UUID sellerId, Reservation.Status status);
}
