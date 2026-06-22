package com.bookbot.hitl.consumers;

import com.bookbot.hitl.entities.ReviewDraft;
import com.bookbot.hitl.entities.ReviewStatus;
import com.bookbot.hitl.models.DraftPayload;
import com.bookbot.hitl.repositories.ReviewDraftRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
@Service
public class DraftConsumer {
    private final ReviewDraftRepository draftRepository;
    private final ObjectMapper objectMapper;
    public DraftConsumer(ReviewDraftRepository draftRepository, ObjectMapper objectMapper) {
        this.draftRepository = draftRepository;
        this.objectMapper = objectMapper;
    }
    @KafkaListener(topics = "DRAFT-TRANSACTION", groupId = "hitl-governance-group")
    @Transactional
    public void consumeDraft(String message) {
        try {
            DraftPayload payload = objectMapper.readValue(message, DraftPayload.class);
            // 1. Check for duplicate transactions within last 24h
            List<ReviewDraft> duplicateCheck = draftRepository.findDuplicateInLast24Hours(
                    payload.amount(), payload.person(), OffsetDateTime.now().minusHours(24)
            );
            boolean isDuplicate = !duplicateCheck.isEmpty();
            UUID duplicateOfId = isDuplicate ? duplicateCheck.getFirst().getId() : null;
            // 2. Map payload straight to PENDING_REVIEW
            ReviewDraft draft = new ReviewDraft();
            draft.setTransactionDate(payload.transactionDate());
            draft.setType(payload.type());
            draft.setCategory(payload.category());
            draft.setAmount(payload.amount());
            draft.setPerson(payload.person());
            draft.setSplits(payload.splits());
            draft.setStatus(ReviewStatus.PENDING_REVIEW);
            draft.setDuplicate(isDuplicate);
            draft.setDuplicateOfId(duplicateOfId);
            draft.setMediaUrl(payload.mediaUrl());
            draft.setMediaType(payload.mediaType());
            draft.setRawOcrOrTranscript(payload.rawOcrOrTranscript());
            draftRepository.save(draft);
        } catch (Exception e) {
            System.err.println("Error processing incoming draft payload: " + e.getMessage());
        }
    }
}
