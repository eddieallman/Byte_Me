package com.byteme.app;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reservation")
public class Reservation {

    public enum Status { RESERVED, COLLECTED, NO_SHOW, EXPIRED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID reservationId;
	public UUID getReservationId() { return this.reservationId; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posting_id", nullable = false)
    private BundlePosting posting;
	public void setBundlePosting(BundlePosting bundlePostingValue) { this.posting = bundlePostingValue; }
    public void setPosting(BundlePosting posting) { this.posting = posting; }
	public BundlePosting getBundlePosting() { return this.posting; }
    public BundlePosting getPosting() { return this.posting; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id")
    private Organisation organisation;
	public Organisation getOrganisation() { return this.organisation; }
	public void setOrganisation(Organisation organisationValue) { this.organisation = organisationValue; }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;
	public void setEmployee(Employee employeeValue) { this.employee = employeeValue; }
	public Employee getEmployee() { return this.employee; }

    @Column(nullable = false)
    private Instant reservedAt = Instant.now();
	public Instant getReservedAt() { return this.reservedAt; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.RESERVED;
	public void setStatus(Status statusValue) { this.status = statusValue; }
	public Status getStatus() { return this.status; }

    @JsonIgnore
    private String claimCodeHash;
	public void setClaimCodeHash(String claimCodeHashValue) { this.claimCodeHash = claimCodeHashValue; }
	public String getClaimCodeHash() { return this.claimCodeHash; }

    private String claimCodeLast4;
	public void setClaimCodeLast4(String claimCodeLast4Value) { this.claimCodeLast4 = claimCodeLast4Value; }
	public String getClaimCodeLast4() { return this.claimCodeLast4; }

    private Instant collectedAt;
	public void setCollectedAt(Instant collectedAtValue) { this.collectedAt = collectedAtValue; }
	public Instant getCollectedAt() { return this.collectedAt; }

    private Instant noShowMarkedAt;
	public void setNoShowMarkedAt(Instant noShowMarkedAtValue) { this.noShowMarkedAt = noShowMarkedAtValue; }
	public Instant getNoShowMarkedAt() { return this.noShowMarkedAt; }

    private Instant expiredMarkedAt;
	public void setExpiredMarkedAt(Instant expiredMarkedAtValue) { this.expiredMarkedAt = expiredMarkedAtValue; }
	public Instant getExpiredMarkedAt() { return this.expiredMarkedAt; }
}
