package com.bookbot.hitl.controllers;
import com.bookbot.hitl.entities.ReviewDraft;
import com.bookbot.hitl.entities.ReviewStatus;
import com.bookbot.hitl.repositories.ReviewDraftRepository;
import com.bookbot.hitl.services.LedgerService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin // Enable CORS for dashboard integration
public class ReviewController {
    private final ReviewDraftRepository draftRepository;
    private final LedgerService ledgerService;
    public ReviewController(ReviewDraftRepository draftRepository, LedgerService ledgerService) {
        this.draftRepository = draftRepository;
        this.ledgerService = ledgerService;
    }
    @GetMapping
    public ResponseEntity<List<ReviewDraft>> getPendingReviews() {
        return ResponseEntity.ok(draftRepository.findByStatus(ReviewStatus.PENDING_REVIEW));
    }
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateDraft(@PathVariable UUID id, @RequestBody ReviewDraft editRequest) {
        ReviewDraft draft = draftRepository.findById(id).orElse(null);
        if (draft == null) {
            return ResponseEntity.notFound().build();
        }
        // Save human-corrected fields directly
        draft.setTransactionDate(editRequest.getTransactionDate());
        draft.setType(editRequest.getType());
        draft.setCategory(editRequest.getCategory());
        draft.setAmount(editRequest.getAmount());
        draft.setPerson(editRequest.getPerson());
        draft.setSplits(editRequest.getSplits());
        draft.setUpdatedAt(OffsetDateTime.now());
        ReviewDraft updated = draftRepository.save(draft);
        return ResponseEntity.ok(updated);
    }
    @PostMapping("/{id}/approve")
    @Transactional
    public ResponseEntity<?> approve(@PathVariable UUID id) {
        ReviewDraft draft = draftRepository.findById(id).orElse(null);
        if (draft == null) {
            return ResponseEntity.notFound().build();
        }
        draft.setStatus(ReviewStatus.ACCEPTED);
        draft.setUpdatedAt(OffsetDateTime.now());
        draftRepository.save(draft);
        // Commit transaction to production ledger
        ledgerService.commitToLedger(draft);
        return ResponseEntity.ok(Map.of(
                "id", id,
                "status", "ACCEPTED",
                "message", "Transaction committed. Confirmation released."
        ));
    }
    @PostMapping("/{id}/reject")
    @Transactional
    public ResponseEntity<?> reject(@PathVariable UUID id) {
        ReviewDraft draft = draftRepository.findById(id).orElse(null);
        if (draft == null) {
            return ResponseEntity.notFound().build();
        }
        draft.setStatus(ReviewStatus.REJECTED);
        draft.setUpdatedAt(OffsetDateTime.now());
        draftRepository.save(draft);
        return ResponseEntity.ok(Map.of(
                "id", id,
                "status", "REJECTED"
        ));
    }
}
