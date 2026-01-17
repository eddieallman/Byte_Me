package com.byteme.rescuebites;

import lombok.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bundles")
@RequiredArgsConstructor
public class BundleController {

    private final BundlePostingRepository bundleRepo;
    private final SellerRepository sellerRepo;
    private final CategoryRepository categoryRepo;

    @GetMapping
    public Page<BundlePosting> getAvailable(Pageable pageable) {
        return bundleRepo.findAvailable(Instant.now(), pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BundlePosting> getById(@PathVariable UUID id) {
        return bundleRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/seller/{sellerId}")
    public List<BundlePosting> getBySeller(@PathVariable UUID sellerId) {
        return bundleRepo.findBySeller_SellerId(sellerId);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateBundleRequest req) {
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var seller = sellerRepo.findByUser_UserId(userId)
                .orElseThrow(() -> new RuntimeException("Not a seller"));

        var bundle = BundlePosting.builder()
                .seller(seller)
                .category(req.categoryId != null ? categoryRepo.findById(req.categoryId).orElse(null) : null)
                .pickupStartAt(req.pickupStartAt)
                .pickupEndAt(req.pickupEndAt)
                .quantityTotal(req.quantityTotal)
                .priceCents(req.priceCents)
                .discountPct(req.discountPct)
                .contentsText(req.contentsText)
                .allergensText(req.allergensText)
                .estimatedWeightGrams(req.estimatedWeightGrams)
                .status(req.activate ? BundlePosting.Status.ACTIVE : BundlePosting.Status.DRAFT)
                .build();

        return ResponseEntity.ok(bundleRepo.save(bundle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody UpdateBundleRequest req) {
        var bundle = bundleRepo.findById(id).orElse(null);
        if (bundle == null) return ResponseEntity.notFound().build();

        // Verify ownership
        UUID userId = (UUID) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!bundle.getSeller().getUser().getUserId().equals(userId)) {
            return ResponseEntity.status(403).body("Not your bundle");
        }

        if (req.quantityTotal != null) bundle.setQuantityTotal(req.quantityTotal);
        if (req.priceCents != null) bundle.setPriceCents(req.priceCents);
        if (req.discountPct != null) bundle.setDiscountPct(req.discountPct);
        if (req.contentsText != null) bundle.setContentsText(req.contentsText);
        if (req.allergensText != null) bundle.setAllergensText(req.allergensText);

        return ResponseEntity.ok(bundleRepo.save(bundle));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable UUID id) {
        var bundle = bundleRepo.findById(id).orElse(null);
        if (bundle == null) return ResponseEntity.notFound().build();
        bundle.setStatus(BundlePosting.Status.ACTIVE);
        return ResponseEntity.ok(bundleRepo.save(bundle));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable UUID id) {
        var bundle = bundleRepo.findById(id).orElse(null);
        if (bundle == null) return ResponseEntity.notFound().build();
        bundle.setStatus(BundlePosting.Status.CLOSED);
        return ResponseEntity.ok(bundleRepo.save(bundle));
    }

    // DTOs
    @Data @NoArgsConstructor @AllArgsConstructor
    public static class CreateBundleRequest {
        UUID categoryId;
        Instant pickupStartAt;
        Instant pickupEndAt;
        Integer quantityTotal;
        Integer priceCents;
        Integer discountPct;
        String contentsText;
        String allergensText;
        Integer estimatedWeightGrams;
        boolean activate;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UpdateBundleRequest {
        Integer quantityTotal;
        Integer priceCents;
        Integer discountPct;
        String contentsText;
        String allergensText;
    }
}
